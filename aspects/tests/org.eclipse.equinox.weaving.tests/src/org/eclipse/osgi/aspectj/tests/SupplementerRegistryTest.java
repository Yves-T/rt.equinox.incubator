/*******************************************************************************
 * Copyright (c) 2008 Martin Lippert and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Martin Lippert               initial implementation      
 *******************************************************************************/

package org.eclipse.osgi.aspectj.tests;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.eclipse.equinox.weaving.hooks.SupplementerRegistry;
import org.eclipse.osgi.util.ManifestElement;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * several test cases for the supplementer registry
 */
public class SupplementerRegistryTest extends TestCase {

    private SupplementerRegistry registry;

    private BundleContext context;

    private Bundle bundle;

    private Bundle otherBundle;

    private Object[] mocks;

    private Bundle supplementedBundle1;

    private Bundle supplementedBundle2;

    private Bundle supplementedBundle3;

    private Bundle supplementerBundle1;

    private Bundle supplementerBundle2;

    private Bundle supplementerBundle3;

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        registry = new SupplementerRegistry();

        context = EasyMock.createMock(BundleContext.class);
        bundle = EasyMock.createMock(Bundle.class);
        otherBundle = EasyMock.createMock(Bundle.class);
        supplementerBundle1 = EasyMock.createMock(Bundle.class);
        supplementerBundle2 = EasyMock.createMock(Bundle.class);
        supplementerBundle3 = EasyMock.createMock(Bundle.class);
        supplementedBundle1 = EasyMock.createMock(Bundle.class);
        supplementedBundle2 = EasyMock.createMock(Bundle.class);
        supplementedBundle3 = EasyMock.createMock(Bundle.class);
        registry.setBundleContext(context);

        mocks = new Object[] { context, bundle, otherBundle,
                supplementedBundle1, supplementedBundle2, supplementedBundle3,
                supplementerBundle1, supplementerBundle2, supplementerBundle3 };
    }

    /**
     * test an empty supplementer registry
     * 
     * @throws Exception
     */
    public void testSupplementerRegistryEmpty() throws Exception {
        EasyMock.expect(context.getBundle(0)).andReturn(bundle);

        EasyMock.replay(mocks);

        Bundle[] supplementers = registry.getSupplementers(0);
        assertNotNull(supplementers);
        assertEquals(0, supplementers.length);

        supplementers = registry.getSupplementers(bundle);
        assertNotNull(supplementers);
        assertEquals(0, supplementers.length);

        EasyMock.verify(mocks);

        ManifestElement[] imports = ManifestElement.parseHeader(
                "Import-Package", "org.test1,\n org.test2"); //$NON-NLS-1$ //$NON-NLS-2$
        ManifestElement[] exports = ManifestElement.parseHeader(
                "Export-Package", "org.test3,\n org.test4"); //$NON-NLS-1$ //$NON-NLS-2$
        List possibleSupplementers = registry.getSupplementers(
                "symbolicName", imports, exports); //$NON-NLS-1$
        assertNotNull(possibleSupplementers);
        assertEquals(0, possibleSupplementers.size());
    }

    /**
     * test the supplementer registry by adding a simple bundle without any
     * supplementing to it
     */
    public void testSupplementerRegistryAddSimpleBundle() {
        EasyMock.expect(bundle.getHeaders()).andStubReturn(new Hashtable());
        EasyMock.expect(bundle.getSymbolicName())
                .andStubReturn("symbolic-name");

        EasyMock.replay(mocks);

        registry.addBundle(bundle);
        Bundle[] supplementers = registry.getSupplementers(bundle);
        assertNotNull(supplementers);
        assertEquals(0, supplementers.length);

        supplementers = registry.getSupplementers(otherBundle);
        assertNotNull(supplementers);
        assertEquals(0, supplementers.length);

        EasyMock.verify(mocks);
    }

    /**
     * test the supplementer registry by adding a supplementing bundle to it
     * 
     * @throws Exception
     */
    public void testSupplementerRegistryWithSupplementer() throws Exception {
        Hashtable headers = new Hashtable();
        headers.put("Eclipse-SupplementBundle", "test.bundle1");
        EasyMock.expect(bundle.getHeaders()).andStubReturn(headers);
        EasyMock.expect(bundle.getSymbolicName()).andStubReturn("supplementer");
        EasyMock.expect(context.getBundles())
                .andReturn(new Bundle[] { bundle });

        EasyMock.replay(mocks);

        registry.addBundle(bundle);
        ManifestElement[] imports = ManifestElement.parseHeader(
                "Import-Package", "org.test1,\n org.test2"); //$NON-NLS-1$ //$NON-NLS-2$
        ManifestElement[] exports = ManifestElement.parseHeader(
                "Export-Package", "org.test3,\n org.test4"); //$NON-NLS-1$ //$NON-NLS-2$
        List supplementers = registry.getSupplementers("test.bundle1", imports,
                exports);
        assertNotNull(supplementers);
        assertEquals(1, supplementers.size());
        assertEquals("supplementer", supplementers.get(0));

        EasyMock.verify(mocks);
    }

    /**
     * test the supplementer registry with a supplemented bundle
     * 
     * @throws Exception
     */
    public void testSupplementerRegistryWithSupplementedBundle()
            throws Exception {
        Hashtable headers = new Hashtable();
        headers.put("Eclipse-SupplementBundle", "test.bundle1");
        EasyMock.expect(bundle.getHeaders()).andStubReturn(headers);
        EasyMock.expect(bundle.getSymbolicName()).andStubReturn("supplementer");
        EasyMock.expect(context.getBundles())
                .andReturn(new Bundle[] { bundle });

        EasyMock.expect(supplementedBundle1.getHeaders()).andStubReturn(
                new Hashtable());
        EasyMock.expect(supplementedBundle1.getSymbolicName()).andStubReturn(
                "test.bundle1");

        EasyMock.replay(mocks);

        registry.addBundle(bundle);
        registry.addBundle(supplementedBundle1);

        Bundle[] supplementers = registry.getSupplementers(supplementedBundle1);
        assertSame(bundle, supplementers[0]);

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
        EasyMock.expect(bundle.getSymbolicName()).andStubReturn("supplementer");
        EasyMock.expect(context.getBundles())
                .andReturn(new Bundle[] { bundle });

        headers = new Hashtable();
        headers.put("Import-Package", "test.package1");
        EasyMock.expect(supplementedBundle1.getHeaders())
                .andStubReturn(headers);
        EasyMock.expect(supplementedBundle1.getSymbolicName()).andStubReturn(
                "test.bundle1");

        EasyMock.replay(mocks);

        registry.addBundle(bundle);
        registry.addBundle(supplementedBundle1);

        Bundle[] supplementers = registry.getSupplementers(supplementedBundle1);
        assertSame(bundle, supplementers[0]);

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
        EasyMock.expect(bundle.getSymbolicName()).andStubReturn("supplementer");
        EasyMock.expect(context.getBundles())
                .andReturn(new Bundle[] { bundle });

        headers = new Hashtable();
        headers.put("Export-Package", "test.package1");
        EasyMock.expect(supplementedBundle1.getHeaders())
                .andStubReturn(headers);
        EasyMock.expect(supplementedBundle1.getSymbolicName()).andStubReturn(
                "test.bundle1");

        EasyMock.replay(mocks);

        registry.addBundle(bundle);
        registry.addBundle(supplementedBundle1);

        Bundle[] supplementers = registry.getSupplementers(supplementedBundle1);
        assertSame(bundle, supplementers[0]);

        EasyMock.verify(mocks);
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
        headers.put("Eclipse-SupplementBundle", "test.bundle1");
        EasyMock.expect(bundle.getHeaders()).andStubReturn(headers);
        EasyMock.expect(bundle.getSymbolicName()).andStubReturn("supplementer");
        EasyMock.expect(context.getBundles())
                .andReturn(new Bundle[] { bundle });

        EasyMock.expect(supplementedBundle1.getHeaders()).andStubReturn(
                new Hashtable());
        EasyMock.expect(supplementedBundle1.getSymbolicName()).andStubReturn(
                "test.bundle1");

        headers = new Hashtable();
        headers.put("Export-Package", "test.export1");
        EasyMock.expect(supplementedBundle2.getHeaders())
                .andStubReturn(headers);
        EasyMock.expect(supplementedBundle2.getSymbolicName()).andStubReturn(
                "test.bundle2");

        headers = new Hashtable();
        headers.put("Import-Package", "test.import1");
        EasyMock.expect(supplementedBundle3.getHeaders())
                .andStubReturn(headers);
        EasyMock.expect(supplementedBundle3.getSymbolicName()).andStubReturn(
                "test.bundle3");

        EasyMock.replay(mocks);

        registry.addBundle(bundle);
        registry.addBundle(supplementedBundle1);
        registry.addBundle(supplementedBundle2);
        registry.addBundle(supplementedBundle3);

        Bundle[] supplementers = registry.getSupplementers(supplementedBundle1);
        assertSame(bundle, supplementers[0]);
        supplementers = registry.getSupplementers(supplementedBundle2);
        assertSame(bundle, supplementers[0]);
        supplementers = registry.getSupplementers(supplementedBundle3);
        assertSame(bundle, supplementers[0]);

        EasyMock.verify(mocks);
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
        EasyMock.expect(supplementerBundle1.getSymbolicName()).andStubReturn(
                "supplementer1");

        headers = new Hashtable();
        headers.put("Eclipse-SupplementExporter", "test.export1");
        EasyMock.expect(supplementerBundle2.getHeaders())
                .andStubReturn(headers);
        EasyMock.expect(supplementerBundle2.getSymbolicName()).andStubReturn(
                "supplementer2");

        headers = new Hashtable();
        headers.put("Eclipse-SupplementBundle", "test.bundle1");
        EasyMock.expect(supplementerBundle3.getHeaders())
                .andStubReturn(headers);
        EasyMock.expect(supplementerBundle3.getSymbolicName()).andStubReturn(
                "supplementer3");

        EasyMock.expect(context.getBundles()).andStubReturn(
                new Bundle[] { supplementerBundle1, supplementerBundle2,
                        supplementerBundle3 });

        headers = new Hashtable();
        headers.put("Import-Package", "test.import1");
        headers.put("Export-Package", "test.export1");
        EasyMock.expect(supplementedBundle1.getHeaders())
                .andStubReturn(headers);
        EasyMock.expect(supplementedBundle1.getSymbolicName()).andStubReturn(
                "test.bundle1");

        EasyMock.replay(mocks);

        registry.addBundle(supplementerBundle1);
        registry.addBundle(supplementerBundle2);
        registry.addBundle(supplementerBundle3);
        registry.addBundle(supplementedBundle1);

        Bundle[] supplementers = registry.getSupplementers(supplementedBundle1);
        assertEquals(3, supplementers.length);
        assertTrue(Arrays.asList(supplementers).contains(supplementerBundle1));
        assertTrue(Arrays.asList(supplementers).contains(supplementerBundle2));
        assertTrue(Arrays.asList(supplementers).contains(supplementerBundle3));

        EasyMock.verify(mocks);
    }
}
