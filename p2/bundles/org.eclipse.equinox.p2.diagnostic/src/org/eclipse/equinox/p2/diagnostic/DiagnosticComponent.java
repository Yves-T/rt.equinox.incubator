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
package org.eclipse.equinox.p2.diagnostic;

import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;
import org.osgi.service.component.ComponentContext;

public class DiagnosticComponent {
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
}
