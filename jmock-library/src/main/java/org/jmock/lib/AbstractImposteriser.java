package org.jmock.lib;

import org.jmock.api.Imposteriser;
import org.jmock.internal.CombineClassLoader;

import java.lang.reflect.Modifier;
import java.util.Arrays;

public abstract class AbstractImposteriser  implements Imposteriser {
    protected <T> ClassLoader findCommonClassLoader(Class<T> mockedType, Class<?>[] ancilliaryTypes) {
        final Class<?>[] proxiedClasses = prepend(mockedType, ancilliaryTypes);
        for (Class<?> ancilliaryType : ancilliaryTypes) {
            if (!Modifier.isPublic(ancilliaryType.getModifiers())) {
                throw new IllegalArgumentException("ancilliaryTypes should be public, but " + ancilliaryType + " is not.");
            }
        }

        boolean canUseClassLoaderFromMockedType = true;
        for (Class<?> proxiedClass : ancilliaryTypes) {
            try {
                canUseClassLoaderFromMockedType &= (getClassLoader(mockedType).loadClass(proxiedClass.getName()).equals(proxiedClass));
            } catch (ClassNotFoundException e) {
                canUseClassLoaderFromMockedType = false;
                break;
            }
        }

        if (!canUseClassLoaderFromMockedType && !Modifier.isPublic(mockedType.getModifiers())) {
            throw new IllegalArgumentException("Can not imposterise " + mockedType + " because it is not public and clases " + Arrays.toString(ancilliaryTypes) + " can not be directed accessed from it's classloader");
        }

        return canUseClassLoaderFromMockedType ? getClassLoader(mockedType) : new CombineClassLoader(proxiedClasses);
    }

    private <T> ClassLoader getClassLoader(Class<T> mockedType) {
        final ClassLoader classLoader = mockedType.getClassLoader();
        return classLoader == null ? ClassLoader.getSystemClassLoader() : classLoader;
    }

    protected Class<?>[] prepend(Class<?> first, Class<?>... rest) {
        Class<?>[] proxiedClasses = new Class<?>[rest.length + 1];

        proxiedClasses[0] = first;
        System.arraycopy(rest, 0, proxiedClasses, 1, rest.length);

        return proxiedClasses;
    }
}
