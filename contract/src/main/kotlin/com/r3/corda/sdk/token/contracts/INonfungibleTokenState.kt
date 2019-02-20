package com.r3.corda.sdk.token.contracts

import com.r3.corda.sdk.token.contracts.types.EmbeddableToken
import net.corda.core.contracts.ContractState
import net.corda.core.identity.AbstractParty

interface INonfungibleTokenState<T : EmbeddableToken> : ContractState, ITokenState<T> {

    /**
     * The [participants] list for a [INonfungibleTokenState] is always the current [owner].
     *
     * TODO: Consider if this list should include the issuer of the token in addition to the current owner.
     */
    override val participants: List<AbstractParty> get() = listOf(owner)
}