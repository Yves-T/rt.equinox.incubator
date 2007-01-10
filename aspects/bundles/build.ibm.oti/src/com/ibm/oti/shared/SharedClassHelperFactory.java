/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Matthew Webster           initial implementation      
 *******************************************************************************/

package com.ibm.oti.shared;

public interface SharedClassHelperFactory {
	
	public SharedClassURLHelper getURLHelper(ClassLoader classLoader) throws HelperAlreadyDefinedException;
	
}
