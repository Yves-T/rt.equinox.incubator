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
 * Class for testing the Supplement-Exporter mechanism
 * @author David Knibb
 */
public class SupplementsExportersTest extends TestCase {
	
	/*
	 * Test method for 'org.eclipse.osgi.aspectj.Extests.SupEx1.testVis(String)'
	 */
	public void testTestVis() {
		String name = "org.eclipse.osgi.aspectj.tests.bundles.exportSupplement.ExportedClass";
		try {
			ClassVisibilityTester.testVis(name);
		} catch (ClassNotFoundException e) {
			fail("Cannot find class " + name + " " + e.getMessage());
		} catch (Exception e) {
			fail("Failed for some other reason  " + e.getMessage());
		}
	}
}
