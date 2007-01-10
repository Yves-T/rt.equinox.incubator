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

import java.net.URL;

public interface SharedClassURLHelper {

	public byte[] findSharedClass(String partition, URL sourceFileURL, String name);
	
	public boolean storeSharedClass(String partition, URL sourceFileURL, Class clazz);
}
