package com.r3.corda.sdk.token.workflow.flows

import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.sdk.token.contracts.SubscriptionContract
import com.r3.corda.sdk.token.contracts.states.EvolvableTokenType
import com.r3.corda.sdk.token.workflow.schemas.DistributionRecord
import net.corda.core.contracts.StateAndRef
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

/**
 * Simple flow to persist a new entity pertaining to a distribution record.
 * TODO: Add some error handling.
 */
@InitiatingFlow
@StartableByRPC
class AddPartyToDistributionList(val party: Party, val evolvableTokenType: StateAndRef<EvolvableTokenType>) : FlowLogic<SignedTransaction>() {

    companion object {
        object CREATING : ProgressTracker.Step("Creating subscription proposal.")

        object SIGNING : ProgressTracker.Step("Signing transaction proposal.")

        object COLLECTING : ProgressTracker.Step("Gathering other maintainer signatures.") {
            override fun childProgressTracker() = CollectSignaturesFlow.tracker()
        }

        object RECORDING : ProgressTracker.Step("Recording signed transaction.") {
            override fun childProgressTracker() = FinalityFlow.tracker()
        }

        fun tracker() = ProgressTracker(CREATING, SIGNING, COLLECTING, RECORDING)
    }

    override val progressTracker: ProgressTracker = tracker()

    @Suspendable
    override fun call(): SignedTransaction {

        // Create the proposed subscription
        progressTracker.currentStep = CREATING
        val distributionRecord = DistributionRecord(evolvableTokenType.state.data.linearId.id, party)
        val signingKeys = maintainers().map { it.owningKey }
        val utx: TransactionBuilder = TransactionBuilder(notary = evolvableTokenType.state.notary).apply {
            addCommand(data = SubscriptionContract.Subscribe(party), keys = signingKeys)
            addReferenceState(evolvableTokenType.referenced())
            addOutputState(state = distributionRecord, contract = distributionRecordContract.ID)
        }

        // Sign the transaction proposal
        progressTracker.currentStep = SIGNING
        val ptx: SignedTransaction = serviceHub.signInitialTransaction(utx)

        // Gather signatures from the subscriber and all maintainers
        progressTracker.currentStep = COLLECTING
        val otherMaintainerSessions = otherMaintainers().map { initiateFlow(it) }
        val stx = subFlow(CollectSignaturesFlow(
                partiallySignedTx = ptx,
                sessionsToCollectFrom = otherMaintainerSessions,
                progressTracker = COLLECTING.childProgressTracker()
        ))

        // Finalize and record
        progressTracker.currentStep = RECORDING
        return subFlow(FinalityFlow(transaction = stx, sessions = observerSessions))

        // Create an persist a new entity.
        serviceHub.withEntityManager { persist(distributionRecord) }
    }

    private fun maintainers(): Set<Party> {
        return evolvableTokenType.state.data.maintainers.toSet()
    }

    private fun otherMaintainers(): Set<Party> {
        return maintainers().minus(this.ourIdentity)
    }

}
