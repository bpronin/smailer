package com.bopr.android.smailer;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;

/**
 * Used in tests to collect method invocations.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
// TODO: 21.02.2020 get rid of it
class MethodInvocationsCollector implements Answer {

    private final List<Object[]> invocationsArguments = new ArrayList<>();

    @Override
    public Object answer(InvocationOnMock invocation) {
        invocationsArguments.add(invocation.getArguments());
        return null;
    }

    Object[] getArguments(int invocationIndex) {
        return invocationsArguments.get(invocationIndex);
    }

    @SuppressWarnings("unchecked")
    <T> T getArgument(int invocationIndex, int argumentIndex) {
        return (T) getArguments(invocationIndex)[argumentIndex];
    }

    boolean isEmpty() {
        return invocationsArguments.isEmpty();
    }

    int count() {
        return invocationsArguments.size();
    }

    void reset() {
        invocationsArguments.clear();
    }
}
