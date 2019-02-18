package com.r3.corda.sdk.token.contracts

import com.r3.corda.sdk.token.contracts.commands.MoveTokenCommand
import com.r3.corda.sdk.token.contracts.types.EmbeddableToken
import com.r3.corda.sdk.token.contracts.types.IssuedToken
import net.corda.core.contracts.ContractState
import net.corda.core.identity.AbstractParty

interface INonfungibleTokenState<T : EmbeddableToken> : ContractState, ITokenState {

    /** Token definition for this token state, wrapped in an [IssuedToken]. */
    val token: IssuedToken<T>

    /** The current [owner] of this token. */
    val owner: AbstractParty

    /**
     * Helper function for changing the ownership of this token. Copies the underlying data structure, updating only
     * the [owner] field, and provides a ready-made [MoveTokenCommand] for ease of use in a [TransactionBuilder].
     * */
    fun withNewOwner(newOwner: AbstractParty): Pair<MoveTokenCommand<T>, INonfungibleTokenState<T>>

    /**
     * The [participants] list for a [INonfungibleTokenState] is always the current [owner].
     *
     * TODO: Consider if this list should include the issuer of the token in addition to the current owner.
     */
    override val participants: List<AbstractParty> get() = listOf(owner)
}