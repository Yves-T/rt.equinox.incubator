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
package solution2;

import java.util.Map;
import org.eclipse.equinox.p2.diagnostic.Activator;
import org.eclipse.equinox.p2.diagnostic.RepositoryDumper;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;
import org.osgi.service.application.ApplicationDescriptor;
import org.osgi.service.application.ApplicationHandle;
import org.osgi.service.component.ComponentContext;

public class RepositoryDumpingApplicationDescriptor extends ApplicationDescriptor {

	private RepositoryDumper repositoryDumper;

	protected void activate(ComponentContext context) {
		IMetadataRepositoryManager repoMgr = (IMetadataRepositoryManager) context.locateService("repoMgr");
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
		RepositoryDumpingApplication2 dump = new RepositoryDumpingApplication2("org.eclipse.equinox.p2.diagnostic.rd", this);
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
