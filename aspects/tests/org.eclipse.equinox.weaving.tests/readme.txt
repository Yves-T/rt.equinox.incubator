Use the WeavingTest.launch configuration to run the weaving tests. Make sure
to use the config.ini in the org.eclipse.osgi.aspectj.tests project. This installs the
org.aspectj.osgi Framework Extension and loads the AspectJ weaving service.

Use the CachingTest (J9).launch configuration to run the caching tests. Make sure
to use the config.ini in the org.eclipse.osgi.aspectj.tests project. This installs the
org.aspectj.osgi Framework Extension and loads the AspectJ weaving service as well
as the J9 caching service. You will need to download an install the IBM 5.0 JVM
appropriate to your platform from http://www.ibm.com/developerworks/java/jdk/index.html
to both build the org.aspectj.osgi.service.caching.j9 bundle and run the tests.