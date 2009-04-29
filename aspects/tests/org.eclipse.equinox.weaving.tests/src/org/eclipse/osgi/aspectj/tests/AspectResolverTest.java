/*******************************************************************************
 * Copyright (c) 2009 Martin Lippert and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html.
 * 
 * Contributors:
 *   Martin Lippert            initial implementation
 *   Martin Lippert            fragment handling fixed
 ******************************************************************************/

package org.eclipse.osgi.aspectj.tests;

import java.util.Hashtable;

import junit.framework.TestCase;

import org.aspectj.weaver.loadtime.definition.Definition;
import org.easymock.EasyMock;
import org.eclipse.equinox.service.weaving.ISupplementerRegistry;
import org.eclipse.equinox.service.weaving.Supplementer;
import org.eclipse.equinox.weaving.aspectj.AspectAdmin;
import org.eclipse.equinox.weaving.aspectj.AspectConfiguration;
import org.eclipse.equinox.weaving.aspectj.loadtime.AspectResolver;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.ExportPackageDescription;
import org.eclipse.osgi.service.resolver.State;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;

/**
 * @author Martin Lippert
 */
@SuppressWarnings("nls")
public class AspectResolverTest extends TestCase {

    private Object[] mocks;

    private Bundle bundle;

    private BundleDescription bundleDescription;

    private ISupplementerRegistry supplementerRegistry;

    private AspectAdmin aspectAdmin;

    private State state;

    private Bundle requiredBundle1;

    private Bundle requiredBundle2;

    private Bundle importedBundle1;

    private Bundle importedBundle2;

    private BundleDescription requiredBundle1Desc;

    private BundleDescription requiredBundle2Desc;

    private ExportPackageDescription importedPackage1;

    private ExportPackageDescription importedPackage2;

    private AspectResolver resolver;

    private Bundle supplementerBundle;

    private BundleDescription supplementerBundleDesc;

    private BundleDescription importedBundle1Desc;

    private BundleDescription importedBundle2Desc;

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        supplementerRegistry = EasyMock.createMock(ISupplementerRegistry.class);
        aspectAdmin = EasyMock.createMock(AspectAdmin.class);
        state = EasyMock.createMock(State.class);

        BundleContext bundleContext = EasyMock.createMock(BundleContext.class);

        bundle = EasyMock.createMock(Bundle.class);
        EasyMock.expect(bundle.getBundleId()).andStubReturn(10l);
        EasyMock.expect(bundle.getSymbolicName()).andStubReturn("bundle");
        bundleDescription = EasyMock.createMock(BundleDescription.class);
        EasyMock.expect(bundleDescription.getBundleId()).andStubReturn(10l);
        EasyMock.expect(bundleDescription.getFragments()).andStubReturn(
                new BundleDescription[0]);
        EasyMock.expect(bundleDescription.getVersion()).andStubReturn(
                new Version("0.0.0")); //$NON-NLS-1$
        EasyMock.expect(bundleContext.getBundle(10l)).andStubReturn(bundle);

        requiredBundle1 = EasyMock.createMock(Bundle.class);
        EasyMock.expect(requiredBundle1.getBundleId()).andStubReturn(30l);
        EasyMock.expect(requiredBundle1.getSymbolicName()).andStubReturn(
                "required1"); //$NON-NLS-1$
        EasyMock.expect(bundleContext.getBundle(30l)).andStubReturn(
                requiredBundle1);
        requiredBundle2 = EasyMock.createMock(Bundle.class);
        EasyMock.expect(requiredBundle2.getBundleId()).andStubReturn(31l);
        EasyMock.expect(requiredBundle2.getSymbolicName()).andStubReturn(
                "required2"); //$NON-NLS-1$
        EasyMock.expect(bundleContext.getBundle(31l)).andStubReturn(
                requiredBundle2);
        importedBundle1 = EasyMock.createMock(Bundle.class);
        importedBundle1Desc = EasyMock.createMock(BundleDescription.class);
        EasyMock.expect(importedBundle1.getBundleId()).andStubReturn(80l);
        EasyMock.expect(importedBundle1Desc.getBundleId()).andStubReturn(80l);
        EasyMock.expect(importedBundle1.getSymbolicName()).andStubReturn(
                "imported1"); //$NON-NLS-1$
        EasyMock.expect(bundleContext.getBundle(80l)).andStubReturn(
                importedBundle1);
        importedBundle2 = EasyMock.createMock(Bundle.class);
        importedBundle2Desc = EasyMock.createMock(BundleDescription.class);
        EasyMock.expect(importedBundle2Desc.getBundleId()).andStubReturn(81l);
        EasyMock.expect(importedBundle2.getBundleId()).andStubReturn(81l);
        EasyMock.expect(importedBundle2.getSymbolicName()).andStubReturn(
                "imported2"); //$NON-NLS-1$
        EasyMock.expect(bundleContext.getBundle(81l)).andStubReturn(
                importedBundle2);

        requiredBundle1Desc = EasyMock.createMock(BundleDescription.class);
        EasyMock.expect(requiredBundle1Desc.getBundleId()).andStubReturn(30l);
        EasyMock.expect(requiredBundle1Desc.getSymbolicName()).andStubReturn(
                "required1"); //$NON-NLS-1$
        EasyMock.expect(requiredBundle1Desc.getVersion()).andStubReturn(
                new Version("1.1.1")); //$NON-NLS-1$
        requiredBundle2Desc = EasyMock.createMock(BundleDescription.class);
        EasyMock.expect(requiredBundle2Desc.getBundleId()).andStubReturn(31l);
        EasyMock.expect(requiredBundle2Desc.getSymbolicName()).andStubReturn(
                "required2"); //$NON-NLS-1$
        EasyMock.expect(requiredBundle2Desc.getVersion()).andStubReturn(
                new Version("2.2.2")); //$NON-NLS-1$
        importedPackage1 = EasyMock.createMock(ExportPackageDescription.class);
        EasyMock.expect(importedPackage1.getName()).andStubReturn("imported1");
        EasyMock.expect(importedPackage1.getVersion()).andStubReturn(
                new Version("5.5.5")); //$NON-NLS-1$
        EasyMock.expect(importedPackage1.getExporter()).andStubReturn(
                importedBundle1Desc);
        importedPackage2 = EasyMock.createMock(ExportPackageDescription.class);

        EasyMock.expect(state.getBundle(30l))
                .andStubReturn(requiredBundle1Desc);
        EasyMock.expect(state.getBundle(31l))
                .andStubReturn(requiredBundle2Desc);

        supplementerBundle = EasyMock.createMock(Bundle.class);
        EasyMock.expect(supplementerBundle.getSymbolicName()).andStubReturn(
                "supplementer"); //$NON-NLS-1$
        EasyMock.expect(supplementerBundle.getBundleId()).andStubReturn(20l);
        supplementerBundleDesc = EasyMock.createMock(BundleDescription.class);
        EasyMock.expect(supplementerBundleDesc.getVersion()).andStubReturn(
                new Version("1.2.3")); //$NON-NLS-1$
        EasyMock.expect(state.getBundle(20l)).andStubReturn(
                supplementerBundleDesc);

        mocks = new Object[] { supplementerRegistry, aspectAdmin, state,
                bundle, bundleDescription, requiredBundle1Desc,
                requiredBundle2Desc, importedPackage1, importedPackage2,
                bundleContext, requiredBundle1, requiredBundle2,
                supplementerBundle, supplementerBundleDesc, importedBundle1,
                importedBundle2, importedBundle1Desc };

        resolver = new AspectResolver(state, supplementerRegistry, aspectAdmin,
                bundleContext);
    }

    public void testResolveNoAspectsNoRequiresNoImportsNoSupplementers() {
        EasyMock.expect(aspectAdmin.getAspectDefinition(bundle)).andStubReturn(
                null);
        EasyMock.expect(bundleDescription.getResolvedRequires()).andStubReturn(
                new BundleDescription[0]);
        EasyMock.expect(bundleDescription.getResolvedImports()).andStubReturn(
                new ExportPackageDescription[0]);
        EasyMock.expect(supplementerRegistry.getSupplementers(10l))
                .andStubReturn(new Supplementer[0]);

        EasyMock.replay(mocks);
        AspectConfiguration resolvedAspects = resolver.resolveAspectsFor(
                bundle, bundleDescription);
        EasyMock.verify(mocks);

        assertNotNull(resolvedAspects);
        assertEquals(0, resolvedAspects.getAspectDefinitions().size());
        assertEquals("", resolvedAspects.getFingerprint());
    }

    public void testResolveIgnoreOwnAspects() {
        Definition ownAspects = new Definition();
        EasyMock.expect(aspectAdmin.getAspectDefinition(bundle)).andStubReturn(
                ownAspects);
        EasyMock.expect(bundleDescription.getResolvedRequires()).andStubReturn(
                new BundleDescription[0]);
        EasyMock.expect(bundleDescription.getResolvedImports()).andStubReturn(
                new ExportPackageDescription[0]);

        EasyMock.expect(supplementerRegistry.getSupplementers(10l))
                .andStubReturn(new Supplementer[0]);

        EasyMock.replay(mocks);
        AspectConfiguration resolvedAspects = resolver.resolveAspectsFor(
                bundle, bundleDescription);
        EasyMock.verify(mocks);

        assertNotNull(resolvedAspects);
        assertEquals(0, resolvedAspects.getAspectDefinitions().size());
        assertEquals("", resolvedAspects.getFingerprint());
    }

    public void testResolveWithSupplementers() {
        Definition ownAspects = new Definition();
        EasyMock.expect(aspectAdmin.getAspectDefinition(bundle)).andStubReturn(
                ownAspects);
        EasyMock.expect(bundleDescription.getResolvedRequires()).andStubReturn(
                new BundleDescription[0]);
        EasyMock.expect(bundleDescription.getResolvedImports()).andStubReturn(
                new ExportPackageDescription[0]);

        Supplementer supplementer = new Supplementer(supplementerBundle, null,
                null, null, null);
        EasyMock.expect(supplementerRegistry.getSupplementers(10l))
                .andStubReturn(new Supplementer[] { supplementer });

        Definition supplementerAspects = new Definition();
        EasyMock.expect(
                aspectAdmin.getExportedAspectDefinitions(supplementerBundle))
                .andStubReturn(supplementerAspects);
        EasyMock.expect(aspectAdmin.getAspectDefinition(supplementerBundle))
                .andStubReturn(new Definition());

        EasyMock.replay(mocks);
        AspectConfiguration resolvedAspects = resolver.resolveAspectsFor(
                bundle, bundleDescription);
        EasyMock.verify(mocks);

        assertNotNull(resolvedAspects);
        assertEquals(2, resolvedAspects.getAspectDefinitions().size());
        assertSame(supplementerAspects, resolvedAspects.getAspectDefinitions()
                .get(0));
        assertSame(ownAspects, resolvedAspects.getAspectDefinitions().get(1));
        assertEquals("bundle:0.0.0;supplementer:1.2.3;", resolvedAspects
                .getFingerprint());
    }

    public void testResolveWithRequiredBundlesNoApplyAspectsPolicy() {
        Hashtable<Object, Object> headers = new Hashtable<Object, Object>();
        headers.put(Constants.REQUIRE_BUNDLE, "required1");
        EasyMock.expect(bundle.getHeaders()).andStubReturn(headers);

        EasyMock.expect(aspectAdmin.getAspectDefinition(bundle)).andStubReturn(
                null);
        EasyMock.expect(bundleDescription.getResolvedRequires()).andStubReturn(
                new BundleDescription[] { requiredBundle1Desc });
        EasyMock.expect(bundleDescription.getResolvedImports()).andStubReturn(
                new ExportPackageDescription[0]);

        EasyMock.expect(supplementerRegistry.getSupplementers(10l))
                .andStubReturn(new Supplementer[0]);

        Definition requiredBundleAspects = new Definition();
        EasyMock.expect(
                aspectAdmin.resolveRequiredBundle(requiredBundle1,
                        AspectAdmin.ASPECT_APPLY_POLICY_NOT_DEFINED))
                .andStubReturn(requiredBundleAspects);
        EasyMock.expect(aspectAdmin.getAspectDefinition(requiredBundle1))
                .andStubReturn(new Definition());

        EasyMock.replay(mocks);
        AspectConfiguration resolvedAspects = resolver.resolveAspectsFor(
                bundle, bundleDescription);
        EasyMock.verify(mocks);

        assertNotNull(resolvedAspects);
        assertEquals(1, resolvedAspects.getAspectDefinitions().size());
        assertSame(requiredBundleAspects, resolvedAspects
                .getAspectDefinitions().get(0));
        assertEquals("required1:1.1.1;", resolvedAspects.getFingerprint());
    }

    public void testResolveWithRequiredBundlesDoApplyAspectsPolicy() {
        Hashtable<Object, Object> headers = new Hashtable<Object, Object>();
        headers.put(Constants.REQUIRE_BUNDLE, "required1;apply-aspects:=true");
        EasyMock.expect(bundle.getHeaders()).andStubReturn(headers);

        EasyMock.expect(aspectAdmin.getAspectDefinition(bundle)).andStubReturn(
                null);
        EasyMock.expect(bundleDescription.getResolvedRequires()).andStubReturn(
                new BundleDescription[] { requiredBundle1Desc });
        EasyMock.expect(bundleDescription.getResolvedImports()).andStubReturn(
                new ExportPackageDescription[0]);

        EasyMock.expect(supplementerRegistry.getSupplementers(10l))
                .andStubReturn(new Supplementer[0]);

        Definition requiredBundleAspects = new Definition();
        EasyMock.expect(
                aspectAdmin.resolveRequiredBundle(requiredBundle1,
                        AspectAdmin.ASPECT_APPLY_POLICY_TRUE)).andStubReturn(
                requiredBundleAspects);
        EasyMock.expect(aspectAdmin.getAspectDefinition(requiredBundle1))
                .andStubReturn(new Definition());

        EasyMock.replay(mocks);
        AspectConfiguration resolvedAspects = resolver.resolveAspectsFor(
                bundle, bundleDescription);
        EasyMock.verify(mocks);

        assertNotNull(resolvedAspects);
        assertEquals(1, resolvedAspects.getAspectDefinitions().size());
        assertSame(requiredBundleAspects, resolvedAspects
                .getAspectDefinitions().get(0));
        assertEquals("required1:1.1.1;", resolvedAspects.getFingerprint());
    }

    public void testResolveWithRequiredBundlesDontApplyAspectsPolicy() {
        Hashtable<Object, Object> headers = new Hashtable<Object, Object>();
        headers.put(Constants.REQUIRE_BUNDLE, "required1;apply-aspects:=false");
        EasyMock.expect(bundle.getHeaders()).andStubReturn(headers);

        EasyMock.expect(aspectAdmin.getAspectDefinition(bundle)).andStubReturn(
                null);
        EasyMock.expect(bundleDescription.getResolvedRequires()).andStubReturn(
                new BundleDescription[] { requiredBundle1Desc });
        EasyMock.expect(bundleDescription.getResolvedImports()).andStubReturn(
                new ExportPackageDescription[0]);

        EasyMock.expect(supplementerRegistry.getSupplementers(10l))
                .andStubReturn(new Supplementer[0]);

        EasyMock.expect(
                aspectAdmin.resolveRequiredBundle(requiredBundle1,
                        AspectAdmin.ASPECT_APPLY_POLICY_FALSE)).andStubReturn(
                null);
        EasyMock.expect(aspectAdmin.getAspectDefinition(requiredBundle1))
                .andStubReturn(new Definition());

        EasyMock.replay(mocks);
        AspectConfiguration resolvedAspects = resolver.resolveAspectsFor(
                bundle, bundleDescription);
        EasyMock.verify(mocks);

        assertNotNull(resolvedAspects);
        assertEquals(0, resolvedAspects.getAspectDefinitions().size());
        assertEquals("", resolvedAspects.getFingerprint());
    }

    public void testResolveWithImportedPackageNoApplyAspectsPolicy() {
        Hashtable<Object, Object> headers = new Hashtable<Object, Object>();
        headers.put(Constants.IMPORT_PACKAGE, "imported1");
        EasyMock.expect(bundle.getHeaders()).andStubReturn(headers);

        EasyMock.expect(aspectAdmin.getAspectDefinition(bundle)).andStubReturn(
                null);
        EasyMock.expect(bundleDescription.getResolvedRequires()).andStubReturn(
                new BundleDescription[0]);
        EasyMock.expect(bundleDescription.getResolvedImports()).andStubReturn(
                new ExportPackageDescription[] { importedPackage1 });

        EasyMock.expect(supplementerRegistry.getSupplementers(10l))
                .andStubReturn(new Supplementer[0]);

        Definition importedPackageAspects = new Definition();
        EasyMock.expect(
                aspectAdmin.resolveImportedPackage(importedBundle1,
                        "imported1", //$NON-NLS-1$
                        AspectAdmin.ASPECT_APPLY_POLICY_NOT_DEFINED))
                .andStubReturn(importedPackageAspects);
        EasyMock.expect(aspectAdmin.getAspectDefinition(requiredBundle1))
                .andStubReturn(new Definition());

        EasyMock.replay(mocks);
        AspectConfiguration resolvedAspects = resolver.resolveAspectsFor(
                bundle, bundleDescription);
        EasyMock.verify(mocks);

        assertNotNull(resolvedAspects);
        assertEquals(1, resolvedAspects.getAspectDefinitions().size());
        assertSame(importedPackageAspects, resolvedAspects
                .getAspectDefinitions().get(0));
        assertEquals("imported1:5.5.5;", resolvedAspects.getFingerprint());
    }

    public void testResolveWithImportedPackageDoApplyAspectsPolicy() {
        Hashtable<Object, Object> headers = new Hashtable<Object, Object>();
        headers.put(Constants.IMPORT_PACKAGE, "imported1;apply-aspects:=true");
        EasyMock.expect(bundle.getHeaders()).andStubReturn(headers);

        EasyMock.expect(aspectAdmin.getAspectDefinition(bundle)).andStubReturn(
                null);
        EasyMock.expect(bundleDescription.getResolvedRequires()).andStubReturn(
                new BundleDescription[0]);
        EasyMock.expect(bundleDescription.getResolvedImports()).andStubReturn(
                new ExportPackageDescription[] { importedPackage1 });

        EasyMock.expect(supplementerRegistry.getSupplementers(10l))
                .andStubReturn(new Supplementer[0]);

        Definition importedPackageAspects = new Definition();
        EasyMock.expect(
                aspectAdmin.resolveImportedPackage(importedBundle1,
                        "imported1", //$NON-NLS-1$
                        AspectAdmin.ASPECT_APPLY_POLICY_TRUE)).andStubReturn(
                importedPackageAspects);
        EasyMock.expect(aspectAdmin.getAspectDefinition(requiredBundle1))
                .andStubReturn(new Definition());

        EasyMock.replay(mocks);
        AspectConfiguration resolvedAspects = resolver.resolveAspectsFor(
                bundle, bundleDescription);
        EasyMock.verify(mocks);

        assertNotNull(resolvedAspects);
        assertEquals(1, resolvedAspects.getAspectDefinitions().size());
        assertSame(importedPackageAspects, resolvedAspects
                .getAspectDefinitions().get(0));
        assertEquals("imported1:5.5.5;", resolvedAspects.getFingerprint());
    }

    public void testResolveWithImportedPackageDontApplyAspectsPolicy() {
        Hashtable<Object, Object> headers = new Hashtable<Object, Object>();
        headers.put(Constants.IMPORT_PACKAGE, "imported1;apply-aspects:=false");
        EasyMock.expect(bundle.getHeaders()).andStubReturn(headers);

        EasyMock.expect(aspectAdmin.getAspectDefinition(bundle)).andStubReturn(
                null);
        EasyMock.expect(bundleDescription.getResolvedRequires()).andStubReturn(
                new BundleDescription[0]);
        EasyMock.expect(bundleDescription.getResolvedImports()).andStubReturn(
                new ExportPackageDescription[] { importedPackage1 });

        EasyMock.expect(supplementerRegistry.getSupplementers(10l))
                .andStubReturn(new Supplementer[0]);

        Definition importedPackageAspects = new Definition();
        EasyMock.expect(
                aspectAdmin.resolveImportedPackage(importedBundle1,
                        "imported1", //$NON-NLS-1$
                        AspectAdmin.ASPECT_APPLY_POLICY_FALSE)).andStubReturn(
                null);
        EasyMock.expect(aspectAdmin.getAspectDefinition(requiredBundle1))
                .andStubReturn(new Definition());

        EasyMock.replay(mocks);
        AspectConfiguration resolvedAspects = resolver.resolveAspectsFor(
                bundle, bundleDescription);
        EasyMock.verify(mocks);

        assertNotNull(resolvedAspects);
        assertEquals(0, resolvedAspects.getAspectDefinitions().size());
        assertEquals("", resolvedAspects.getFingerprint());
    }

}
