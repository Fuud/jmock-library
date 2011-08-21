package org.jmock.test.acceptance;


import org.jmock.api.Imposteriser;
import org.jmock.api.Invocation;
import org.jmock.lib.JavaReflectionImposteriser;
import org.jmock.lib.action.CustomAction;
import org.jmock.lib.action.VoidAction;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import sun.misc.IOUtils;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

@RunWith(Parameterized.class)
public class ImposterisersAcceptanceTests {
    private final Class iFooClass;
    private final Class iBarClass;
    private final Class fooClass;
    private final Imposteriser imposteriser;

    @Parameterized.Parameters
    public static Collection data() {
        Object[][] data = new Object[][]{
                {new JavaReflectionImposteriser()},
                {ClassImposteriser.INSTANCE}};
        return Arrays.asList(data);
    }

    public ImposterisersAcceptanceTests(Imposteriser imposteriser) throws Exception {
        this.imposteriser = imposteriser;

        final IncompatibleClassLoader iFooCL = new IncompatibleClassLoader();
        final IncompatibleClassLoader iBarCL = new IncompatibleClassLoader();
        final IncompatibleClassLoader fooCL = new IncompatibleClassLoader();

        iFooClass = iFooCL.findClass(IFoo.class.getName());
        iBarClass = iBarCL.findClass(IBar.class.getName());
        fooClass = fooCL.findClass(Foo.class.getName());

        // check that IncompatibleClassLoader works correct

        assertNotEquals(iFooClass, iBarCL.loadClass(IFoo.class.getName()));
        assertNotEquals(iFooClass, fooCL.loadClass(IFoo.class.getName()));

        assertNotEquals(iBarClass, iFooCL.loadClass(IBar.class.getName()));
        assertNotEquals(iBarClass, fooCL.loadClass(IBar.class.getName()));

        assertNotEquals(fooClass, iBarCL.loadClass(Foo.class.getName()));
        assertNotEquals(fooClass, iFooCL.loadClass(Foo.class.getName()));
    }


    @Test
    public void canImposteriseInterfaces() throws Exception {
        assertTrue("Imposteriser should can imposterice interfaces", imposteriser.canImposterise(IFoo.class));
        final IFoo iFoo = imposteriser.imposterise(new VoidAction(), IFoo.class, IBar.class);
        assertTrue(iFoo instanceof IBar);
    }

    @Test
    public void canImposteriseClasses() throws Exception {
        if (imposteriser.canImposterise(Foo.class)) {
            final Foo foo = imposteriser.imposterise(new VoidAction(), Foo.class, IFoo.class, IBar.class);
            assertTrue(foo instanceof IFoo);
            assertTrue(foo instanceof IBar);
        } else {
            System.out.println("Warning: Imposteriser " + imposteriser + " can not imposterise interfaces");
        }
    }

    @Test
    public void canImposteriseInterfaces_fromDifferentClassLoader() throws Exception {
        assertTrue("Imposteriser should can imposterice interfaces", imposteriser.canImposterise(iFooClass));
        final Object iFoo = imposteriser.imposterise(new VoidAction(), iFooClass, iBarClass);
        assertTrue(iFooClass.isInstance(iFoo));
        assertTrue(iBarClass.isInstance(iFoo));
    }

    @Test
    public void canImposteriseInterfaces_packagePrivate() throws Exception {
        assertTrue("Imposteriser should can imposterice interfaces", imposteriser.canImposterise(IPackagePrivate.class));
        final IPackagePrivate iPackagePrivate = imposteriser.imposterise(new VoidAction(), IPackagePrivate.class, IBar.class);
        assertTrue(iPackagePrivate instanceof IBar);
    }

    @Test
    public void canImposteriseBootstrapClasses() throws Exception {
        assertTrue("Imposteriser should can imposterice interfaces", imposteriser.canImposterise(Collection.class));
        final Collection collection = imposteriser.imposterise(new VoidAction(), Collection.class, iBarClass);
        assertTrue(iBarClass.isInstance(collection));
    }

    @Test
    public void delegateInvokationsToMockObject() throws Exception {
        final CustomAction increment = new CustomAction("increment") {
            public Object invoke(Invocation invocation) throws Throwable {
                final Integer arg1 = (Integer) invocation.getParameter(0);
                return arg1+1;
            }
        };

        final IFoo iFoo = imposteriser.imposterise(increment, IFoo.class, IBar.class);

        assertEquals(43, iFoo.doSomething(42));
    }

    // -----------------------------------------------------------------------------------------------------------------


    public static interface IFoo {
        int doSomething(int arg);
    }

    static interface IPackagePrivate {
        int doSomething(int arg);
    }

    public static interface IBar {
        void doSomethingElse();
    }

    public static class Foo {
        void doSomething() {
        }
    }

    class IncompatibleClassLoader extends ClassLoader {
        private ClassLoader delegate = ImposterisersAcceptanceTests.class.getClassLoader();

        IncompatibleClassLoader() {
            super(null);
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            if (name.contains("ImposterisersAcceptanceTests")) {
                try {
                    final InputStream inputStream = delegate.getResourceAsStream(name.replace('.', '/') + ".class");
                    final byte[] classBytes = IOUtils.readFully(inputStream, -1, true);
                    return super.defineClass(name, classBytes, 0, classBytes.length);
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            } else {
                return delegate.loadClass(name);
            }
        }
    }

    private void assertNotEquals(Object object1, Object object2) {
        assertFalse(object1.equals(object2));
    }
}
