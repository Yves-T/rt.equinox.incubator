/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.frameworkadmin.knopflerfish.internal;

import org.eclipse.equinox.configuratormanipulator.ConfiguratorManipulatorFactory;
import org.eclipse.equinox.frameworkadmin.FrameworkAdmin;
import org.eclipse.equinox.frameworkadmin.FrameworkAdminFactory;

public class KfFrameworkAdminFactoryImpl extends FrameworkAdminFactory {
	public FrameworkAdmin createFrameworkAdmin() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		String className = System.getProperty(ConfiguratorManipulatorFactory.SYSTEM_PROPERTY_KEY);
		if (className == null)
			return new KfFwAdminImpl();;
		return new KfFwAdminImpl(className);
	}

	//	public FrameworkAdmin createFrameworkAdmin() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
	//		return new EquinoxFwAdminImpl();;
	//		
	//	}

	//	public FrameworkAdmin createFrameworkAdmin(String className) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
	//		return new EquinoxFwAdminImpl(className);
	//	}
}
