package com.r3.corda.sdk.token.contracts

import com.r3.corda.sdk.token.contracts.states.Subscription
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.requireThat
import net.corda.core.identity.Party
import net.corda.core.transactions.LedgerTransaction

class SubscriptionContract : Contract {

    class Subscribe(val party: Party) : CommandData
    class Unsubscribe(val party: Party) : CommandData


    override fun verify(tx: LedgerTransaction) {
        requireThat {
            // Verify that exactly one EvolvableTokenType is submitted in this transaction.
            "Only one reference state may be provided when making a new subscription." using (tx.referenceInputsOfType<LinearState>().size == 1)
            val subscribedState = tx.referenceInputsOfType<LinearState>().single()

            // Group subscriptions by party
            val groups = groupStates(tx)
            val commands = groups.

            // For every subscribe command, verify that a subscription is created

            // For every unsubscribe command, verify that the
        }
    }

    fun groupStates(tx: LedgerTransaction): List<LedgerTransaction.InOutGroup<Subscription, Party>> {
        return tx.groupStates { state -> state.subscriber }
    }


    fun verifySubscribe(commands: List<CommandData>, inputs: List<>)
}