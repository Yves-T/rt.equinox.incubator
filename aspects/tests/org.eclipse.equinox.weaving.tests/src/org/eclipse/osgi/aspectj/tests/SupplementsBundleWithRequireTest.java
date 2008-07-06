/*******************************************************************************
 * Copyright (c) 2008 Martin Lippert and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Martin Lippert               initial implementation      
 *******************************************************************************/

package org.eclipse.osgi.aspectj.tests;

import junit.framework.TestCase;

/**
 * Class for testing the Supplement-Bundle mechanism
 * 
 *  @author Martin Lippert
 */
public class SupplementsBundleWithRequireTest extends TestCase {

	/*
	 * Test method for 'org.eclipse.osgi.aspectj.tests.ClassVisibilityTester.testVis(String)'
	 * Does Supplement-Bundle work
	 */
	public void testTestVis() {
		// The variable name holds the full name of a class.
		// The test will determine whether or not this class is visible.
		String name = "org.eclipse.osgi.aspectj.tests.bundles.bundleSupplementWithRequire.BundleSupplement";
		try {
			ClassVisibilityTester.testVis(name);
		} catch (ClassNotFoundException e) {
			fail("Cannot find class " + name + " " + e.getMessage());
		} catch (Exception e) {
			fail("Failed for some other reason  " + e.getMessage());
		}
	}
	
}
