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
package org.eclipse.equinox.frameworkadmin.felix.internal;

import org.eclipse.equinox.frameworkadmin.*;

public class FelixFrameworkAdminFactoryImpl extends FrameworkAdminFactory {
	public FrameworkAdmin createFrameworkAdmin() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		String className = System.getProperty("org.eclipse.equinox.frameworkadmin.configuratorManipulatorFactory");
		if (className == null)
			return new FelixFwAdminImpl();;
		return new FelixFwAdminImpl(className);
	}

	//	public FrameworkAdmin createFrameworkAdmin() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
	//		return new felixFwAdminImpl();;
	//		
	//	}

	//	public FrameworkAdmin createFrameworkAdmin(String className) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
	//		return new FelixFwAdminImpl(className);
	//	}
}
