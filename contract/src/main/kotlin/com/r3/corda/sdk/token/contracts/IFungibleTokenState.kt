package com.r3.corda.sdk.token.contracts

import com.r3.corda.sdk.token.contracts.commands.MoveTokenCommand
import com.r3.corda.sdk.token.contracts.types.EmbeddableToken
import com.r3.corda.sdk.token.contracts.types.IssuedToken
import net.corda.core.contracts.Amount
import net.corda.core.contracts.ContractState
import net.corda.core.identity.AbstractParty

interface IFungibleTokenState<T : EmbeddableToken> : ContractState, ITokenState {

    // TODO: Remove this amount!
    val amount: Amount<IssuedToken<T>> get() = Amount(quantity, token)

    /**
     *  Token definition for this token state, wrapped in an [IssuedToken].
     */
    val token: IssuedToken<T>

    /** The current [owner] of this token. */
    val owner: AbstractParty

    /**
     * Helper function for changing the ownership of this token. Copies the underlying data structure, updating only
     * the [owner] field, and provides a ready-made [MoveTokenCommand] for ease of use in a [TransactionBuilder].
     * */
    fun withNewOwner(newOwner: AbstractParty): Pair<MoveTokenCommand<T>, IFungibleTokenState<T>>

    /**
     *  Quantity of this token held. Details on presentation, divisible elements, etc. are defined by [token].
     */
    val quantity: Long

    /**
     * The [participants] list for a [IFungibleTokenState] is always the current [owner].
     */
    override val participants: List<AbstractParty> get() = listOf(owner)
}