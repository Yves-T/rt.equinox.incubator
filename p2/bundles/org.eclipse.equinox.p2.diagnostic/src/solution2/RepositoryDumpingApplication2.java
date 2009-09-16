package solution2;

import java.io.File;
import java.net.URI;
import java.util.Map;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.p2.diagnostic.RepositoryDumper;
import org.osgi.service.application.ApplicationDescriptor;
import org.osgi.service.application.ApplicationHandle;

public class RepositoryDumpingApplication2 extends ApplicationHandle {

	protected RepositoryDumpingApplication2(String instanceId,
			ApplicationDescriptor descriptor) {
		super(instanceId, descriptor);
	}

	public Object start(Map arguments) throws Exception {
		RepositoryDumper dumper = (RepositoryDumper) arguments.get("repo_dumper");
		new File("/Users/Pascal/tmp/allRepos").mkdirs();
		dumper.dump(new URI("file:/Users/Pascal/tmp/allRepos"));
		return IApplication.EXIT_OK;
	}

	public void stop() {

	}

	@Override
	protected void destroySpecific() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getState() {
		// TODO Auto-generated method stub
		return null;
	}

}
