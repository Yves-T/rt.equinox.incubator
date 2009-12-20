package org.eclipse.equinox.p2.cudf.tests;

import java.io.File;
import java.io.FilenameFilter;
import junit.framework.*;

public class CheckAllFailingInstances extends TestCase {
	public static Test suite() {
		TestSuite suite = new TestSuite(CheckAllFailingInstances.class.getName());
		File resourceDirectory = new File(CheckAllFailingInstances.class.getClassLoader().getResource("testData/instances/expectedFailure/").toString().substring("file:".length()));
		File[] resources = resourceDirectory.listFiles(new FilenameFilter() {

			public boolean accept(File dir, String name) {
				if (name.startsWith("disabled-"))
					return false;
				return true;
			}
		});
		for (int i = 0; i < resources.length; i++) {
			suite.addTest(new CheckInstance(resources[i], false));
		}
		return suite;
	}
}
