/*******************************************************************************
 * Copyright (c) 2009 Martin Lippert and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html.
 * 
 * Contributors:
 *     Martin Lippert - initial implementation
 ******************************************************************************/

package org.eclipse.osgi.aspectj.tests;

import java.net.URL;
import java.util.Hashtable;
import java.util.List;

import junit.framework.TestCase;

import org.aspectj.weaver.loadtime.definition.Definition;
import org.easymock.EasyMock;
import org.eclipse.equinox.weaving.aspectj.AspectAdmin;
import org.eclipse.equinox.weaving.aspectj.loadtime.AspectAdminImpl;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;

/**
 * @author Martin Lippert
 */
public class AspectAdminImplTest extends TestCase {

    private AspectAdminImpl aspectRegistry;

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        aspectRegistry = new AspectAdminImpl();
    }

    public void testDefaultAspectDefinitionLocation() {
        Bundle bundle = EasyMock.createMock(Bundle.class);
        EasyMock.expect(bundle.getHeaders()).andStubReturn(new Hashtable());

        EasyMock.replay(bundle);

        String definitionLocation = aspectRegistry
                .getDefinitionLocation(bundle);
        assertEquals(AspectAdmin.DEFAULT_AOP_CONTEXT_LOCATION,
                definitionLocation);

        EasyMock.verify(bundle);
    }

    public void testSpecializedAspectDefinitionLocation() {
        String location = " specialLocationForAspects ";

        Bundle bundle = EasyMock.createMock(Bundle.class);
        Hashtable headers = new Hashtable();
        headers.put(AspectAdmin.AOP_CONTEXT_LOCATION_HEADER, location);
        EasyMock.expect(bundle.getHeaders()).andStubReturn(headers);

        EasyMock.replay(bundle);

        String definitionLocation = aspectRegistry
                .getDefinitionLocation(bundle);
        assertEquals("specialLocationForAspects", definitionLocation);

        EasyMock.verify(bundle);
    }

    public void testResolveUnresolveBundle() {
        Bundle bundleWithDef = EasyMock.createMock(Bundle.class);
        Bundle bundleWithoutDef = EasyMock.createMock(Bundle.class);

        URL testAopDefFile = this.getClass().getResource("test-aop.xml");
        EasyMock.expect(bundleWithDef.getHeaders()).andStubReturn(
                new Hashtable());
        EasyMock.expect(
                bundleWithDef
                        .getEntry(AspectAdmin.DEFAULT_AOP_CONTEXT_LOCATION))
                .andReturn(testAopDefFile);

        EasyMock.expect(bundleWithoutDef.getHeaders()).andStubReturn(
                new Hashtable());
        EasyMock.expect(
                bundleWithoutDef
                        .getEntry(AspectAdmin.DEFAULT_AOP_CONTEXT_LOCATION))
                .andReturn(null);

        // step 1: resolve bundles
        EasyMock.replay(bundleWithDef, bundleWithoutDef);
        aspectRegistry.bundleResolved(bundleWithDef);
        aspectRegistry.bundleResolved(bundleWithoutDef);
        EasyMock.verify(bundleWithDef, bundleWithoutDef);

        Definition definition = aspectRegistry
                .getAspectDefinition(bundleWithDef);
        assertNotNull(definition);
        List<?> aspectClassNames = definition.getAspectClassNames();
        assertNotNull(aspectClassNames);
        assertEquals(1, aspectClassNames.size());
        assertEquals("org.eclipse.equinox.weaving.tests.TestAspectType",
                aspectClassNames.get(0));

        definition = aspectRegistry.getAspectDefinition(bundleWithoutDef);
        assertNull(definition);

        // step 2: unresolve bundles
        aspectRegistry.bundleUnresolved(bundleWithDef);
        aspectRegistry.bundleUnresolved(bundleWithoutDef);

        assertNull(aspectRegistry.getAspectDefinition(bundleWithDef));
        assertNull(aspectRegistry.getAspectDefinition(bundleWithoutDef));
    }

    public void testNonExportedAspectsInAopXml() {
        Bundle bundle = EasyMock.createMock(Bundle.class);

        URL testAopDefFile = this.getClass().getResource("test-aop.xml");
        Hashtable<Object, Object> headers = new Hashtable<Object, Object>();
        EasyMock.expect(bundle.getHeaders()).andStubReturn(headers);
        EasyMock.expect(
                bundle.getEntry(AspectAdmin.DEFAULT_AOP_CONTEXT_LOCATION))
                .andReturn(testAopDefFile);

        EasyMock.replay(bundle);
        aspectRegistry.bundleResolved(bundle);
        EasyMock.verify(bundle);

        Definition definition = aspectRegistry
                .getExportedAspectDefinitions(bundle);
        assertNull(definition);
    }

    public void testExportedAspectsInAopXml() {
        Bundle bundle = EasyMock.createMock(Bundle.class);

        URL testAopDefFile = this.getClass().getResource("test-aop.xml");
        Hashtable<Object, Object> headers = new Hashtable<Object, Object>();
        headers.put(Constants.EXPORT_PACKAGE,
                "org.eclipse.equinox.weaving.tests");
        EasyMock.expect(bundle.getHeaders()).andStubReturn(headers);
        EasyMock.expect(
                bundle.getEntry(AspectAdmin.DEFAULT_AOP_CONTEXT_LOCATION))
                .andReturn(testAopDefFile);

        EasyMock.replay(bundle);
        aspectRegistry.bundleResolved(bundle);
        EasyMock.verify(bundle);

        Definition definition = aspectRegistry
                .getExportedAspectDefinitions(bundle);
        List<?> aspectClassNames = definition.getAspectClassNames();
        assertEquals(1, aspectClassNames.size());
        assertEquals("org.eclipse.equinox.weaving.tests.TestAspectType",
                aspectClassNames.get(0));
        assertEquals(AspectAdmin.OPT_OUT_POLICY, aspectRegistry
                .getAspectPolicy(bundle, "org.eclipse.equinox.weaving.tests"));
    }

    public void testExportedAspectsInManifest() {
        Bundle bundle = EasyMock.createMock(Bundle.class);

        Hashtable<Object, Object> headers = new Hashtable<Object, Object>();
        StringBuilder export = new StringBuilder();
        export.append("org.eclipse.equinox.weaving.tests;");
        export.append("aspect-policy:=opt-in;");
        export.append("aspects=\"TestAspect,OtherAspect\", ");

        export.append("org.eclipse.equinox.weaving.tests2;");
        export.append("aspect-policy:=opt-out;");
        export.append("aspects=\"SecondAspect,OtherSecondAspect\"");
        headers.put(Constants.EXPORT_PACKAGE, export.toString());

        EasyMock.expect(bundle.getHeaders()).andStubReturn(headers);
        EasyMock.expect(
                bundle.getEntry(AspectAdmin.DEFAULT_AOP_CONTEXT_LOCATION))
                .andReturn(null);

        EasyMock.replay(bundle);
        aspectRegistry.bundleResolved(bundle);
        EasyMock.verify(bundle);

        Definition definition = aspectRegistry
                .getExportedAspectDefinitions(bundle);
        List<?> aspectClassNames = definition.getAspectClassNames();
        assertEquals(4, aspectClassNames.size());
        assertEquals("org.eclipse.equinox.weaving.tests.TestAspect",
                aspectClassNames.get(0));
        assertEquals("org.eclipse.equinox.weaving.tests.OtherAspect",
                aspectClassNames.get(1));
        assertEquals("org.eclipse.equinox.weaving.tests2.SecondAspect",
                aspectClassNames.get(2));
        assertEquals("org.eclipse.equinox.weaving.tests2.OtherSecondAspect",
                aspectClassNames.get(3));
        assertEquals(AspectAdmin.OPT_IN_POLICY, aspectRegistry.getAspectPolicy(
                bundle, "org.eclipse.equinox.weaving.tests"));
        assertEquals(AspectAdmin.OPT_OUT_POLICY, aspectRegistry
                .getAspectPolicy(bundle, "org.eclipse.equinox.weaving.tests2"));
    }
}
