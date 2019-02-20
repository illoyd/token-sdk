package com.r3.corda.sdk.token.contracts

import com.r3.corda.sdk.token.contracts.commands.MoveTokenCommand
import com.r3.corda.sdk.token.contracts.types.EmbeddableToken
import com.r3.corda.sdk.token.contracts.types.IssuedToken
import net.corda.core.contracts.Amount
import net.corda.core.contracts.ContractState
import net.corda.core.identity.AbstractParty

interface IFungibleTokenState<T : EmbeddableToken> : ContractState, ITokenState<T> {

    /**
     * Holds the [Amount] of this [IssuedToken].
     */
    val amount: Amount<IssuedToken<T>>

    /**
     *  Token definition for this token state, wrapped in an [IssuedToken].
     */
    override val token: IssuedToken<T> get() = amount.token

    /**
     *  Quantity of this token held. Details on presentation, divisible elements, etc. are defined by [token].
     */
    val quantity: Long get() = amount.quantity

    /**
     * Isser for this [IssuedToken].
     */
    val issuer: AbstractParty get() = token.issuer

    /**
     * The [participants] list for a [IFungibleTokenState] is always the current [owner].
     */
    override val participants: List<AbstractParty> get() = listOf(owner)

    /**
     * Helper function for changing the ownership of this token. Copies the underlying data structure, updating only
     * the [owner] field, and provides a ready-made [MoveTokenCommand] for ease of use in a [TransactionBuilder].
     * */
    fun withNewOwner(newOwner: AbstractParty): Pair<MoveTokenCommand<T>, IFungibleTokenState<T>>

}