package com.r3.corda.sdk.token.contracts.types

import net.corda.core.contracts.TokenizableAssetInfo

/**
 * Overarching interface for all things token. All tokens implement this interface. Just a quick level-set on
 * terminology here. [Token] refers to a "type of thing" as opposed to the vehicle which is used to assign units of a
 * token to a particular owner. For that we use the [OwnedToken] state for assigning non-fungible tokens to an owner and
 * the [OwnedTokenAmount] state for assigning amounts of some fungible token to an owner. Currently, this interface is
 * just a marker and it will probably stay that way.
 *
 * TODO: Consider moving tokenIdentifier and tokenClass out of [AbstractOwnedToken] into the token definition.
 * This way,  * the token is responsible for storing itself in the schema rather than needing to decide within the
 * Contract. Also opens up the option of using any other thing as a token, and we can remove [EmbeddableToken].
 */
interface Token : TokenizableAssetInfo {
    val tokenIdentifier : String
    val tokenClass : String
}