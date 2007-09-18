/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.internal.security.provider;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class SecurityPlugin implements BundleActivator {

	public void start(BundleContext context) throws Exception {
		ProviderServiceListener.attachServiceListener(context);
	}

	public void stop(BundleContext context) throws Exception {
		// TBD anything to do here?  
	}

}
