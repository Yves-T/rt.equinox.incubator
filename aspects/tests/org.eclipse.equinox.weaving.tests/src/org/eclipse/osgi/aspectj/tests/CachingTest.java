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

    public void testBundleNamespaces() {
        testNamespaces(HELLO_CLASS);
    }

    public void testFragmentNamespaces() {
        testNamespaces(HELLOFRAGMENT_CLASS);
    }

    private void testNamespaces(String className) {
        Class helloClazz;

        TestMessageHandler.clear();
        helloClazz = runHello(className);
        assertNotWoven(helloClazz, TRACING_ASPECT);

        TestMessageHandler.clear();
        helloClazz = runHelloAndTracing(className);
        assertWoven(helloClazz, TRACING_ASPECT);

        TestMessageHandler.clear();
        helloClazz = runHelloAndTracing(className);
        assertNotWoven(helloClazz, TRACING_ASPECT);

        TestMessageHandler.clear();
        helloClazz = runSecondHello(className);
        assertNotWoven(helloClazz, TRACING_ASPECT);

        TestMessageHandler.clear();
        helloClazz = runSecondHelloAndTracing(className);
        assertWoven(helloClazz, TRACING_ASPECT);

        TestMessageHandler.clear();
        helloClazz = runSecondHelloAndTracing(className);
        assertNotWoven(helloClazz, TRACING_ASPECT);

        TestMessageHandler.clear();
        helloClazz = runHello(className);
        assertNotWoven(helloClazz, TRACING_ASPECT);
    }

    private Class runHello(String className) {
        Class clazz = null;
        Bundle bundle1 = installBundle(HELLO_BUNDLE, true);
        Bundle bundle2 = installBundle(HELLO_FRAGMENT, false);
        try {
            clazz = bundle1.loadClass(className);
            if (debug)
                System.out.println("? CachingTest.testHello() clazz=" + clazz
                        + "@" + Integer.toString(clazz.hashCode(), 16)
                        + ", loader=" + clazz.getClassLoader());
            assertNotSame(previousClazz, clazz);
            previousClazz = clazz;
            runMain(clazz, new String[] {});
        } catch (Exception ex) {
            fail(ex.toString());
        } finally {
            uninstallBundles(new Bundle[] { bundle1, bundle2 });
        }

        return clazz;
    }

    private Class runSecondHello(String className) {
        Class clazz = null;
        Bundle bundle1 = installBundle(SECOND_HELLO_BUNDLE, true);
        Bundle bundle2 = installBundle(SECOND_HELLO_FRAGMENT, false);
        try {
            clazz = bundle1.loadClass(className);
            if (debug)
                System.out.println("? CachingTest.testHello() clazz=" + clazz
                        + "@" + Integer.toString(clazz.hashCode(), 16)
                        + ", loader=" + clazz.getClassLoader());
            assertNotSame(previousClazz, clazz);
            previousClazz = clazz;
            runMain(clazz, new String[] {});
        } catch (Exception ex) {
            fail(ex.toString());
        } finally {
            uninstallBundles(new Bundle[] { bundle1, bundle2 });
        }

        return clazz;
    }

    private Class runHelloAndTracing(String className) {
        Class clazz = null;
        Bundle bundle2 = installBundle(TRACING_BUNDLE, true);
        Bundle bundle3 = installBundle(HELLOTRACING_FRAGMENT, false);
        Bundle bundle1 = installBundle(HELLO_BUNDLE, true);
        Bundle bundle4 = installBundle(HELLO_FRAGMENT, false);
        //		System.out.println("? CachingTest.testHelloAndTracing() host=" + Platform.getHosts(bundle3)[0].getSymbolicName());
        try {
            clazz = bundle1.loadClass(className);
            if (debug)
                System.out.println("? CachingTest.testHelloAndTracing() clazz="
                        + clazz + "@" + Integer.toString(clazz.hashCode(), 16)
                        + ", loader=" + clazz.getClassLoader());
            assertNotSame(previousClazz, clazz);
            previousClazz = clazz;
            runMain(clazz, new String[] {});
        } catch (Exception ex) {
            fail(ex.toString());
        } finally {
            uninstallBundles(new Bundle[] { bundle1, bundle3, bundle2, bundle4 });
        }

        return clazz;
    }

    private Class runSecondHelloAndTracing(String className) {
        Class clazz = null;
        Bundle bundle2 = installBundle(TRACING_BUNDLE, true);
        Bundle bundle3 = installBundle(HELLOTRACING_FRAGMENT, false);
        Bundle bundle1 = installBundle(SECOND_HELLO_BUNDLE, true);
        Bundle bundle4 = installBundle(SECOND_HELLO_FRAGMENT, false);
        //      System.out.println("? CachingTest.testHelloAndTracing() host=" + Platform.getHosts(bundle3)[0].getSymbolicName());
        try {
            clazz = bundle1.loadClass(className);
            if (debug)
                System.out.println("? CachingTest.testHelloAndTracing() clazz="
                        + clazz + "@" + Integer.toString(clazz.hashCode(), 16)
                        + ", loader=" + clazz.getClassLoader());
            assertNotSame(previousClazz, clazz);
            previousClazz = clazz;
            runMain(clazz, new String[] {});
        } catch (Exception ex) {
            fail(ex.toString());
        } finally {
            uninstallBundles(new Bundle[] { bundle1, bundle3, bundle2, bundle4 });
        }

        return clazz;
    }

    private void runMain(Class clazz, String[] args) {
        try {
            Object[] parameters = new Object[] { args };
            Class[] parameterTypes = new Class[] { args.getClass() };
            Method mainMethod = clazz.getMethod("main", parameterTypes);
            mainMethod.invoke(null, parameters);
        } catch (Exception ex) {
            ex.printStackTrace();
            fail(ex.toString());
        }
    }

    protected static Bundle installBundle(String name, boolean start) {
        if (CachingTest.debug)
            System.out.println("> CachingTest.installBundle() name=" + name);
        Bundle bundle = null;

        try {
            bundle = TestsActivator.installBundle(name, start);
            //			installedBundles.remove(bundle);
        } catch (Exception ex) {
            fail(ex.toString());
        }

        if (CachingTest.debug)
            System.out.println("< CachingTest.installBundle() bundle=" + bundle
                    + ", state=" + bundle.getState());
        return bundle;
    }

    protected void uninstallBundles(Bundle[] bundles) {
        if (debug)
            System.out.println("> CachingTest.uninstallBundles() bundles="
                    + Arrays.asList(bundles));

        try {
            TestsActivator.uninstallBundles(bundles);
            //			installedBundles.remove(bundle);
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
        super.tearDown();

        //		assertTrue("Unexpected bundles: " + installedBundles.toString(),installedBundles.isEmpty());
    }
}
