package org.eclipse.equinox.p2.sharedprofile;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import org.eclipse.core.runtime.*;
import org.eclipse.equinox.p2.engine.Profile;
import org.eclipse.equinox.p2.engine.SelfProfileProducer;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.metadata.query.InstallableUnitQuery;
import org.eclipse.equinox.p2.metadata.repository.IMetadataRepository;
import org.eclipse.equinox.p2.metadata.repository.IMetadataRepositoryManager;
import org.eclipse.equinox.p2.query.Collector;

public class Shared implements SelfProfileProducer {

	public void fill(Profile p) throws CoreException {

		// get the "shared" repo
		IMetadataRepositoryManager manager = (IMetadataRepositoryManager) ServiceHelper.getService(Activator.getContext(), IMetadataRepositoryManager.class.getName());
		URL sharedRepoURL;
		try {
			sharedRepoURL = new URL(Activator.getContext().getProperty("p2.shared.repo"));
		} catch (MalformedURLException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Can't find the shared repo", e));
		}
		manager.addRepository(sharedRepoURL);
		IMetadataRepository repo = manager.loadRepository(sharedRepoURL, null);

		// get the base IU
		Collector collector = repo.query(new InstallableUnitQuery("_SHARED_BASE_"), new Collector(), null);

		// add it to profile
		IInstallableUnit iu;
		if (collector.size() > 0) {
			iu = (IInstallableUnit) collector.iterator().next();
			p.addInstallableUnit(iu);
		} else {
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Can't find the shared IU"));
		}
	}

	public Profile create(Profile p) {
		Profile result = null;
		if (p == null) {
			result = new Profile("THE WAY I WANT");
		} else
			result = p;
		Map properties = new Properties();
		properties.put("osgi.os", Platform.getOS());
		properties.put("osgi.ws", Platform.getWS());
		properties.put("osgi.arch", Platform.getOSArch());
		properties.put("flavor", "tooling");
		result.addProperties(properties);
		return result;
	}

}
