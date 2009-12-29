package solution3;

import java.net.URI;
import java.util.HashMap;

import org.eclipse.equinox.internal.provisional.p2.core.ProvisionException;
import org.eclipse.equinox.internal.provisional.p2.metadata.query.InstallableUnitQuery;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.metadata.query.IQueryResult;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepository;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;

public class RepositoryDumper {
	IMetadataRepositoryManager mgr = null;
	
	public void setRepositoryManager(IMetadataRepositoryManager mgr) {
		this.mgr = mgr;
	}
	
	public void dump(URI targetRepository) throws ProvisionException {
		IMetadataRepository repo = mgr.createRepository(targetRepository, "Dumped repository", IMetadataRepositoryManager.TYPE_SIMPLE_REPOSITORY, new HashMap<String,String>());
		mgr.removeRepository(targetRepository);
		IQueryResult<IInstallableUnit> c = mgr.query(InstallableUnitQuery.ANY, null);
		repo.addInstallableUnits(c.toArray(IInstallableUnit.class));
	}

}
