package org.eclipse.equinox.p2.diagnostic;

import java.net.URI;

import org.eclipse.equinox.internal.provisional.p2.core.ProvisionException;
import org.eclipse.equinox.internal.provisional.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.internal.provisional.p2.metadata.query.InstallableUnitQuery;
import org.eclipse.equinox.internal.provisional.p2.metadata.repository.IMetadataRepository;
import org.eclipse.equinox.internal.provisional.p2.metadata.repository.IMetadataRepositoryManager;
import org.eclipse.equinox.internal.provisional.p2.query.Collector;

public class RepositoryDumper {
	IMetadataRepositoryManager mgr = null;
	
	public void setRepositoryManager(IMetadataRepositoryManager mgr) {
		this.mgr = mgr;
	}
	
	public void dump(URI targetRepository) throws ProvisionException {
		IMetadataRepository repo = mgr.createRepository(targetRepository, "Dumped repository", null, null);
		mgr.removeRepository(targetRepository);
		Collector c = mgr.query(InstallableUnitQuery.ANY, new Collector(), null);
		repo.addInstallableUnits((IInstallableUnit[]) c.toArray(IInstallableUnit.class));
	}

}
