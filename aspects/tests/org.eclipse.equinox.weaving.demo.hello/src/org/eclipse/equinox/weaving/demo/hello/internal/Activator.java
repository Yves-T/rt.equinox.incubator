/*******************************************************************************
 * Copyright (c) 2008 Heiko Seeberger and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html.
 * 
 * Contributors:
 *     Heiko Seeberger - initial implementation
 ******************************************************************************/

package org.eclipse.equinox.weaving.demo.hello.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Just for demo: "Hello world!" activator.
 * 
 * @author Heiko Seeberger
 */
public class Activator implements BundleActivator {

    /**
     * Says "Hello world!".
     * 
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start(final BundleContext context) throws Exception {
        System.out.println("Hello world!");
    }

    /**
     * Says "Good bye world!".
     * 
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(final BundleContext context) throws Exception {
        System.out.println("Good bye world!");
    }
}
