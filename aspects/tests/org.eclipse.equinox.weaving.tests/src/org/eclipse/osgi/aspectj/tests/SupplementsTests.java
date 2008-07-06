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

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Full suite of Supplements Test
 * 
 * @author David Knibb
 */
public class SupplementsTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Test for org.eclipse.osgi.aspectj.tests");
		//$JUnit-BEGIN$
		suite.addTestSuite(SupplementsImportersTest.class);
		suite.addTestSuite(SupplementsExportersTest.class);
		suite.addTestSuite(SupplementsBundlesTest.class);
		suite.addTestSuite(SupplementsBundleWithRequireTest.class);
		//$JUnit-END$
		return suite;
	}

}
