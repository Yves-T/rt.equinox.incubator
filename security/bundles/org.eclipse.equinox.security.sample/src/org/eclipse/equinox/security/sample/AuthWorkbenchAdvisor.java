/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.security.sample;

import org.eclipse.ui.application.*;

public class AuthWorkbenchAdvisor extends WorkbenchAdvisor {

	public String getInitialWindowPerspectiveId() {
		return "org.eclipse.equinox.security.sample.storagePerspective"; //$NON-NLS-1$
	}

	public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
		return new AuthWorkbenchWindowAdvisor(configurer);
	}
}
