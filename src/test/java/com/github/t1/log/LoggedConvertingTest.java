package com.github.t1.log;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import lombok.experimental.Value;

import org.junit.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.MDC;

public class LoggedConvertingTest extends AbstractLoggedTest {
    @Value
    private static class Pojo {
        String one, two;
    }

    @Before
    @Override
    public void setupConverters() {
        when(converters.convert(isA(Pojo.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return ((Pojo) invocation.getArguments()[0]).one;
            }
        });
    }

    @Test
    public void shouldConvertParameter() throws Exception {
        class Container {
            @Logged
            public void foo(Pojo pojo) {}
        }
        whenMethod(new Container(), "foo", new Pojo("a", "b"));

        interceptor.aroundInvoke(context);

        verify(logger).debug("foo {}", new Object[] { "a" });
    }

    @Test
    public void shouldConvertLogContextParameter() throws Exception {
        class Container {
            @Logged
            public void foo(@LogContext(value = "foobar") Pojo pojo) {}
        }
        whenMethod(new Container(), "foo", new Pojo("a", "b"));
        StoreMdcAnswer mdc = new StoreMdcAnswer("foobar");
        when(context.proceed()).then(mdc);
        MDC.put("foobar", "bar");

        interceptor.aroundInvoke(context);

        assertEquals("bar", MDC.get("foobar"));
        assertEquals("a", mdc.value);
    }

    // TODO test return value converter
    // TODO test log context variable converter
}
