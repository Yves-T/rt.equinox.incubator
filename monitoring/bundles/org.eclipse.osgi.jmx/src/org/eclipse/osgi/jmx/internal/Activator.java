/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.osgi.jmx.internal;

import org.osgi.framework.*;

/**
 * The activator for this bundle.
 * 
 * @since 1.0
 */
public class Activator implements BundleActivator {

	private static BundleContext context;

	/**
	 * The constructor.
	 */
	public Activator() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
	}

	/**
	 * @return The bundle context.
	 */
	public static BundleContext getBundleContext() {
		return context;
	}

	public static Bundle getBundle() {
		return context.getBundle();
	}
}
