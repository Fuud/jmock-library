package org.jmock.lib;

import junit.framework.TestCase;

import org.jmock.api.MockObjectNamingScheme;
import org.jmock.lib.RetroNamingScheme;
import org.jmock.support.DummyInterface;

public class RetroNamingSchemeTests extends TestCase {
    public void testNamesMocksByLowerCasingFirstCharacterOfTypeName() {
        MockObjectNamingScheme namingScheme = RetroNamingScheme.INSTANCE;
        
        assertEquals("mockRunnable", namingScheme.defaultNameFor(Runnable.class));
        assertEquals("mockDummyInterface", namingScheme.defaultNameFor(DummyInterface.class));
    }
}
