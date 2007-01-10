This bundle allows us to build org.aspectj.osgi.service.caching.j9 in Eclipse
and under headless PDE (for exporting bundles and building the update site)
without the IBM JDK. You still need it to run the CachingTest. There appears
no other way to persuade headless PDE to put the shared classes API on the 
CLASSPATH.