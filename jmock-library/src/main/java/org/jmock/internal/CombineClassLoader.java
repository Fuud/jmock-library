package org.jmock.internal;

public class CombineClassLoader extends ClassLoader {
    private final Class<?>[] knownClasses;

    public CombineClassLoader(Class<?>... knownClasses) {
        super(null);
        this.knownClasses = knownClasses;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        for (Class<?> aClass : knownClasses) {
            if (aClass.getName().equals(name)) {
                return aClass;
            }
        }

        for (Class<?> aClass : knownClasses) {
            ClassLoader classLoader = aClass.getClassLoader();
            if (classLoader != null) {
                try {
                    return classLoader.loadClass(name);
                } catch (ClassNotFoundException ignore) {
                }
            }
        }

        return ClassLoader.getSystemClassLoader().loadClass(name);
    }
}
