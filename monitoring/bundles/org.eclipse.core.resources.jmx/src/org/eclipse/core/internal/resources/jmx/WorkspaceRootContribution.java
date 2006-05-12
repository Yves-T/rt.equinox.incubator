/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.resources.jmx;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * @since 1.0
 */
public class WorkspaceRootContribution extends ContainerContribution {

	private static final String ICON_PATH = "icons/root.gif";//$NON-NLS-1$

	/*
	 * Constructor for the class.
	 */
	public WorkspaceRootContribution(IWorkspaceRoot root) {
		super(root);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.internal.resources.jmx.ResourceContribution#getIconPath()
	 */
	protected IPath getIconPath() {
		return new Path(ICON_PATH);
	}

	/* (non-Javadoc)
	 * @see com.jmx.server.contrib.Contribution#getName()
	 */
	protected String getName() {
		return Messages.rootName;
	}

}
