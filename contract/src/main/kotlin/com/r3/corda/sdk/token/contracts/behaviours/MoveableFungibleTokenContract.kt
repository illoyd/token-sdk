package com.r3.corda.sdk.token.contracts.behaviours

import com.r3.corda.sdk.token.contracts.IFungibleTokenContract
import com.r3.corda.sdk.token.contracts.IFungibleTokenState
import com.r3.corda.sdk.token.contracts.VerifyTokenCommandMethod
import com.r3.corda.sdk.token.contracts.commands.IssueTokenCommand
import com.r3.corda.sdk.token.contracts.commands.MoveTokenCommand
import com.r3.corda.sdk.token.contracts.types.EmbeddableToken
import com.r3.corda.sdk.token.contracts.types.IssuedToken
import net.corda.core.contracts.TransactionVerificationException
import net.corda.core.contracts.requireSingleCommand
import net.corda.core.contracts.select
import net.corda.core.internal.sumByLong
import net.corda.core.transactions.LedgerTransaction

interface MoveableFungibleTokenContract<T: EmbeddableToken> : IFungibleTokenContract<T> {

    // Standard behaviour for Issue
    @VerifyTokenCommandMethod(MoveTokenCommand::class)
    override fun verifyMove(
            command: MoveTokenCommand<T>,
            inputs: List<IFungibleTokenState<T>>,
            outputs: List<IFungibleTokenState<T>>,
            tx: LedgerTransaction
    ) {
        // Get any other move commands for this token
        val commands = tx.commands.select<MoveTokenCommand<T>>().filter { it.value.token == command.token }

        // Verify all inputs
        inputs.apply {
            require(isNotEmpty()) { "When moving tokens, there must be input states present." }

            // No zero-value or negative amounts may be moved
            val hasZeroAmounts = any { it.quantity <= 0L }
            require(hasZeroAmounts.not()) { "You cannot input tokens with a zero amount." }
        }

        // Verify all outputs
        outputs.apply {
            require(isNotEmpty()) { "When moving tokens, there must be output states present." }

            // No zero-value or negative amounts may be moved
            val hasZeroAmounts = any { it.quantity <= 0L }
            require(hasZeroAmounts.not()) { "You cannot output tokens with a zero amount." }
        }

        // Ensure that output amounts match input amounts
        val totalInputQuantity = inputs.sumByLong { it.quantity }
        val totalOutputQuantity = outputs.sumByLong { it.quantity }
        require(totalInputQuantity == totalOutputQuantity) {
            "In move groups the amount of input tokens MUST EQUAL the amount of output tokens. In other words, you " +
                    "cannot create or destroy value when moving tokens."
        }

        // Ensure that all owners have signed at least one Move command
        val owners = inputs.map { it.owner.owningKey }.toSet()
        val signers = commands.flatMap { it.signers }.toSet()
        require(owners == signers) {
            "There are required signers missing or some of the specified signers are not required. A transaction " +
                    "to move owned token amounts must be signed by ONLY ALL the owners of ALL the input owned " +
                    "token amounts."
        }
    }

}