package com.r3.corda.sdk.token.contracts

import com.r3.corda.sdk.token.contracts.types.EmbeddableToken
import com.r3.corda.sdk.token.contracts.types.IssuedToken
import net.corda.core.contracts.ContractState
import net.corda.core.crypto.toStringShort
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party

interface ITokenState<T : EmbeddableToken> : ContractState {

    val token : IssuedToken<T>

    /** The current [owner] of this token. */
    val owner: AbstractParty

    /** Converts [owner] into a more friendly string, e.g. shortens the public key for [AnonymousParty]s. */
    // TODO: Is AbstractOwnedToken#ownerString needed?
    val ownerString: String get() = (owner as? Party)?.name?.organisation
            ?: owner.owningKey.toStringShort().substring(0, 16)
}