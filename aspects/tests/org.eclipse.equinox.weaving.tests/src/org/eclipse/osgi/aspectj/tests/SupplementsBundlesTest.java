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
 * Class for testing the Supplement-Bundle mechanism
 * 
 *  @author David Knibb
 */
public class SupplementsBundlesTest extends TestCase {

	/*
	 * Test method for 'org.eclipse.osgi.aspectj.tests.ClassVisibilityTester.testVis(String)'
	 * Does Supplement-Bundle work
	 */
	public void testTestVis() {
		// The variable name holds the full name of a class.
		// The test will determine whether or not this class is visible.
		String name = "org.eclipse.osgi.aspectj.tests.bundles.bundleSupplement.BundleSupplement";
		try {
			ClassVisibilityTester.testVis(name);
		} catch (ClassNotFoundException e) {
			fail("Cannot find class " + name + " " + e.getMessage());
		} catch (Exception e) {
			fail("Failed for some other reason  " + e.getMessage());
		}
	}
	
	/*
	 * Can a bundle be supplemented by multiple bundles
	 */
	public void test002() {
		String name = "org.eclipse.osgi.aspectj.tests.bundles.aspectWeavingLT.FlagSetterLT";
		try {
			ClassVisibilityTester.testVis(name);
		} catch (ClassNotFoundException e) {
			fail("Cannot find class " + name + " " + e.getMessage());
		} catch (Exception e) {
			fail("Failed for some other reason  " + e.getMessage());
		}
	}
	
	/*
	 * Can we match wildcards?
	 * (bundles ending in * only)
	 */
	public void test003(){
		String name = "org.eclipse.osgi.aspectj.tests.bundles.wildBundleSupplement.Foo";
		try {
			ClassVisibilityTester.testVis(name);
		} catch (ClassNotFoundException e) {
			fail("Cannot find class " + name + " " + e.getMessage());
		} catch (Exception e) {
			fail("Failed for some other reason  " + e.getMessage());
		}
	}
}
