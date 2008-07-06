/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   David Knibb               initial implementation      
 *******************************************************************************/

package org.eclipse.osgi.aspectj.tests;

/**
 * This class is used by the main testing classes.
 * It attempt to resolve a specified class using Class.forname()
 * @author David Knibb
 *
 */
public class ClassVisibilityTester {
	
	public static void testVis(String name) throws ClassNotFoundException {
		Class clazz = Class.forName(name);
		System.out.println(clazz);
	}
}
