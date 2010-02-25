/*******************************************************************************
 *  Copyright (c) 2010 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package solution3;

import java.net.URI;
import java.util.HashMap;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.query.IQueryResult;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepository;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;

public class RepositoryDumper {
	IMetadataRepositoryManager mgr = null;

	public void setRepositoryManager(IMetadataRepositoryManager mgr) {
		this.mgr = mgr;
	}

	public void dump(URI targetRepository) throws ProvisionException {
		IMetadataRepository repo = mgr.createRepository(targetRepository, "Dumped repository", IMetadataRepositoryManager.TYPE_SIMPLE_REPOSITORY, new HashMap<String, String>());
		mgr.removeRepository(targetRepository);
		IQueryResult<IInstallableUnit> c = mgr.query(QueryUtil.createIUAnyQuery(), null);
		repo.addInstallableUnits(c.toSet());
	}

}
