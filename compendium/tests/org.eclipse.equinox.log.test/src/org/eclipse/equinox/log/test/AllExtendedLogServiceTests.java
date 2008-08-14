package org.eclipse.equinox.log.test;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllExtendedLogServiceTests {
	public static Test suite() {
		TestSuite suite = new TestSuite("Test extended log service"); //$NON-NLS-1$
		suite.addTestSuite(ExtendedLogServiceTest.class);
		return suite;
	}
}
