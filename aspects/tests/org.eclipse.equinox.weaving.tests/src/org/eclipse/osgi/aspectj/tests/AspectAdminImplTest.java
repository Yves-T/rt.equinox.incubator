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
import org.aspectj.weaver.loadtime.definition.Definition.ConcreteAspect;
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
        EasyMock.expect(bundle.getHeaders()).andStubReturn(
                new Hashtable<Object, Object>());

        EasyMock.replay(bundle);

        String definitionLocation = aspectRegistry
                .getDefinitionLocation(bundle);
        assertEquals(AspectAdmin.AOP_CONTEXT_DEFAULT_LOCATION,
                definitionLocation);

        EasyMock.verify(bundle);
    }

    public void testSpecializedAspectDefinitionLocation() {
        String location = " specialLocationForAspects "; //$NON-NLS-1$

        Bundle bundle = EasyMock.createMock(Bundle.class);
        Hashtable<Object, Object> headers = new Hashtable<Object, Object>();
        headers.put(AspectAdmin.AOP_CONTEXT_LOCATION_HEADER, location);
        EasyMock.expect(bundle.getHeaders()).andStubReturn(headers);

        EasyMock.replay(bundle);

        String definitionLocation = aspectRegistry
                .getDefinitionLocation(bundle);
        assertEquals("specialLocationForAspects", definitionLocation); //$NON-NLS-1$

        EasyMock.verify(bundle);
    }

    public void testResolveUnresolveBundle() {
        Bundle bundleWithDef = EasyMock.createMock(Bundle.class);
        Bundle bundleWithoutDef = EasyMock.createMock(Bundle.class);

        URL testAopDefFile = this.getClass().getResource("test-aop.xml"); //$NON-NLS-1$
        EasyMock.expect(bundleWithDef.getHeaders()).andStubReturn(
                new Hashtable<Object, Object>());
        EasyMock.expect(
                bundleWithDef
                        .getEntry(AspectAdmin.AOP_CONTEXT_DEFAULT_LOCATION))
                .andReturn(testAopDefFile);

        EasyMock.expect(bundleWithoutDef.getHeaders()).andStubReturn(
                new Hashtable<Object, Object>());
        EasyMock.expect(
                bundleWithoutDef
                        .getEntry(AspectAdmin.AOP_CONTEXT_DEFAULT_LOCATION))
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
        assertEquals("org.eclipse.equinox.weaving.tests.TestAspectType", //$NON-NLS-1$
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

        URL testAopDefFile = this.getClass().getResource("test-aop.xml"); //$NON-NLS-1$
        Hashtable<Object, Object> headers = new Hashtable<Object, Object>();
        EasyMock.expect(bundle.getHeaders()).andStubReturn(headers);
        EasyMock.expect(
                bundle.getEntry(AspectAdmin.AOP_CONTEXT_DEFAULT_LOCATION))
                .andReturn(testAopDefFile);

        EasyMock.replay(bundle);
        aspectRegistry.bundleResolved(bundle);
        EasyMock.verify(bundle);

        Definition definition = aspectRegistry
                .getExportedAspectDefinitions(bundle);
        assertNull(definition);

        Definition resolvedRequired = aspectRegistry.resolveRequiredBundle(
                bundle, AspectAdmin.ASPECT_APPLY_POLICY_NOT_DEFINED);
        assertNull(resolvedRequired);

        Definition resolvedImports = aspectRegistry.resolveImportedPackage(
                bundle, "org.eclipse.equinox.weaving.tests", //$NON-NLS-1$
                AspectAdmin.ASPECT_APPLY_POLICY_NOT_DEFINED);
        assertNull(resolvedImports);
    }

    public void testNoAspectsButOptionsInAopXml() {
        Bundle bundle = EasyMock.createMock(Bundle.class);

        URL testAopDefFile = this.getClass().getResource(
                "test-aop-with-options.xml"); //$NON-NLS-1$
        Hashtable<Object, Object> headers = new Hashtable<Object, Object>();
        EasyMock.expect(bundle.getHeaders()).andStubReturn(headers);
        EasyMock.expect(
                bundle.getEntry(AspectAdmin.AOP_CONTEXT_DEFAULT_LOCATION))
                .andReturn(testAopDefFile);

        EasyMock.replay(bundle);
        aspectRegistry.bundleResolved(bundle);
        EasyMock.verify(bundle);

        Definition definition = aspectRegistry
                .getExportedAspectDefinitions(bundle);
        assertNotNull(definition);
        assertEquals(0, definition.getAspectClassNames().size());
        assertEquals("here are the options ", definition.getWeaverOptions()); //$NON-NLS-1$

        Definition resolvedRequires = aspectRegistry.resolveRequiredBundle(
                bundle, AspectAdmin.ASPECT_APPLY_POLICY_NOT_DEFINED);
        assertNotNull(resolvedRequires);
        assertEquals(0, resolvedRequires.getAspectClassNames().size());
        assertEquals("here are the options ", resolvedRequires //$NON-NLS-1$
                .getWeaverOptions());

        Definition resolvedImports = aspectRegistry.resolveImportedPackage(
                bundle, "org.eclipse.equinox.weaving.tests", //$NON-NLS-1$
                AspectAdmin.ASPECT_APPLY_POLICY_NOT_DEFINED);
        assertNotNull(resolvedImports);
        assertEquals(0, resolvedImports.getAspectClassNames().size());
        assertEquals("here are the options ", resolvedImports //$NON-NLS-1$
                .getWeaverOptions());
    }

    public void testNonExportedAspectsButOptionsInAopXml() {
        Bundle bundle = EasyMock.createMock(Bundle.class);

        URL testAopDefFile = this.getClass().getResource(
                "test-aop-with-options-and-aspects.xml"); //$NON-NLS-1$
        Hashtable<Object, Object> headers = new Hashtable<Object, Object>();
        EasyMock.expect(bundle.getHeaders()).andStubReturn(headers);
        EasyMock.expect(
                bundle.getEntry(AspectAdmin.AOP_CONTEXT_DEFAULT_LOCATION))
                .andReturn(testAopDefFile);

        EasyMock.replay(bundle);
        aspectRegistry.bundleResolved(bundle);
        EasyMock.verify(bundle);

        Definition definition = aspectRegistry
                .getExportedAspectDefinitions(bundle);
        assertNotNull(definition);
        assertEquals(0, definition.getAspectClassNames().size());
        assertEquals("here are the options ", definition.getWeaverOptions()); //$NON-NLS-1$

        Definition resolvedRequires = aspectRegistry.resolveRequiredBundle(
                bundle, AspectAdmin.ASPECT_APPLY_POLICY_NOT_DEFINED);
        assertNotNull(resolvedRequires);
        assertEquals(0, resolvedRequires.getAspectClassNames().size());
        assertEquals("here are the options ", resolvedRequires //$NON-NLS-1$
                .getWeaverOptions());

        Definition resolvedImports = aspectRegistry.resolveImportedPackage(
                bundle, "org.eclipse.equinox.weaving.tests", //$NON-NLS-1$
                AspectAdmin.ASPECT_APPLY_POLICY_NOT_DEFINED);
        assertNotNull(resolvedImports);
        assertEquals(0, resolvedImports.getAspectClassNames().size());
        assertEquals("here are the options ", resolvedImports //$NON-NLS-1$
                .getWeaverOptions());
    }

    public void testExportedAspectsInAopXml() {
        Bundle bundle = EasyMock.createMock(Bundle.class);

        URL testAopDefFile = this.getClass().getResource("test-aop.xml"); //$NON-NLS-1$
        Hashtable<Object, Object> headers = new Hashtable<Object, Object>();
        headers.put(Constants.EXPORT_PACKAGE,
                "org.eclipse.equinox.weaving.tests"); //$NON-NLS-1$
        EasyMock.expect(bundle.getHeaders()).andStubReturn(headers);
        EasyMock.expect(
                bundle.getEntry(AspectAdmin.AOP_CONTEXT_DEFAULT_LOCATION))
                .andReturn(testAopDefFile);

        EasyMock.replay(bundle);
        aspectRegistry.bundleResolved(bundle);
        EasyMock.verify(bundle);

        Definition definition = aspectRegistry
                .getExportedAspectDefinitions(bundle);
        List<?> aspectClassNames = definition.getAspectClassNames();
        assertEquals(1, aspectClassNames.size());
        assertEquals("org.eclipse.equinox.weaving.tests.TestAspectType", //$NON-NLS-1$
                aspectClassNames.get(0));
        assertEquals(AspectAdmin.ASPECT_POLICY_NOT_DEFINED, aspectRegistry
                .getAspectPolicy(bundle, "org.eclipse.equinox.weaving.tests")); //$NON-NLS-1$

        Definition resolvedRequires = aspectRegistry.resolveRequiredBundle(
                bundle, AspectAdmin.ASPECT_APPLY_POLICY_NOT_DEFINED);
        assertNotNull(resolvedRequires);
        assertEquals(1, aspectClassNames.size());
        assertEquals("org.eclipse.equinox.weaving.tests.TestAspectType", //$NON-NLS-1$
                aspectClassNames.get(0));

        Definition resolvedImports = aspectRegistry.resolveImportedPackage(
                bundle, "org.eclipse.equinox.weaving.tests", //$NON-NLS-1$
                AspectAdmin.ASPECT_APPLY_POLICY_NOT_DEFINED);
        assertNotNull(resolvedImports);
        assertEquals(1, aspectClassNames.size());
        assertEquals("org.eclipse.equinox.weaving.tests.TestAspectType", //$NON-NLS-1$
                aspectClassNames.get(0));
    }

    public void testExportedAspectsAndOptionsInAopXml() {
        Bundle bundle = EasyMock.createMock(Bundle.class);

        URL testAopDefFile = this.getClass().getResource(
                "test-aop-with-options-and-aspects.xml"); //$NON-NLS-1$
        Hashtable<Object, Object> headers = new Hashtable<Object, Object>();
        headers.put(Constants.EXPORT_PACKAGE,
                "org.eclipse.equinox.weaving.tests"); //$NON-NLS-1$
        EasyMock.expect(bundle.getHeaders()).andStubReturn(headers);
        EasyMock.expect(
                bundle.getEntry(AspectAdmin.AOP_CONTEXT_DEFAULT_LOCATION))
                .andReturn(testAopDefFile);

        EasyMock.replay(bundle);
        aspectRegistry.bundleResolved(bundle);
        EasyMock.verify(bundle);

        Definition definition = aspectRegistry
                .getExportedAspectDefinitions(bundle);
        List<?> aspectClassNames = definition.getAspectClassNames();
        assertEquals(1, aspectClassNames.size());
        assertEquals("org.eclipse.equinox.weaving.tests.TestAspectType", //$NON-NLS-1$
                aspectClassNames.get(0));
        assertEquals(AspectAdmin.ASPECT_POLICY_NOT_DEFINED, aspectRegistry
                .getAspectPolicy(bundle, "org.eclipse.equinox.weaving.tests")); //$NON-NLS-1$
        assertEquals("here are the options ", definition.getWeaverOptions());

        Definition resolvedRequires = aspectRegistry.resolveRequiredBundle(
                bundle, AspectAdmin.ASPECT_APPLY_POLICY_NOT_DEFINED);
        assertNotNull(resolvedRequires);
        assertEquals(1, aspectClassNames.size());
        assertEquals("org.eclipse.equinox.weaving.tests.TestAspectType", //$NON-NLS-1$
                aspectClassNames.get(0));
        assertEquals("here are the options ", resolvedRequires
                .getWeaverOptions());

        Definition resolvedImports = aspectRegistry.resolveImportedPackage(
                bundle, "org.eclipse.equinox.weaving.tests", //$NON-NLS-1$
                AspectAdmin.ASPECT_APPLY_POLICY_NOT_DEFINED);
        assertNotNull(resolvedImports);
        assertEquals(1, aspectClassNames.size());
        assertEquals("org.eclipse.equinox.weaving.tests.TestAspectType", //$NON-NLS-1$
                aspectClassNames.get(0));
        assertEquals("here are the options ", resolvedImports
                .getWeaverOptions());
    }

    public void testConcreteAspectsInAopXml() {
        Bundle bundle = EasyMock.createMock(Bundle.class);

        URL testAopDefFile = this.getClass().getResource(
                "test-aop-with-concrete-aspect.xml"); //$NON-NLS-1$
        Hashtable<Object, Object> headers = new Hashtable<Object, Object>();
        headers
                .put(Constants.EXPORT_PACKAGE,
                        "org.eclipse.equinox.weaving.tests, org.eclipse.equinox.weaving.tests.concrete"); //$NON-NLS-1$
        EasyMock.expect(bundle.getHeaders()).andStubReturn(headers);
        EasyMock.expect(
                bundle.getEntry(AspectAdmin.AOP_CONTEXT_DEFAULT_LOCATION))
                .andReturn(testAopDefFile);

        EasyMock.replay(bundle);
        aspectRegistry.bundleResolved(bundle);
        EasyMock.verify(bundle);

        // exported definition
        Definition definition = aspectRegistry
                .getExportedAspectDefinitions(bundle);
        List<?> aspectClassNames = definition.getAspectClassNames();
        assertEquals(1, aspectClassNames.size());
        assertEquals("org.eclipse.equinox.weaving.tests.MyAspect", //$NON-NLS-1$
                aspectClassNames.get(0));
        assertEquals(AspectAdmin.ASPECT_POLICY_NOT_DEFINED, aspectRegistry
                .getAspectPolicy(bundle, "org.eclipse.equinox.weaving.tests")); //$NON-NLS-1$

        List<?> concreteAspects = definition.getConcreteAspects();
        assertEquals(1, concreteAspects.size());
        Definition.ConcreteAspect concreteAspect = (ConcreteAspect) concreteAspects
                .get(0);
        assertEquals("tracing.AbstractTracing", concreteAspect.extend); //$NON-NLS-1$
        assertEquals("org.eclipse.equinox.weaving.tests.concrete.MyTracing", //$NON-NLS-1$
                concreteAspect.name);
        assertNull(concreteAspect.perclause);
        assertEquals("com.xyz.first, *", concreteAspect.precedence); //$NON-NLS-1$
        assertEquals(
                "within(org.maw.*)", //$NON-NLS-1$
                ((Definition.Pointcut) concreteAspect.pointcuts.get(0)).expression);
        assertEquals("tracingScope", //$NON-NLS-1$
                ((Definition.Pointcut) concreteAspect.pointcuts.get(0)).name);

        // resolved require bundle
        Definition resolvedRequires = aspectRegistry.resolveRequiredBundle(
                bundle, AspectAdmin.ASPECT_APPLY_POLICY_NOT_DEFINED);
        aspectClassNames = resolvedRequires.getAspectClassNames();
        assertEquals(1, aspectClassNames.size());
        assertEquals("org.eclipse.equinox.weaving.tests.MyAspect", //$NON-NLS-1$
                aspectClassNames.get(0));
        assertEquals(AspectAdmin.ASPECT_POLICY_NOT_DEFINED, aspectRegistry
                .getAspectPolicy(bundle, "org.eclipse.equinox.weaving.tests")); //$NON-NLS-1$

        concreteAspects = resolvedRequires.getConcreteAspects();
        assertEquals(1, concreteAspects.size());
        concreteAspect = (ConcreteAspect) concreteAspects.get(0);
        assertEquals("tracing.AbstractTracing", concreteAspect.extend); //$NON-NLS-1$
        assertEquals("org.eclipse.equinox.weaving.tests.concrete.MyTracing", //$NON-NLS-1$
                concreteAspect.name);
        assertNull(concreteAspect.perclause);
        assertEquals("com.xyz.first, *", concreteAspect.precedence); //$NON-NLS-1$
        assertEquals(
                "within(org.maw.*)", //$NON-NLS-1$
                ((Definition.Pointcut) concreteAspect.pointcuts.get(0)).expression);
        assertEquals("tracingScope", //$NON-NLS-1$
                ((Definition.Pointcut) concreteAspect.pointcuts.get(0)).name);

        // resolve import package 1
        Definition resolvedImports = aspectRegistry.resolveImportedPackage(
                bundle, "org.eclipse.equinox.weaving.tests", //$NON-NLS-1$
                AspectAdmin.ASPECT_APPLY_POLICY_NOT_DEFINED);
        aspectClassNames = resolvedImports.getAspectClassNames();
        assertEquals(1, aspectClassNames.size());
        assertEquals("org.eclipse.equinox.weaving.tests.MyAspect", //$NON-NLS-1$
                aspectClassNames.get(0));
        assertEquals(AspectAdmin.ASPECT_POLICY_NOT_DEFINED, aspectRegistry
                .getAspectPolicy(bundle, "org.eclipse.equinox.weaving.tests")); //$NON-NLS-1$
        concreteAspects = resolvedImports.getConcreteAspects();
        assertEquals(0, concreteAspects.size());

        // resolve import package 2
        resolvedImports = aspectRegistry.resolveImportedPackage(bundle,
                "org.eclipse.equinox.weaving.tests.concrete", //$NON-NLS-1$
                AspectAdmin.ASPECT_APPLY_POLICY_NOT_DEFINED);
        aspectClassNames = resolvedImports.getAspectClassNames();
        assertEquals(0, aspectClassNames.size());
        concreteAspects = resolvedImports.getConcreteAspects();
        assertEquals(1, concreteAspects.size());
        concreteAspect = (ConcreteAspect) concreteAspects.get(0);
        assertEquals("tracing.AbstractTracing", concreteAspect.extend); //$NON-NLS-1$
        assertEquals("org.eclipse.equinox.weaving.tests.concrete.MyTracing", //$NON-NLS-1$
                concreteAspect.name);
        assertNull(concreteAspect.perclause);
        assertEquals("com.xyz.first, *", concreteAspect.precedence); //$NON-NLS-1$
        assertEquals(
                "within(org.maw.*)", //$NON-NLS-1$
                ((Definition.Pointcut) concreteAspect.pointcuts.get(0)).expression);
        assertEquals("tracingScope", //$NON-NLS-1$
                ((Definition.Pointcut) concreteAspect.pointcuts.get(0)).name);
    }

    public void testExportedAndNotExportedConcreteAspectsInAopXml() {
        Bundle bundle = EasyMock.createMock(Bundle.class);

        URL testAopDefFile = this.getClass().getResource(
                "test-aop-with-concrete-aspects-in-different-packages.xml"); //$NON-NLS-1$
        Hashtable<Object, Object> headers = new Hashtable<Object, Object>();
        headers.put(Constants.EXPORT_PACKAGE,
                "org.eclipse.equinox.weaving.tests.concrete"); //$NON-NLS-1$
        EasyMock.expect(bundle.getHeaders()).andStubReturn(headers);
        EasyMock.expect(
                bundle.getEntry(AspectAdmin.AOP_CONTEXT_DEFAULT_LOCATION))
                .andReturn(testAopDefFile);

        EasyMock.replay(bundle);
        aspectRegistry.bundleResolved(bundle);
        EasyMock.verify(bundle);

        Definition definition = aspectRegistry
                .getExportedAspectDefinitions(bundle);
        List<?> aspectClassNames = definition.getAspectClassNames();
        assertEquals(0, aspectClassNames.size());

        List<?> concreteAspects = definition.getConcreteAspects();
        assertEquals(1, concreteAspects.size());
        Definition.ConcreteAspect concreteAspect = (ConcreteAspect) concreteAspects
                .get(0);
        assertEquals("tracing.AbstractTracing", concreteAspect.extend); //$NON-NLS-1$
        assertEquals("org.eclipse.equinox.weaving.tests.concrete.MyTracing", //$NON-NLS-1$
                concreteAspect.name);
        assertNull(concreteAspect.perclause);
        assertEquals("com.xyz.first, *", concreteAspect.precedence); //$NON-NLS-1$
        assertEquals(
                "within(org.maw.*)", //$NON-NLS-1$
                ((Definition.Pointcut) concreteAspect.pointcuts.get(0)).expression);
        assertEquals("tracingScope", //$NON-NLS-1$
                ((Definition.Pointcut) concreteAspect.pointcuts.get(0)).name);

        // resolved require bundle
        Definition resolvedRequires = aspectRegistry.resolveRequiredBundle(
                bundle, AspectAdmin.ASPECT_APPLY_POLICY_NOT_DEFINED);
        aspectClassNames = resolvedRequires.getAspectClassNames();
        assertEquals(0, aspectClassNames.size());

        concreteAspects = definition.getConcreteAspects();
        assertEquals(1, concreteAspects.size());
        concreteAspect = (ConcreteAspect) concreteAspects.get(0);
        assertEquals("tracing.AbstractTracing", concreteAspect.extend); //$NON-NLS-1$
        assertEquals("org.eclipse.equinox.weaving.tests.concrete.MyTracing", //$NON-NLS-1$
                concreteAspect.name);
        assertNull(concreteAspect.perclause);
        assertEquals("com.xyz.first, *", concreteAspect.precedence); //$NON-NLS-1$
        assertEquals(
                "within(org.maw.*)", //$NON-NLS-1$
                ((Definition.Pointcut) concreteAspect.pointcuts.get(0)).expression);
        assertEquals("tracingScope", //$NON-NLS-1$
                ((Definition.Pointcut) concreteAspect.pointcuts.get(0)).name);

        // resolve import package 1
        Definition resolvedImports = aspectRegistry.resolveImportedPackage(
                bundle, "org.eclipse.equinox.weaving.tests.concrete", //$NON-NLS-1$
                AspectAdmin.ASPECT_APPLY_POLICY_NOT_DEFINED);
        aspectClassNames = resolvedImports.getAspectClassNames();
        assertEquals(0, aspectClassNames.size());
        concreteAspects = resolvedImports.getConcreteAspects();
        assertEquals(1, concreteAspects.size());
        concreteAspect = (ConcreteAspect) concreteAspects.get(0);
        assertEquals("tracing.AbstractTracing", concreteAspect.extend); //$NON-NLS-1$
        assertEquals("org.eclipse.equinox.weaving.tests.concrete.MyTracing", //$NON-NLS-1$
                concreteAspect.name);
        assertNull(concreteAspect.perclause);
        assertEquals("com.xyz.first, *", concreteAspect.precedence); //$NON-NLS-1$
        assertEquals(
                "within(org.maw.*)", //$NON-NLS-1$
                ((Definition.Pointcut) concreteAspect.pointcuts.get(0)).expression);
        assertEquals("tracingScope", //$NON-NLS-1$
                ((Definition.Pointcut) concreteAspect.pointcuts.get(0)).name);

        // resolve import package 2
        resolvedImports = aspectRegistry.resolveImportedPackage(bundle,
                "org.eclipse.equinox.weaving.tests.concrete.nonvisible", //$NON-NLS-1$
                AspectAdmin.ASPECT_APPLY_POLICY_NOT_DEFINED);
        assertNull(resolvedImports);
    }

    public void testExportedAspectsInManifest() {
        Bundle bundle = EasyMock.createMock(Bundle.class);

        Hashtable<Object, Object> headers = new Hashtable<Object, Object>();
        StringBuilder export = new StringBuilder();
        export.append("org.eclipse.equinox.weaving.tests;"); //$NON-NLS-1$
        export.append("aspect-policy:=opt-in;"); //$NON-NLS-1$
        export.append("aspects=\"TestAspect,OtherAspect\", "); //$NON-NLS-1$

        export.append("org.eclipse.equinox.weaving.tests2;"); //$NON-NLS-1$
        export.append("aspect-policy:=opt-out;"); //$NON-NLS-1$
        export.append("aspects=\"SecondAspect,OtherSecondAspect\""); //$NON-NLS-1$
        headers.put(Constants.EXPORT_PACKAGE, export.toString());

        EasyMock.expect(bundle.getHeaders()).andStubReturn(headers);
        EasyMock.expect(
                bundle.getEntry(AspectAdmin.AOP_CONTEXT_DEFAULT_LOCATION))
                .andReturn(null);

        EasyMock.replay(bundle);
        aspectRegistry.bundleResolved(bundle);
        EasyMock.verify(bundle);

        Definition definition = aspectRegistry
                .getExportedAspectDefinitions(bundle);
        List<?> aspectClassNames = definition.getAspectClassNames();
        assertEquals(4, aspectClassNames.size());
        assertEquals("org.eclipse.equinox.weaving.tests.TestAspect", //$NON-NLS-1$
                aspectClassNames.get(0));
        assertEquals("org.eclipse.equinox.weaving.tests.OtherAspect", //$NON-NLS-1$
                aspectClassNames.get(1));
        assertEquals("org.eclipse.equinox.weaving.tests2.SecondAspect", //$NON-NLS-1$
                aspectClassNames.get(2));
        assertEquals("org.eclipse.equinox.weaving.tests2.OtherSecondAspect", //$NON-NLS-1$
                aspectClassNames.get(3));
        assertEquals(AspectAdmin.ASPECT_POLICY_OPT_IN, aspectRegistry
                .getAspectPolicy(bundle, "org.eclipse.equinox.weaving.tests")); //$NON-NLS-1$
        assertEquals(AspectAdmin.ASPECT_POLICY_OPT_OUT, aspectRegistry
                .getAspectPolicy(bundle, "org.eclipse.equinox.weaving.tests2")); //$NON-NLS-1$
    }

    public void testResolveRequiredAndImportedAspects() {
        Bundle bundle = EasyMock.createMock(Bundle.class);

        Hashtable<Object, Object> headers = new Hashtable<Object, Object>();
        StringBuilder export = new StringBuilder();

        // opt-in aspects
        export.append("org.eclipse.equinox.weaving.tests;"); //$NON-NLS-1$
        export.append("aspect-policy:=opt-in;"); //$NON-NLS-1$
        export.append("aspects=\"TestAspect,OtherAspect\", "); //$NON-NLS-1$

        // opt-out aspects
        export.append("org.eclipse.equinox.weaving.tests2;"); //$NON-NLS-1$
        export.append("aspect-policy:=opt-out;"); //$NON-NLS-1$
        export.append("aspects=\"SecondAspect,OtherSecondAspect\", "); //$NON-NLS-1$

        // policy not defined aspects
        export.append("org.eclipse.equinox.weaving.tests3;"); //$NON-NLS-1$
        export.append("aspects=\"ThirdAspect,OtherThirdAspect\""); //$NON-NLS-1$
        headers.put(Constants.EXPORT_PACKAGE, export.toString());

        EasyMock.expect(bundle.getHeaders()).andStubReturn(headers);
        EasyMock.expect(
                bundle.getEntry(AspectAdmin.AOP_CONTEXT_DEFAULT_LOCATION))
                .andReturn(null);

        EasyMock.replay(bundle);
        aspectRegistry.bundleResolved(bundle);
        EasyMock.verify(bundle);

        // test required bundle with apply-aspects:=true
        Definition definition = aspectRegistry.resolveRequiredBundle(bundle,
                AspectAdmin.ASPECT_APPLY_POLICY_TRUE);
        assertNotNull(definition);
        List<?> aspects = definition.getAspectClassNames();
        assertEquals(6, aspects.size());
        assertEquals("org.eclipse.equinox.weaving.tests.TestAspect", aspects //$NON-NLS-1$
                .get(0));
        assertEquals("org.eclipse.equinox.weaving.tests.OtherAspect", aspects //$NON-NLS-1$
                .get(1));
        assertEquals("org.eclipse.equinox.weaving.tests2.SecondAspect", aspects //$NON-NLS-1$
                .get(2));
        assertEquals("org.eclipse.equinox.weaving.tests2.OtherSecondAspect", //$NON-NLS-1$
                aspects.get(3));
        assertEquals("org.eclipse.equinox.weaving.tests3.ThirdAspect", aspects //$NON-NLS-1$
                .get(4));
        assertEquals("org.eclipse.equinox.weaving.tests3.OtherThirdAspect", //$NON-NLS-1$
                aspects.get(5));

        // test required bundle with apply-aspects:=false
        definition = aspectRegistry.resolveRequiredBundle(bundle,
                AspectAdmin.ASPECT_APPLY_POLICY_FALSE);
        assertNull(definition);

        // test required bundle with apply-aspects not being defined
        definition = aspectRegistry.resolveRequiredBundle(bundle,
                AspectAdmin.ASPECT_APPLY_POLICY_NOT_DEFINED);
        assertNotNull(definition);
        aspects = definition.getAspectClassNames();
        assertEquals(4, aspects.size());
        assertEquals("org.eclipse.equinox.weaving.tests2.SecondAspect", aspects //$NON-NLS-1$
                .get(0));
        assertEquals("org.eclipse.equinox.weaving.tests2.OtherSecondAspect", //$NON-NLS-1$
                aspects.get(1));
        assertEquals("org.eclipse.equinox.weaving.tests3.ThirdAspect", aspects //$NON-NLS-1$
                .get(2));
        assertEquals("org.eclipse.equinox.weaving.tests3.OtherThirdAspect", //$NON-NLS-1$
                aspects.get(3));

        // test imported first package with apply-aspects:=true
        definition = aspectRegistry
                .resolveImportedPackage(
                        bundle,
                        "org.eclipse.equinox.weaving.tests", AspectAdmin.ASPECT_APPLY_POLICY_TRUE); //$NON-NLS-1$
        assertNotNull(definition);
        aspects = definition.getAspectClassNames();
        assertEquals(2, aspects.size());
        assertEquals("org.eclipse.equinox.weaving.tests.TestAspect", aspects //$NON-NLS-1$
                .get(0));
        assertEquals("org.eclipse.equinox.weaving.tests.OtherAspect", aspects //$NON-NLS-1$
                .get(1));

        // test imported first package with apply-aspects:=false
        definition = aspectRegistry
                .resolveImportedPackage(
                        bundle,
                        "org.eclipse.equinox.weaving.tests", AspectAdmin.ASPECT_APPLY_POLICY_FALSE); //$NON-NLS-1$
        assertNull(definition);

        // test imported first package with apply-aspects not defined
        definition = aspectRegistry
                .resolveImportedPackage(
                        bundle,
                        "org.eclipse.equinox.weaving.tests", AspectAdmin.ASPECT_APPLY_POLICY_NOT_DEFINED); //$NON-NLS-1$
        assertNull(definition);

        // test imported second package with apply-aspects:=true
        definition = aspectRegistry
                .resolveImportedPackage(
                        bundle,
                        "org.eclipse.equinox.weaving.tests2", AspectAdmin.ASPECT_APPLY_POLICY_TRUE); //$NON-NLS-1$
        assertNotNull(definition);
        aspects = definition.getAspectClassNames();
        assertEquals(2, aspects.size());
        assertEquals("org.eclipse.equinox.weaving.tests2.SecondAspect", aspects //$NON-NLS-1$
                .get(0));
        assertEquals("org.eclipse.equinox.weaving.tests2.OtherSecondAspect", //$NON-NLS-1$
                aspects.get(1));

        // test imported second package with apply-aspects:=false
        definition = aspectRegistry
                .resolveImportedPackage(
                        bundle,
                        "org.eclipse.equinox.weaving.tests2", AspectAdmin.ASPECT_APPLY_POLICY_FALSE); //$NON-NLS-1$
        assertNull(definition);

        // test imported second package with apply-aspects not defined
        definition = aspectRegistry
                .resolveImportedPackage(
                        bundle,
                        "org.eclipse.equinox.weaving.tests2", AspectAdmin.ASPECT_APPLY_POLICY_NOT_DEFINED); //$NON-NLS-1$
        assertNotNull(definition);
        aspects = definition.getAspectClassNames();
        assertEquals(2, aspects.size());
        assertEquals("org.eclipse.equinox.weaving.tests2.SecondAspect", aspects //$NON-NLS-1$
                .get(0));
        assertEquals("org.eclipse.equinox.weaving.tests2.OtherSecondAspect", //$NON-NLS-1$
                aspects.get(1));

        // test imported third package with apply-aspects:=true
        definition = aspectRegistry
                .resolveImportedPackage(
                        bundle,
                        "org.eclipse.equinox.weaving.tests3", AspectAdmin.ASPECT_APPLY_POLICY_TRUE); //$NON-NLS-1$
        assertNotNull(definition);
        aspects = definition.getAspectClassNames();
        assertEquals(2, aspects.size());
        assertEquals("org.eclipse.equinox.weaving.tests3.ThirdAspect", aspects //$NON-NLS-1$
                .get(0));
        assertEquals("org.eclipse.equinox.weaving.tests3.OtherThirdAspect", //$NON-NLS-1$
                aspects.get(1));

        // test imported third package with apply-aspects:=false
        definition = aspectRegistry
                .resolveImportedPackage(
                        bundle,
                        "org.eclipse.equinox.weaving.tests3", AspectAdmin.ASPECT_APPLY_POLICY_FALSE); //$NON-NLS-1$
        assertNull(definition);

        // test imported third package with apply-aspects not defined
        definition = aspectRegistry
                .resolveImportedPackage(
                        bundle,
                        "org.eclipse.equinox.weaving.tests3", AspectAdmin.ASPECT_APPLY_POLICY_NOT_DEFINED); //$NON-NLS-1$
        assertNotNull(definition);
        aspects = definition.getAspectClassNames();
        assertEquals(2, aspects.size());
        assertEquals("org.eclipse.equinox.weaving.tests3.ThirdAspect", aspects //$NON-NLS-1$
                .get(0));
        assertEquals("org.eclipse.equinox.weaving.tests3.OtherThirdAspect", //$NON-NLS-1$
                aspects.get(1));
    }

    public void testExportedAspectsInAopXmlAndManifest() {
        Bundle bundle = EasyMock.createMock(Bundle.class);

        Hashtable<Object, Object> headers = new Hashtable<Object, Object>();
        StringBuilder export = new StringBuilder();
        export.append("org.eclipse.equinox.weaving.tests, "); //$NON-NLS-1$
        export.append("org.eclipse.equinox.weaving.tests2;"); //$NON-NLS-1$
        export.append("aspect-policy:=opt-out;"); //$NON-NLS-1$
        export.append("aspects=\"SecondAspect,OtherSecondAspect\""); //$NON-NLS-1$
        headers.put(Constants.EXPORT_PACKAGE, export.toString());

        EasyMock.expect(bundle.getHeaders()).andStubReturn(headers);
        URL testAopDefFile = this.getClass().getResource("test-aop.xml"); //$NON-NLS-1$
        EasyMock.expect(
                bundle.getEntry(AspectAdmin.AOP_CONTEXT_DEFAULT_LOCATION))
                .andReturn(testAopDefFile);

        EasyMock.replay(bundle);
        aspectRegistry.bundleResolved(bundle);
        EasyMock.verify(bundle);

        Definition definition = aspectRegistry
                .getExportedAspectDefinitions(bundle);
        List<?> aspectClassNames = definition.getAspectClassNames();
        assertEquals(3, aspectClassNames.size());
        assertEquals("org.eclipse.equinox.weaving.tests2.SecondAspect", //$NON-NLS-1$
                aspectClassNames.get(0));
        assertEquals("org.eclipse.equinox.weaving.tests2.OtherSecondAspect", //$NON-NLS-1$
                aspectClassNames.get(1));
        assertEquals("org.eclipse.equinox.weaving.tests.TestAspectType", //$NON-NLS-1$
                aspectClassNames.get(2));
        assertEquals(AspectAdmin.ASPECT_POLICY_NOT_DEFINED, aspectRegistry
                .getAspectPolicy(bundle, "org.eclipse.equinox.weaving.tests")); //$NON-NLS-1$
        assertEquals(AspectAdmin.ASPECT_POLICY_OPT_OUT, aspectRegistry
                .getAspectPolicy(bundle, "org.eclipse.equinox.weaving.tests2")); //$NON-NLS-1$
    }

}
