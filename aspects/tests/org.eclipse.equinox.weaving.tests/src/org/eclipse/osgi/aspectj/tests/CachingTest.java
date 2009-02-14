/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Matthew Webster           initial implementation      
 *   Martin Lippert            extracted caching service factory
 *******************************************************************************/

package org.eclipse.osgi.aspectj.tests;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.aspectj.aunit.TestMessageHandler;
import org.aspectj.aunit.WeavingTestCase;
import org.eclipse.equinox.service.weaving.ICachingServiceFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;

public class CachingTest extends WeavingTestCase {

    private final static String HELLO_BUNDLE = "demo.hello";

    private final static String HELLO_FRAGMENT = "demo.hello.fragment";

    private final static String HELLOTRACING_FRAGMENT = "demo.hello.tracing";

    private final static String TRACING_BUNDLE = "demo.tracing";

    private final static String SECOND_HELLO_BUNDLE = "demo.hello_2.0.0";

    private final static String SECOND_HELLO_FRAGMENT = "demo.hello.fragment_2.0.0";

    private final static String HELLO_CLASS = "hello.HelloWorld";

    private final static String HELLOFRAGMENT_CLASS = "hello.HelloFragment";

    private final static String TRACING_ASPECT = "demo.hello.tracing.HelloWorldTracing";

    public final static boolean debug = Boolean
            .getBoolean("org.eclipse.osgi.aspectj.tests.CachingTest.debug");

    static BundleContext context = TestsActivator.getContext();

    private static Class previousClazz = null;

    private Bundle[] installedBundles;

    //	private List installedBundles = new LinkedList();

    public void testCachingService() {
        ServiceReference ref = context
                .getServiceReference(ICachingServiceFactory.class.getName());
        assertNotNull("Cannot find caching service", ref);
        ICachingServiceFactory cachingServiceFactory = (ICachingServiceFactory) context
                .getService(ref);
        assertNotNull("Cannot instantiate caching service",
                cachingServiceFactory);
    }

    public void testBundleNoWeaveNoCache() {
        testNoWeaveNoCache(HELLO_CLASS);
    }

    public void testFragmentNoWeaveNoCache() {
        testNoWeaveNoCache(HELLOFRAGMENT_CLASS);
    }

    private void testNoWeaveNoCache(String className) {
        TestMessageHandler.clear();

        Bundle bundle1 = installBundle(HELLO_BUNDLE, true);
        Bundle bundle2 = installBundle(HELLO_FRAGMENT, false);
        installedBundles = new Bundle[] { bundle1, bundle2 };

        Class<?> helloClazz = runHello(bundle1, className);

        assertNotWoven(helloClazz, TRACING_ASPECT);
    }

    public void testBundleFirstWeaveNoCache() {
        testFirstWeaveNoCache(HELLO_CLASS);
    }

    public void testFragmentFirstWeaveNoCache() {
        testFirstWeaveNoCache(HELLOFRAGMENT_CLASS);
    }

    private void testFirstWeaveNoCache(String className) {
        TestMessageHandler.clear();

        Bundle bundle2 = installBundle(TRACING_BUNDLE, true);
        Bundle bundle3 = installBundle(HELLOTRACING_FRAGMENT, false);
        Bundle bundle1 = installBundle(HELLO_BUNDLE, true);
        Bundle bundle4 = installBundle(HELLO_FRAGMENT, false);
        installedBundles = new Bundle[] { bundle1, bundle2, bundle3, bundle4 };

        Class<?> helloClazz = runHello(bundle1, className);

        assertWoven(helloClazz, TRACING_ASPECT);
    }

    public void testBundleSecondLoadCached() throws InterruptedException {
        testSecondLoadCached(HELLO_CLASS);
    }

    public void testFragmentSecondLoadCached() throws InterruptedException {
        testSecondLoadCached(HELLOFRAGMENT_CLASS);
    }

    private void testSecondLoadCached(String className)
            throws InterruptedException {
        Bundle bundle2 = installBundle(TRACING_BUNDLE, true);
        Bundle bundle3 = installBundle(HELLOTRACING_FRAGMENT, false);
        Bundle bundle1 = installBundle(HELLO_BUNDLE, true);
        Bundle bundle4 = installBundle(HELLO_FRAGMENT, false);
        installedBundles = new Bundle[] { bundle1, bundle2, bundle3, bundle4 };

        runHello(bundle1, className);

        updateBundles(new Bundle[] { bundle1, bundle4 });
        Thread.sleep(2000);

        TestMessageHandler.clear();
        Class<?> helloClazz = runHello(bundle1, className);
        assertNotWoven(helloClazz, TRACING_ASPECT);
    }

    public void testBundleDifferentVersionNoWeavingNoCache() {
        testDifferentVersionNoWeavingNoCache(HELLO_CLASS);
    }

    public void testFragmentDifferentVersionNoWeavingNoCache() {
        testDifferentVersionNoWeavingNoCache(HELLOFRAGMENT_CLASS);
    }

    private void testDifferentVersionNoWeavingNoCache(String className) {
        TestMessageHandler.clear();

        Bundle bundle1 = installBundle(SECOND_HELLO_BUNDLE, true);
        Bundle bundle2 = installBundle(SECOND_HELLO_FRAGMENT, false);
        installedBundles = new Bundle[] { bundle1, bundle2 };

        Class<?> helloClazz = runHello(bundle1, className);

        assertNotWoven(helloClazz, TRACING_ASPECT);
    }

    public void testBundleDifferentVersionFirstWeaveNoCache() {
        testDifferentVersionFirstWeaveNoCache(HELLO_CLASS);
    }

    public void testFragmentDifferentVersionFirstWeaveNoCache() {
        testDifferentVersionFirstWeaveNoCache(HELLOFRAGMENT_CLASS);
    }

    private void testDifferentVersionFirstWeaveNoCache(String className) {
        TestMessageHandler.clear();

        Bundle bundle2 = installBundle(TRACING_BUNDLE, true);
        Bundle bundle3 = installBundle(HELLOTRACING_FRAGMENT, false);
        Bundle bundle1 = installBundle(SECOND_HELLO_BUNDLE, true);
        Bundle bundle4 = installBundle(SECOND_HELLO_FRAGMENT, false);
        installedBundles = new Bundle[] { bundle1, bundle2, bundle3, bundle4 };

        Class<?> helloClazz = runHello(bundle1, className);

        assertWoven(helloClazz, TRACING_ASPECT);
    }

    public void testBundleDifferentVersionSecondLoadCached()
            throws InterruptedException {
        testDifferentVersionSecondLoadCached(HELLO_CLASS);
    }

    public void testFragmentDifferentVersionSecondLoadCached()
            throws InterruptedException {
        testDifferentVersionSecondLoadCached(HELLOFRAGMENT_CLASS);
    }

    private void testDifferentVersionSecondLoadCached(String className)
            throws InterruptedException {
        Bundle bundle2 = installBundle(TRACING_BUNDLE, true);
        Bundle bundle3 = installBundle(HELLOTRACING_FRAGMENT, false);
        Bundle bundle1 = installBundle(SECOND_HELLO_BUNDLE, true);
        Bundle bundle4 = installBundle(SECOND_HELLO_FRAGMENT, false);
        installedBundles = new Bundle[] { bundle1, bundle2, bundle3, bundle4 };

        runHello(bundle1, className);

        updateBundles(new Bundle[] { bundle1, bundle4 });
        Thread.sleep(2000);

        TestMessageHandler.clear();
        Class<?> helloClazz = runHello(bundle1, className);
        assertNotWoven(helloClazz, TRACING_ASPECT);
    }

    private Class<?> runHello(Bundle bundle, String className) {
        Class<?> clazz = null;

        try {
            clazz = bundle.loadClass(className);
            if (debug)
                System.out.println("? CachingTest.testHello() clazz=" + clazz
                        + "@" + Integer.toString(clazz.hashCode(), 16)
                        + ", loader=" + clazz.getClassLoader());
            assertNotSame(previousClazz, clazz);
            previousClazz = clazz;
            runMain(clazz, new String[] {});
        } catch (Exception ex) {
            fail(ex.toString());
        }

        return clazz;
    }

    private void runMain(Class<?> clazz, String[] args) {
        try {
            Object[] parameters = new Object[] { args };
            Class<?>[] parameterTypes = new Class[] { args.getClass() };
            Method mainMethod = clazz.getMethod("main", parameterTypes);
            mainMethod.invoke(null, parameters);
        } catch (Exception ex) {
            ex.printStackTrace();
            fail(ex.toString());
        }
    }

    private Bundle installBundle(String name, boolean start) {
        if (CachingTest.debug)
            System.out.println("> CachingTest.installBundle() name=" + name);
        Bundle bundle = null;

        try {
            bundle = TestsActivator.installBundle(name, start);
        } catch (Exception ex) {
            fail(ex.toString());
        }

        if (CachingTest.debug)
            System.out.println("< CachingTest.installBundle() bundle=" + bundle
                    + ", state=" + bundle.getState());
        return bundle;
    }

    private void updateBundles(Bundle[] bundles) {
        if (debug)
            System.out.println("> CachingTest.refreshBundles() bundles="
                    + Arrays.asList(bundles));

        try {
            TestsActivator.refreshPackages(bundles);
        } catch (BundleException ex) {
            fail(ex.toString());
        }

        if (debug)
            System.out.println("< CachingTest.refreshBundles() states="
                    + getBundleStates(bundles));
    }

    private void uninstallBundles(Bundle[] bundles) {
        if (debug)
            System.out.println("> CachingTest.uninstallBundles() bundles="
                    + Arrays.asList(bundles));

        try {
            TestsActivator.uninstallBundles(bundles);
        } catch (BundleException ex) {
            fail(ex.toString());
        }

        if (debug)
            System.out.println("< CachingTest.uninstallBundles() states="
                    + getBundleStates(bundles));
    }

    private List getBundleStates(Bundle[] bundles) {
        List states = new LinkedList();
        for (int i = 0; i < bundles.length; i++) {
            states.add(new Integer(bundles[i].getState()));
        }
        return states;
    }

    protected void tearDown() throws Exception {
        if (installedBundles != null && installedBundles.length > 0) {
            uninstallBundles(installedBundles);
            installedBundles = null;
        }
        super.tearDown();
    }

}
