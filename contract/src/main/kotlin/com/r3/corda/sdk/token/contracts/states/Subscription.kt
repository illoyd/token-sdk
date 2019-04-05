package com.r3.corda.sdk.token.contracts.states

import com.r3.corda.sdk.token.contracts.SubscriptionContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.LinearPointer
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party

@BelongsToContract(SubscriptionContract::class)
data class Subscription(
        val evolvableTokenType: LinearPointer<EvolvableTokenType>,
        val subscriber: Party,
        val maintainer: Party
) : ContractState {

    override val participants: List<AbstractParty> get() = listOf(subscriber, maintainer)

}