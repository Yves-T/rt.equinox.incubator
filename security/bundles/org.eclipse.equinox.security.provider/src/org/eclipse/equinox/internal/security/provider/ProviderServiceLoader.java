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

import org.osgi.framework.Bundle;

public class ProviderServiceLoader extends ClassLoader {

	private Bundle bundle;
	
	public ProviderServiceLoader(Bundle bundle) {
		this.bundle = bundle;
	}
	
	public Class loadClass(String name) throws ClassNotFoundException {
		return bundle.loadClass(name);
	}
}
