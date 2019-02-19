package com.r3.corda.sdk.token.contracts.behaviours

import com.r3.corda.sdk.token.contracts.IFungibleTokenContract
import com.r3.corda.sdk.token.contracts.IFungibleTokenState
import com.r3.corda.sdk.token.contracts.VerifyTokenCommandMethod
import com.r3.corda.sdk.token.contracts.commands.IssueTokenCommand
import com.r3.corda.sdk.token.contracts.commands.RedeemTokenCommand
import com.r3.corda.sdk.token.contracts.types.EmbeddableToken
import net.corda.core.contracts.TransactionVerificationException
import net.corda.core.contracts.requireSingleCommand
import net.corda.core.contracts.select
import net.corda.core.transactions.LedgerTransaction

interface RedeemableFungibleTokenContract<T: EmbeddableToken> : IFungibleTokenContract<T> {

    // Standard behaviour for Issue
    @VerifyTokenCommandMethod(RedeemTokenCommand::class)
    override fun verifyRedeem(
            command: RedeemTokenCommand<T>,
            inputs: List<IFungibleTokenState<T>>,
            outputs: List<IFungibleTokenState<T>>,
            tx: LedgerTransaction
    ) {
        // Get any other commands for this token and verify that only one Issue command is present
        val commands = tx.commands.select<RedeemTokenCommand<T>>().filter { it.value.token == command.token }
        commands.requireSingleCommand<RedeemTokenCommand<T>>()
        val commandWithParties = commands.single()

        // Verify all inputs
        inputs.apply {
            require(isNotEmpty()) { "When redeeming tokens, there must be input states present." }

            // No zero-value or negative amounts may be issued
            val hasZeroAmounts = any { it.quantity <= 0L }
            require(hasZeroAmounts.not()) { "You cannot redeem tokens with a zero amount." }

            // There can only be one issuer per group as the issuer is part of the token which is used to group states.
            // If there are multiple issuers for the same tokens then there will be a group for each issued token. So,
            // the line below should never fail on single().
            val issuer = map { it.token.issuer }.toSet().single().owningKey

            // Should only be one signer
            val signer = commandWithParties.signers.toSet().single()

            // Only the issuer may sign the issue command.
            require(issuer == signer) {
                "The issuer must be the only signing party when an amount of tokens are redeemed."
            }
        }

        // Verify no outputs
        outputs.apply {
            require(isEmpty()) { "When redeeming tokens, there cannot be any output states." }
        }
    }

}