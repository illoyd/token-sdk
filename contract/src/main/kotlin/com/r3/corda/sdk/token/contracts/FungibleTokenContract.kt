package com.r3.corda.sdk.token.contracts

import com.r3.corda.sdk.token.contracts.behaviours.IssuableFungibleTokenContract
import com.r3.corda.sdk.token.contracts.behaviours.MoveableFungibleTokenContract
import com.r3.corda.sdk.token.contracts.behaviours.RedeemableFungibleTokenContract
import com.r3.corda.sdk.token.contracts.commands.TokenCommand
import com.r3.corda.sdk.token.contracts.states.AbstractOwnedToken
import com.r3.corda.sdk.token.contracts.states.FungibleTokenState
import com.r3.corda.sdk.token.contracts.types.EmbeddableToken
import com.r3.corda.sdk.token.contracts.types.IssuedToken
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.CommandWithParties
import net.corda.core.contracts.select
import net.corda.core.transactions.LedgerTransaction
import net.corda.core.transactions.LedgerTransaction.InOutGroup
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.functions
import kotlin.reflect.full.memberFunctions

/**
 * This is the [FungibleTokenState] contract. It is likely to be present in MANY transactions. The [FungibleTokenState]
 * state is a "lowest common denominator" state in that its contract does not reference any other state types, only the
 * [FungibleTokenState]. However, the [FungibleTokenState] state can and will be referenced by many other contracts, for
 * example, the obligation contract.
 *
 * The [FungibleTokenState] contract sub-classes the [AbstractOwnedToken] contract which contains the "verify" method.
 * To add functionality to this contract, developers should:
 * 1. Create their own commands which implement the [TokenCommand] interface.
 * 2. override the [AbstractOwnedTokenContract.dispatchOnCommand] method to add support for the new command, remembering
 *    to call the super method to handle the existing commands.
 * 3. Add a method to handle the new command in the new sub-class contract.
 */
open class FungibleTokenContract<T : EmbeddableToken> :
        IFungibleTokenContract<T>,
        MoveableFungibleTokenContract<T>,
        IssuableFungibleTokenContract<T>,
        RedeemableFungibleTokenContract<T> {

    companion object {
        val ID = this::class.java.enclosingClass.canonicalName
        val contractId = this::class.java.enclosingClass.canonicalName
    }

    private fun groupStates(tx: LedgerTransaction): List<InOutGroup<IFungibleTokenState<T>, IssuedToken<T>>> {
        return tx.groupStates { state -> state.token }
    }

    override fun verify(tx: LedgerTransaction) {
        // Group owned token amounts by token type. We need to do this because tokens of different types need to be
        // verified separately. This works for the same token type with different issuers, or different token types
        // altogether. The grouping function returns a list containing groups of input and output states grouped by
        // token type. The type is specified explicitly to aid understanding.
        val groups: List<LedgerTransaction.InOutGroup<IFungibleTokenState<T>, IssuedToken<T>>> = groupStates(tx)

        // A list of only the commands which implement TokenCommand.
        val tokenCommands = tx.commands.select<TokenCommand<T>>()
        require(tokenCommands.isNotEmpty()) { "There must be at least one owned token command this transaction." }

        // Find all verify method annotations for this contract
        val verifyMap = mutableMapOf<KClass<out TokenCommand<*>>, MutableList<KFunction<*>>>()
        this::class.functions.forEach { func ->
            func.annotations.forEach { annotation ->
                if (annotation is VerifyTokenCommandMethod) {
                    if (verifyMap.containsKey(annotation.value).not()) {
                        verifyMap[annotation.value] = mutableListOf<KFunction<*>>()
                    }
                    verifyMap[annotation.value]!!.add(func)
                }
            }
        }

        // As inputs and outputs are just "bags of states" and the InOutGroups do not contain commands, we must match
        // the TokenCommand to each InOutGroup. There should be at least a single command for each group. If there
        // isn't then we don't know what to do for each group. For token moves it might be the case that there is more
        // than one command. However, for issuances and redemptions we would expect to see only one command.
        groups.forEach { group ->
            // Select all commands for the given token (grouping key)
            val commandsForToken: List<CommandWithParties<TokenCommand<T>>> = tokenCommands.filter { it.value.token == group.groupingKey }

            // Evaluate commands
            commandsForToken.map { it.value.javaClass.name }.toSet().apply {
                // Must have a command for this group
                require(isNotEmpty()) {
                    "There is a token group with no assigned command!"
                }

                // Cannot mix command types for the same token
                require(size == 1) {
                    "There must be exactly one TokenCommand type per group! For example: You cannot map an Issue AND a Move command to one group of tokens in a transaction."
                }
            }

            commandsForToken.forEach {

                if (verifyMap[it.value::class]?.isNotEmpty() == true) {
                    verifyMap[it.value::class]?.forEach { verifyFunction ->
                        try {
                            verifyFunction.call(this, it.value, group.inputs, group.outputs, tx)
                        } catch (e: InvocationTargetException) {
                            throw e.targetException
                        }
                    }
                } else {
                    verify(it.value, group.inputs, group.outputs, tx)
                }
            }
        }
    }

    override fun verify(command: TokenCommand<T>, inputs: List<IFungibleTokenState<T>>, outputs: List<IFungibleTokenState<T>>, tx: LedgerTransaction) {
        throw NotImplementedError("Verify with TokenCommand, given ${command.javaClass.name}")
    }

    override fun verify(command: CommandData, inputs: List<IFungibleTokenState<T>>, outputs: List<IFungibleTokenState<T>>, tx: LedgerTransaction) {
        throw NotImplementedError("Verify with CommandData, given ${command.javaClass.name}")
    }
}

