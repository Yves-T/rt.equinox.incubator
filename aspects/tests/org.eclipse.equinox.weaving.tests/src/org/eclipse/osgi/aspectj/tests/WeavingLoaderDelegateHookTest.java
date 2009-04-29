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

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.eclipse.equinox.service.weaving.ISupplementerRegistry;
import org.eclipse.equinox.service.weaving.Supplementer;
import org.eclipse.equinox.weaving.hooks.WeavingLoaderDelegateHook;
import org.eclipse.osgi.framework.adaptor.BundleClassLoader;
import org.eclipse.osgi.framework.adaptor.BundleData;

public class WeavingLoaderDelegateHookTest extends TestCase {

    public void testNoSupplementersFound() throws Exception {
        ISupplementerRegistry supplementerRegistry = EasyMock
                .createMock(ISupplementerRegistry.class);
        BundleClassLoader loader = EasyMock.createMock(BundleClassLoader.class);
        BundleData bundleData = EasyMock.createMock(BundleData.class);

        EasyMock.expect(bundleData.getBundleID()).andReturn((long) 5);
        EasyMock.expect(supplementerRegistry.getSupplementers(5)).andReturn(
                new Supplementer[0]);

        WeavingLoaderDelegateHook hook = new WeavingLoaderDelegateHook(
                supplementerRegistry);

        EasyMock.replay(supplementerRegistry, loader, bundleData);

        Class result = hook.postFindClass("classname", loader, bundleData);
        assertNull(result);

        EasyMock.verify(supplementerRegistry, loader, bundleData);
    }

}
