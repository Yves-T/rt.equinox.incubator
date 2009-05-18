/*******************************************************************************
 * Copyright (c) 2008, 2009 Martin Lippert and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Martin Lippert            initial implementation      
 *   Martin Lippert            fragment handling fixed
 *******************************************************************************/

package org.eclipse.osgi.aspectj.tests;

import java.util.Hashtable;
import java.util.List;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.eclipse.equinox.service.weaving.ISupplementerRegistry;
import org.eclipse.equinox.service.weaving.Supplementer;
import org.eclipse.equinox.weaving.adaptors.IWeavingAdaptor;
import org.eclipse.equinox.weaving.hooks.IAdaptorProvider;
import org.eclipse.equinox.weaving.hooks.SupplementerRegistry;
import org.eclipse.osgi.util.ManifestElement;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.packageadmin.PackageAdmin;

/**
 * several test cases for the supplementer registry
 */
@SuppressWarnings("nls")
public class SupplementerRegistryTest extends TestCase {

    private Bundle bundle;

    private BundleContext context;

    private Object[] mocks;

    private Bundle otherBundle;

    private ISupplementerRegistry registry;

    private Bundle supplementedBundle1;

    private Bundle supplementedBundle2;

    private Bundle supplementedBundle3;

    private Bundle supplementerBundle1;

    private Bundle supplementerBundle2;

    private Bundle supplementerBundle3;

    private IAdaptorProvider adaptorProvider;

    private PackageAdmin packageAdmin;

    /**
     * test the supplementer registry by adding a simple bundle without any
     * supplementing to it
     */
    public void testSupplementerRegistryAddSimpleBundle() {
        EasyMock.expect(bundle.getHeaders()).andStubReturn(new Hashtable());

        EasyMock.replay(mocks);

        registry.addBundle(bundle);
        Supplementer[] supplementers = registry.getSupplementers(bundle);
        assertNotNull(supplementers);
        assertEquals(0, supplementers.length);

        supplementers = registry.getSupplementers(otherBundle);
        assertNotNull(supplementers);
        assertEquals(0, supplementers.length);

        EasyMock.verify(mocks);
    }

    /**
     * test an empty supplementer registry
     * 
     * @throws Exception
     */
    public void testSupplementerRegistryEmpty() throws Exception {

        EasyMock.replay(mocks);

        Supplementer[] supplementers = registry.getSupplementers(0);
        assertNotNull(supplementers);
        assertEquals(0, supplementers.length);

        supplementers = registry.getSupplementers(bundle);
        assertNotNull(supplementers);
        assertEquals(0, supplementers.length);

        EasyMock.verify(mocks);

        final ManifestElement[] imports = ManifestElement.parseHeader(
                "Import-Package", "org.test1,\n org.test2");
        final ManifestElement[] exports = ManifestElement.parseHeader(
                "Export-Package", "org.test3,\n org.test4");
        final List<Supplementer> possibleSupplementers = registry
                .getMatchingSupplementers("symbolicName", imports, exports);
        assertNotNull(possibleSupplementers);
        assertEquals(0, possibleSupplementers.size());
    }

    /**
     * test the supplementer registry with a supplemented bundle
     * 
     * @throws Exception
     */
    public void testSupplementerRegistryWithCombinedSupplementer()
            throws Exception {
        Hashtable headers = new Hashtable();
        headers.put("Eclipse-SupplementImporter", "test.import1");
        headers.put("Eclipse-SupplementExporter", "test.export1");
        headers.put("Eclipse-SupplementBundle",
                "symbolic-name-supplementedBundle1");
        EasyMock.expect(bundle.getHeaders()).andStubReturn(headers);
        EasyMock.expect(bundle.getSymbolicName()).andStubReturn("supplementer");
        EasyMock.expect(context.getBundles())
                .andReturn(new Bundle[] { bundle });

        EasyMock.expect(supplementedBundle1.getHeaders()).andStubReturn(
                new Hashtable());

        headers = new Hashtable();
        headers.put("Export-Package", "test.export1");
        EasyMock.expect(supplementedBundle2.getHeaders())
                .andStubReturn(headers);

        headers = new Hashtable();
        headers.put("Import-Package", "test.import1");
        EasyMock.expect(supplementedBundle3.getHeaders())
                .andStubReturn(headers);

        EasyMock.replay(mocks);

        registry.addBundle(bundle);
        registry.addBundle(supplementedBundle1);
        registry.addBundle(supplementedBundle2);
        registry.addBundle(supplementedBundle3);

        Supplementer[] supplementers = registry
                .getSupplementers(supplementedBundle1);
        assertSame(bundle, supplementers[0].getSupplementerBundle());
        supplementers = registry.getSupplementers(supplementedBundle2);
        assertSame(bundle, supplementers[0].getSupplementerBundle());
        supplementers = registry.getSupplementers(supplementedBundle3);
        assertSame(bundle, supplementers[0].getSupplementerBundle());

        EasyMock.verify(mocks);
    }

    /**
     * test different supplementers and removed supplementers
     * 
     * @throws Exception
     */
    public void testSupplementerRegistryWithDifferentRemovedSupplementers()
            throws Exception {
        Hashtable headers = new Hashtable();
        headers.put("Eclipse-SupplementImporter", "test.import1");
        EasyMock.expect(supplementerBundle1.getHeaders())
                .andStubReturn(headers);

        headers = new Hashtable();
        headers.put("Eclipse-SupplementExporter", "test.export1");
        EasyMock.expect(supplementerBundle2.getHeaders())
                .andStubReturn(headers);

        headers = new Hashtable();
        headers.put("Eclipse-SupplementBundle",
                "symbolic-name-supplementedBundle*");
        EasyMock.expect(supplementerBundle3.getHeaders())
                .andStubReturn(headers);

        EasyMock.expect(context.getBundles()).andStubReturn(
                new Bundle[] { supplementerBundle1, supplementerBundle2,
                        supplementerBundle3 });

        headers = new Hashtable();
        headers.put("Import-Package", "test.import1");
        headers.put("Export-Package", "test.export1");
        EasyMock.expect(supplementedBundle1.getHeaders())
                .andStubReturn(headers);

        headers = new Hashtable();
        EasyMock.expect(supplementedBundle2.getHeaders())
                .andStubReturn(headers);

        EasyMock.expect(supplementedBundle1.getState()).andStubReturn(
                Bundle.RESOLVED);
        EasyMock.expect(supplementedBundle2.getState()).andStubReturn(
                Bundle.RESOLVED);

        packageAdmin.refreshPackages(EasyMock
                .aryEq(new Bundle[] { supplementedBundle1 }));
        packageAdmin.refreshPackages(EasyMock.aryEq(new Bundle[] {
                supplementedBundle1, supplementedBundle2 }));

        IWeavingAdaptor adaptor = EasyMock
                .createNiceMock(IWeavingAdaptor.class);
        EasyMock.expect(adaptorProvider.getAdaptor(6l)).andStubReturn(adaptor);
        EasyMock.expect(adaptorProvider.getAdaptor(7l)).andStubReturn(adaptor);

        EasyMock.replay(mocks);

        registry.addBundle(supplementerBundle1);
        registry.addBundle(supplementerBundle2);
        registry.addBundle(supplementerBundle3);
        registry.addBundle(supplementedBundle1);
        registry.addBundle(supplementedBundle2);

        registry.removeBundle(supplementerBundle1);
        registry.removeBundle(supplementerBundle3);

        Supplementer[] supplementers = registry
                .getSupplementers(supplementedBundle1);
        assertEquals(1, supplementers.length);
        assertFalse(containsSupplementer(supplementers, supplementerBundle1));
        assertTrue(containsSupplementer(supplementers, supplementerBundle2));
        assertFalse(containsSupplementer(supplementers, supplementerBundle3));

        supplementers = registry.getSupplementers(supplementedBundle2);
        assertEquals(0, supplementers.length);
        assertFalse(containsSupplementer(supplementers, supplementerBundle1));
        assertFalse(containsSupplementer(supplementers, supplementerBundle2));
        assertFalse(containsSupplementer(supplementers, supplementerBundle3));

        EasyMock.verify(mocks);
    }

    private boolean containsSupplementer(Supplementer[] supplementers,
            Bundle supplementer) {
        for (int i = 0; i < supplementers.length; i++) {
            if (supplementers[i].getSupplementerBundle() == supplementer)
                return true;
        }
        return false;
    }

    /**
     * test different supplementers and different supplemented bundles
     * 
     * @throws Exception
     */
    public void testSupplementerRegistryWithDifferentSupplementers()
            throws Exception {
        Hashtable headers = new Hashtable();
        headers.put("Eclipse-SupplementImporter", "test.import1");
        EasyMock.expect(supplementerBundle1.getHeaders())
                .andStubReturn(headers);

        headers = new Hashtable();
        headers.put("Eclipse-SupplementExporter", "test.export1");
        EasyMock.expect(supplementerBundle2.getHeaders())
                .andStubReturn(headers);

        headers = new Hashtable();
        headers.put("Eclipse-SupplementBundle",
                "symbolic-name-supplementedBundle*");
        EasyMock.expect(supplementerBundle3.getHeaders())
                .andStubReturn(headers);

        EasyMock.expect(context.getBundles()).andStubReturn(
                new Bundle[] { supplementerBundle1, supplementerBundle2,
                        supplementerBundle3 });

        headers = new Hashtable();
        headers.put("Import-Package", "test.import1");
        headers.put("Export-Package", "test.export1");
        EasyMock.expect(supplementedBundle1.getHeaders())
                .andStubReturn(headers);

        headers = new Hashtable();
        EasyMock.expect(supplementedBundle2.getHeaders())
                .andStubReturn(headers);

        EasyMock.replay(mocks);

        registry.addBundle(supplementerBundle1);
        registry.addBundle(supplementerBundle2);
        registry.addBundle(supplementerBundle3);
        registry.addBundle(supplementedBundle1);
        registry.addBundle(supplementedBundle2);

        Supplementer[] supplementers = registry
                .getSupplementers(supplementedBundle1);
        assertEquals(3, supplementers.length);
        assertTrue(containsSupplementer(supplementers, supplementerBundle1));
        assertTrue(containsSupplementer(supplementers, supplementerBundle2));
        assertTrue(containsSupplementer(supplementers, supplementerBundle3));

        supplementers = registry.getSupplementers(supplementedBundle2);
        assertEquals(1, supplementers.length);
        assertFalse(containsSupplementer(supplementers, supplementerBundle1));
        assertFalse(containsSupplementer(supplementers, supplementerBundle2));
        assertTrue(containsSupplementer(supplementers, supplementerBundle3));

        EasyMock.verify(mocks);
    }

    public void testSupplementerRegistryWithRemovedSupplementer()
            throws Exception {
        final Hashtable headers = new Hashtable();
        headers.put("Eclipse-SupplementBundle", "test.bundle1");
        EasyMock.expect(bundle.getHeaders()).andStubReturn(headers);
        EasyMock.expect(context.getBundles())
                .andReturn(new Bundle[] { bundle });

        EasyMock.replay(mocks);

        registry.addBundle(bundle);
        registry.removeBundle(bundle);

        final ManifestElement[] imports = ManifestElement.parseHeader(
                "Import-Package", "org.test1,\n org.test2");
        final ManifestElement[] exports = ManifestElement.parseHeader(
                "Export-Package", "org.test3,\n org.test4");
        final List<Supplementer> supplementers = registry
                .getMatchingSupplementers("test.bundle1", imports, exports);
        assertNotNull(supplementers);
        assertEquals(0, supplementers.size());

        EasyMock.verify(mocks);
    }

    /**
     * test the supplementer registry with a supplemented bundle
     * 
     * @throws Exception
     */
    public void testSupplementerRegistryWithSupplementedBundle()
            throws Exception {
        final Hashtable headers = new Hashtable();
        headers.put("Eclipse-SupplementBundle",
                "symbolic-name-supplementedBundle1");
        EasyMock.expect(bundle.getHeaders()).andStubReturn(headers);
        EasyMock.expect(context.getBundles())
                .andReturn(new Bundle[] { bundle });

        EasyMock.expect(supplementedBundle1.getHeaders()).andStubReturn(
                new Hashtable());

        EasyMock.replay(mocks);

        registry.addBundle(bundle);
        registry.addBundle(supplementedBundle1);

        final Supplementer[] supplementers = registry
                .getSupplementers(supplementedBundle1);
        assertSame(bundle, supplementers[0].getSupplementerBundle());

        EasyMock.verify(mocks);
    }

    /**
     * test the supplementer registry with a supplemented bundle
     * 
     * @throws Exception
     */
    public void testSupplementerRegistryWithSupplementedExporter()
            throws Exception {
        Hashtable headers = new Hashtable();
        headers.put("Eclipse-SupplementExporter", "test.package1");
        EasyMock.expect(bundle.getHeaders()).andStubReturn(headers);
        EasyMock.expect(context.getBundles())
                .andReturn(new Bundle[] { bundle });

        headers = new Hashtable();
        headers.put("Export-Package", "test.package1");
        EasyMock.expect(supplementedBundle1.getHeaders())
                .andStubReturn(headers);

        EasyMock.replay(mocks);

        registry.addBundle(bundle);
        registry.addBundle(supplementedBundle1);

        final Supplementer[] supplementers = registry
                .getSupplementers(supplementedBundle1);
        assertSame(bundle, supplementers[0].getSupplementerBundle());

        EasyMock.verify(mocks);
    }

    /**
     * test the supplementer registry with a supplemented bundle
     * 
     * @throws Exception
     */
    public void testSupplementerRegistryWithSupplementedImporter()
            throws Exception {
        Hashtable headers = new Hashtable();
        headers.put("Eclipse-SupplementImporter", "test.package1");
        EasyMock.expect(bundle.getHeaders()).andStubReturn(headers);
        EasyMock.expect(context.getBundles())
                .andReturn(new Bundle[] { bundle });

        headers = new Hashtable();
        headers.put("Import-Package", "test.package1");
        EasyMock.expect(supplementedBundle1.getHeaders())
                .andStubReturn(headers);

        EasyMock.replay(mocks);

        registry.addBundle(bundle);
        registry.addBundle(supplementedBundle1);

        final Supplementer[] supplementers = registry
                .getSupplementers(supplementedBundle1);
        assertSame(bundle, supplementers[0].getSupplementerBundle());

        EasyMock.verify(mocks);
    }

    /**
     * test the supplementer registry by adding a supplementing bundle to it
     * 
     * @throws Exception
     */
    public void testSupplementerRegistryWithSupplementer() throws Exception {
        final Hashtable headers = new Hashtable();
        headers.put("Eclipse-SupplementBundle", "test.bundle1");
        EasyMock.expect(bundle.getHeaders()).andStubReturn(headers);
        EasyMock.expect(context.getBundles())
                .andReturn(new Bundle[] { bundle });

        EasyMock.replay(mocks);

        registry.addBundle(bundle);
        final ManifestElement[] imports = ManifestElement.parseHeader(
                "Import-Package", "org.test1,\n org.test2");
        final ManifestElement[] exports = ManifestElement.parseHeader(
                "Export-Package", "org.test3,\n org.test4");
        final List<Supplementer> supplementers = registry
                .getMatchingSupplementers("test.bundle1", imports, exports);
        assertNotNull(supplementers);
        assertEquals(1, supplementers.size());
        assertEquals(bundle, supplementers.get(0).getSupplementerBundle());

        EasyMock.verify(mocks);
    }

    public void testSupplementerRegistryWithWildcards() throws Exception {
        Hashtable headers = new Hashtable();
        headers = new Hashtable();
        headers.put("Eclipse-SupplementBundle",
                "symbolic-name-supplementedBundle*");
        EasyMock.expect(supplementerBundle1.getHeaders())
                .andStubReturn(headers);

        headers = new Hashtable();
        headers.put("Eclipse-SupplementBundle", "symbolic-name-supplemented*");
        EasyMock.expect(supplementerBundle2.getHeaders())
                .andStubReturn(headers);

        EasyMock.expect(context.getBundles()).andStubReturn(
                new Bundle[] { supplementerBundle1, supplementerBundle2 });

        headers = new Hashtable();
        EasyMock.expect(supplementedBundle1.getHeaders())
                .andStubReturn(headers);

        headers = new Hashtable();
        EasyMock.expect(supplementedBundle2.getHeaders())
                .andStubReturn(headers);

        headers = new Hashtable();
        EasyMock.expect(supplementedBundle3.getHeaders())
                .andStubReturn(headers);

        EasyMock.replay(mocks);

        registry.addBundle(supplementerBundle1);
        registry.addBundle(supplementerBundle2);
        registry.addBundle(supplementedBundle1);
        registry.addBundle(supplementedBundle2);
        registry.addBundle(supplementedBundle3);

        Supplementer[] supplementers = registry
                .getSupplementers(supplementedBundle1);
        assertEquals(2, supplementers.length);
        assertTrue(containsSupplementer(supplementers, supplementerBundle1));
        assertTrue(containsSupplementer(supplementers, supplementerBundle2));

        supplementers = registry.getSupplementers(supplementedBundle2);
        assertEquals(2, supplementers.length);
        assertTrue(containsSupplementer(supplementers, supplementerBundle1));
        assertTrue(containsSupplementer(supplementers, supplementerBundle2));

        supplementers = registry.getSupplementers(supplementedBundle3);
        assertEquals(0, supplementers.length);
        assertFalse(containsSupplementer(supplementers, supplementerBundle1));
        assertFalse(containsSupplementer(supplementers, supplementerBundle2));

        EasyMock.verify(mocks);
    }

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        adaptorProvider = EasyMock.createNiceMock(IAdaptorProvider.class);
        context = EasyMock.createNiceMock(BundleContext.class);
        bundle = EasyMock.createMock(Bundle.class);
        otherBundle = EasyMock.createMock(Bundle.class);
        supplementerBundle1 = EasyMock.createMock(Bundle.class);
        supplementerBundle2 = EasyMock.createMock(Bundle.class);
        supplementerBundle3 = EasyMock.createMock(Bundle.class);
        supplementedBundle1 = EasyMock.createMock(Bundle.class);
        supplementedBundle2 = EasyMock.createMock(Bundle.class);
        supplementedBundle3 = EasyMock.createMock(Bundle.class);

        packageAdmin = EasyMock.createMock(PackageAdmin.class);

        EasyMock.expect(packageAdmin.getHosts(bundle)).andStubReturn(null);
        EasyMock.expect(packageAdmin.getHosts(supplementedBundle1))
                .andStubReturn(null);
        EasyMock.expect(packageAdmin.getHosts(supplementedBundle2))
                .andStubReturn(null);
        EasyMock.expect(packageAdmin.getHosts(supplementedBundle3))
                .andStubReturn(null);
        EasyMock.expect(packageAdmin.getHosts(supplementerBundle1))
                .andStubReturn(null);
        EasyMock.expect(packageAdmin.getHosts(supplementerBundle2))
                .andStubReturn(null);
        EasyMock.expect(packageAdmin.getHosts(supplementerBundle3))
                .andStubReturn(null);

        registry = new SupplementerRegistry(adaptorProvider);
        registry.setBundleContext(context);
        registry.setPackageAdmin(packageAdmin);

        EasyMock.expect(bundle.getBundleId()).andStubReturn(1l);
        EasyMock.expect(otherBundle.getBundleId()).andStubReturn(2l);
        EasyMock.expect(supplementerBundle1.getBundleId()).andStubReturn(3l);
        EasyMock.expect(supplementerBundle2.getBundleId()).andStubReturn(4l);
        EasyMock.expect(supplementerBundle3.getBundleId()).andStubReturn(5l);
        EasyMock.expect(supplementedBundle1.getBundleId()).andStubReturn(6l);
        EasyMock.expect(supplementedBundle2.getBundleId()).andStubReturn(7l);
        EasyMock.expect(supplementedBundle3.getBundleId()).andStubReturn(8l);

        EasyMock.expect(bundle.getSymbolicName()).andStubReturn(
                "symbolic-name-bundle");
        EasyMock.expect(otherBundle.getSymbolicName()).andStubReturn(
                "symbolic-name-otherBundle");
        EasyMock.expect(supplementerBundle1.getSymbolicName()).andStubReturn(
                "symbolic-name-supplementerBundle1");
        EasyMock.expect(supplementerBundle2.getSymbolicName()).andStubReturn(
                "symbolic-name-supplementerBundle2");
        EasyMock.expect(supplementerBundle3.getSymbolicName()).andStubReturn(
                "symbolic-name-supplementerBundle3");
        EasyMock.expect(supplementedBundle1.getSymbolicName()).andStubReturn(
                "symbolic-name-supplementedBundle1");
        EasyMock.expect(supplementedBundle2.getSymbolicName()).andStubReturn(
                "symbolic-name-supplementedBundle2");
        EasyMock.expect(supplementedBundle3.getSymbolicName()).andStubReturn(
                "different-symbolic-name");

        mocks = new Object[] { adaptorProvider, packageAdmin, context, bundle,
                otherBundle, supplementedBundle1, supplementedBundle2,
                supplementedBundle3, supplementerBundle1, supplementerBundle2,
                supplementerBundle3 };
    }

}
