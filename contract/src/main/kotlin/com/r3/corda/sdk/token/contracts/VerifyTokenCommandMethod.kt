package com.r3.corda.sdk.token.contracts

import com.r3.corda.sdk.token.contracts.commands.TokenCommand
import kotlin.reflect.KClass

@Target(AnnotationTarget.FUNCTION)
@Repeatable
@MustBeDocumented
annotation class VerifyTokenCommandMethod(val value: KClass<out TokenCommand<*>>)