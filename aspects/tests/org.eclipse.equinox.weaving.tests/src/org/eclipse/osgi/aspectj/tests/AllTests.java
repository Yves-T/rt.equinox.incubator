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

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Test for org.eclipse.equinox.weaving.tests");
		//$JUnit-BEGIN$
		suite.addTestSuite(SupplementsBundlesTest.class);
		suite.addTestSuite(SupplementsBundleWithRequireTest.class);
		suite.addTestSuite(SupplementsExportersTest.class);
		suite.addTestSuite(SupplementsImportersTest.class);
		suite.addTestSuite(WeavingTest.class);
		//$JUnit-END$
		return suite;
	}

}
