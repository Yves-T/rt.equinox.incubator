package solution2;

import java.util.Map;

import org.eclipse.equinox.internal.provisional.p2.metadata.repository.IMetadataRepositoryManager;
import org.eclipse.equinox.p2.diagnostic.Activator;
import org.eclipse.equinox.p2.diagnostic.RepositoryDumper;
import org.osgi.service.application.ApplicationDescriptor;
import org.osgi.service.application.ApplicationHandle;
import org.osgi.service.component.ComponentContext;

public class RepositoryDumpingApplicationDescriptor extends
		ApplicationDescriptor {

	private RepositoryDumper repositoryDumper;

	protected void activate(ComponentContext context) {
		IMetadataRepositoryManager repoMgr = (IMetadataRepositoryManager) context
				.locateService("repoMgr");
		repositoryDumper = new RepositoryDumper();
		repositoryDumper.setRepositoryManager(repoMgr);

		Activator.setComponentInstance(context.getComponentInstance());
	}

	protected void deactivate(ComponentContext context) {
		repositoryDumper.setRepositoryManager(null);
	}

	public RepositoryDumper getDumper() {
		return repositoryDumper;
	}

	protected RepositoryDumpingApplicationDescriptor(String applicationId) {
		super(applicationId);
	}

	@Override
	protected Map getPropertiesSpecific(String locale) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected boolean isLaunchableSpecific() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected ApplicationHandle launchSpecific(Map arguments) throws Exception {
		RepositoryDumpingApplication2 dump = new RepositoryDumpingApplication2(
				"org.eclipse.equinox.p2.diagnostic.rd", this);
		arguments.put("repo_dumper", repositoryDumper);
		dump.start(arguments);
		return dump;
	}

	@Override
	protected void lockSpecific() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean matchDNChain(String pattern) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void unlockSpecific() {
		// TODO Auto-generated method stub

	}

}
