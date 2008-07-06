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

import junit.framework.TestCase;

/**
 * Class for testing the Supplement-Importer mechnanism
 * @author David Knibb
 */
public class SupplementsImportersTest extends TestCase {
	
	private static final String name = 
		"org.eclipse.osgi.aspectj.tests.bundles.importSupplement.ImportedClass";

	/*
	 * Test method for 'org.eclipse.osgi.aspectj.tests.SupIm1.testVis(String)'
	 */
	public void testTestVis() {
		// The variable name holds the full name of a class.
		// The test will determine whether or not this class is visible.
		// An auxialliary bundle is needed - this is the bundle which contains the package(s) named in Supplement-Importer
		try {
			//is the named package visible to this bundle 
			//spareBundle is the auxialliary bundle
			ClassVisibilityTester.testVis(name);
		} catch (ClassNotFoundException e) {
			fail("Cannot find class " + name + " " + e.getMessage());
		} catch (Exception e) {
			fail("Failed for some other reason  " + e.getMessage());
		}
	}
	
	/*
	 * Test method for 'org.eclipse.osgi.aspectj.tests.bundles.extraSpareBundle.ClassVisibilityTester.testVis(String)'
	 * tests whether the importSupplement package is supplementing the importers of a second package 
	 */
	public void testMulipleSupplementation() {
		try {
			/*
			 * now spareBundle.second is being used as the auxiliary package
			 * so extraSpareBundle should be able to see importSupplement
			 */
			org.eclipse.osgi.aspectj.tests.bundles.extraSpareBundle.ClassVisibilityTester.testVis(name);
		} catch (ClassNotFoundException e) {
			fail("Cannot find class " + name + " " + e.getMessage());
		} catch (Exception e) {
			fail("Failed for some other reason  " + e.getMessage());
		}

	}

}
