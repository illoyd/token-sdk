package com.r3.corda.sdk.token.contracts.states

import com.r3.corda.sdk.token.contracts.FungibleTokenContract
import com.r3.corda.sdk.token.contracts.IFungibleTokenState
import com.r3.corda.sdk.token.contracts.commands.MoveTokenCommand
import com.r3.corda.sdk.token.contracts.schemas.OwnedTokenAmountSchemaV1
import com.r3.corda.sdk.token.contracts.schemas.PersistentOwnedTokenAmount
import com.r3.corda.sdk.token.contracts.types.EmbeddableToken
import com.r3.corda.sdk.token.contracts.types.IssuedToken
import net.corda.core.contracts.Amount
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.CommandAndState
import net.corda.core.contracts.FungibleState
import net.corda.core.crypto.toStringShort
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState

/**
 * This class is for handling the issuer / owner relationship for "non-fungible" token types. If the [EmbeddableToken]
 * is a [TokenPointer], then it allows the token can evolve independently of who owns it. This state object implements
 * [FungibleState] as the expectation is that it contains amounts of a token type which can be split and merged.
 *
 * All [EmbeddableToken]s are wrapped with an [IssuedToken] class to add the issuer party. This is necessary so that the
 * [NonfungibleTokenState] represents a contract or agreement between an issuer and an owner. In effect, this token conveys a right
 * for the owner to make a claim on the issuer for whatever the [EmbeddableToken] represents.
 *
 * The class is open, so it can be extended to add new functionality, like a whitelisted token, for example.
 */
@BelongsToContract(FungibleTokenContract::class)
open class FungibleTokenState<T : EmbeddableToken>(
        override val token: IssuedToken<T>,
        override val quantity: Long,
        override val owner: AbstractParty
) : IFungibleTokenState<T>, QueryableState {

    constructor(amount: Amount<IssuedToken<T>>, owner: AbstractParty) : this(amount.token, amount.quantity, owner)

    override fun withNewOwner(newOwner: AbstractParty): Pair<MoveTokenCommand<T>, IFungibleTokenState<T>> {
        return Pair(MoveTokenCommand(token), FungibleTokenState(token, quantity, newOwner))
    }

    override fun toString(): String = "$amount owned by $ownerString"

    override fun generateMappedObject(schema: MappedSchema): PersistentState = when (schema) {
        is OwnedTokenAmountSchemaV1 -> PersistentOwnedTokenAmount(
                issuer = amount.token.issuer,
                owner = owner,
                amount = amount.quantity,
                tokenClass = amount.token.product.tokenClass,
                tokenIdentifier = amount.token.product.tokenIdentifier
        )
        else -> throw IllegalArgumentException("Unrecognised schema $schema")
    }

    override fun supportedSchemas() = listOf(OwnedTokenAmountSchemaV1)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FungibleTokenState<*>) return false

        if (amount != other.amount) return false
        if (owner != other.owner) return false

        return true
    }

    override fun hashCode(): Int {
        var result = amount.hashCode()
        result = 31 * result + owner.hashCode()
        return result
    }

    /** Converts [owner] into a more friendly string, e.g. shortens the public key for [AnonymousParty]s. */
    // TODO: Is AbstractOwnedToken#ownerString needed?
    private val ownerString
        get() = (owner as? Party)?.name?.organisation
                ?: owner.owningKey.toStringShort().substring(0, 16)
}

