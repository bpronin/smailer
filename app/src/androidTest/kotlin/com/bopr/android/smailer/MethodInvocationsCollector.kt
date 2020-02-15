package com.bopr.android.smailer

import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import java.util.*

/**
 * Used in tests to collect method invocations.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
internal class MethodInvocationsCollector : Answer<Any?> {

    private val invocationsArguments: MutableList<Array<Any>> = ArrayList()

    override fun answer(invocation: InvocationOnMock): Any? {
        invocationsArguments.add(invocation.arguments)
        return null
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getArgument(invocationIndex: Int, argumentIndex: Int): T {
        return invocationsArguments[invocationIndex][argumentIndex] as T
    }

    val isEmpty: Boolean
        get() = invocationsArguments.isEmpty()

    fun count(): Int {
        return invocationsArguments.size
    }

    fun reset() {
        invocationsArguments.clear()
    }
}