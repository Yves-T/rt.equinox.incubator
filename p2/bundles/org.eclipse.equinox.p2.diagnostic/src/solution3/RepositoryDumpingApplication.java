package solution3;


import java.io.File;
import java.net.URI;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.equinox.p2.diagnostic.Activator;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class RepositoryDumpingApplication implements IApplication {

	public Object start(IApplicationContext context) throws Exception {
		final BundleContext bundleContext = Activator.getContext();
		System.out.println(IMetadataRepositoryManager.class.getName());
		ServiceReference sr = bundleContext.getServiceReference(RepositoryDumper.class.getName());
		RepositoryDumper dumper = (RepositoryDumper)bundleContext.getService(sr);
		new File("/Users/Pascal/tmp/allRepos").mkdirs();
		dumper.dump(new URI("file:/Users/Pascal/tmp/allRepos"));
		bundleContext.ungetService(sr);
		return IApplication.EXIT_OK;
	}

	public void stop() {

	}

}
