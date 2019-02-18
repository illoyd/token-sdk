package com.r3.corda.sdk.token.contracts

import com.r3.corda.sdk.token.contracts.commands.IssueTokenCommand
import com.r3.corda.sdk.token.contracts.commands.MoveTokenCommand
import com.r3.corda.sdk.token.contracts.commands.TokenCommand
import com.r3.corda.sdk.token.contracts.commands.RedeemTokenCommand
import com.r3.corda.sdk.token.contracts.types.EmbeddableToken
import com.r3.corda.sdk.token.contracts.types.IssuedToken
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.CommandWithParties
import net.corda.core.contracts.Contract
import net.corda.core.transactions.LedgerTransaction

interface IFungibleTokenContract<T : EmbeddableToken> : Contract {
    // Standard behaviour for Issue
    fun verify(
            command: IssueTokenCommand<T>,
            inputs: List<IFungibleTokenState<T>>,
            outputs: List<IFungibleTokenState<T>>,
            tx: LedgerTransaction
    )

    // Standard behaviour for Move
    fun verify(
            command: MoveTokenCommand<T>,
            inputs: List<IFungibleTokenState<T>>,
            outputs: List<IFungibleTokenState<T>>,
            tx: LedgerTransaction
    )

    // Standard behaviour for Redeem
    fun verify(
            command: RedeemTokenCommand<T>,
            inputs: List<IFungibleTokenState<T>>,
            outputs: List<IFungibleTokenState<T>>,
            tx: LedgerTransaction
    )

    // Fallback for any unrecognised Token Commands
    fun verify(
            command: TokenCommand<T>,
            inputs: List<IFungibleTokenState<T>>,
            outputs: List<IFungibleTokenState<T>>,
            tx: LedgerTransaction
    )

    // Fallback for any unrecognised commands
    fun verify(
            command: CommandData,
            inputs: List<IFungibleTokenState<T>>,
            outputs: List<IFungibleTokenState<T>>,
            tx: LedgerTransaction
    )

}