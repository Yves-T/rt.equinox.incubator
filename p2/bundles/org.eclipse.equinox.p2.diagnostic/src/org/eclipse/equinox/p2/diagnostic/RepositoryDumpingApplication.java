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

import java.io.File;
import java.net.URI;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

public class RepositoryDumpingApplication implements IApplication {

	public Object start(IApplicationContext context) throws Exception {
		new File("/Users/Pascal/tmp/allRepos").mkdirs();
		((DiagnosticComponent) Activator.getComponentInstance().getInstance()).getDumper().dump(new URI("file:/Users/Pascal/tmp/allRepos"));
		return IApplication.EXIT_OK;
	}

	public void stop() {

	}

}
