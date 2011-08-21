package org.jmock.lib;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Arrays;

import org.jmock.api.Imposteriser;
import org.jmock.api.Invocation;
import org.jmock.api.Invokable;
import org.jmock.internal.CombineClassLoader;
import org.jmock.internal.SearchingClassLoader;

/**
 * An {@link org.jmock.api.Imposteriser} that uses the
 * {@link java.lang.reflect.Proxy} class of the Java Reflection API.
 *
 * @author npryce
 */
public class JavaReflectionImposteriser extends AbstractImposteriser {
    public static final Imposteriser INSTANCE = new JavaReflectionImposteriser();

    public boolean canImposterise(Class<?> type) {
        return type.isInterface();
    }

    @SuppressWarnings("unchecked")
    public <T> T imposterise(final Invokable mockObject, Class<T> mockedType, Class<?>... ancilliaryTypes) {
        final Class<?>[] proxiedClasses = prepend(mockedType, ancilliaryTypes);

        final ClassLoader classLoader = findCommonClassLoader(mockedType, ancilliaryTypes);

        return (T) Proxy.newProxyInstance(classLoader, proxiedClasses, new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                return mockObject.invoke(new Invocation(proxy, method, args));
            }
        });
    }

}
