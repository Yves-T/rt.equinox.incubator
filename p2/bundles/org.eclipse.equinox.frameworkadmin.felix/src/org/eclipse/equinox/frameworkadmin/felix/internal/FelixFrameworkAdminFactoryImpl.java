/*******************************************************************************
 * Copyright (c) 2007, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.frameworkadmin.felix.internal;

import org.eclipse.equinox.internal.provisional.configuratormanipulator.ConfiguratorManipulatorFactory;
import org.eclipse.equinox.internal.provisional.frameworkadmin.FrameworkAdmin;
import org.eclipse.equinox.internal.provisional.frameworkadmin.FrameworkAdminFactory;

public class FelixFrameworkAdminFactoryImpl extends FrameworkAdminFactory {

	/* (non-Javadoc)
	 * @see org.eclipse.equinox.internal.provisional.frameworkadmin.FrameworkAdminFactory#createFrameworkAdmin()
	 */
	public FrameworkAdmin createFrameworkAdmin() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		String className = System.getProperty(ConfiguratorManipulatorFactory.SYSTEM_PROPERTY_KEY);
		if (className == null)
			return new FelixFwAdminImpl();
		return new FelixFwAdminImpl(className);
	}
}
