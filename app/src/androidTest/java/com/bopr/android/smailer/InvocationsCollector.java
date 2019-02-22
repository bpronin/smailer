package com.bopr.android.smailer;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;

/**
 * Used in tests to collect method invocation data.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
class InvocationsCollector implements Answer {

    private final List<Object[]> arguments = new ArrayList<>();

    @Override
    public Object answer(InvocationOnMock invocation) throws Throwable {
        arguments.add(invocation.getArguments());
        return null;
    }

    public Object[] invocation(int index) {
        return arguments.get(index);
    }

    public boolean isEmpty() {
        return arguments.isEmpty();
    }

    public void clear() {
        arguments.clear();
    }

    public int size() {
        return arguments.size();
    }
}
