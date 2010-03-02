/*******************************************************************************
 * Copyright (c) 2007, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.frameworkadmin.knopflerfish;

import java.io.File;
import org.eclipse.equinox.internal.provisional.frameworkadmin.ConfigData;

public class KfConfigData extends ConfigData {

	File xargsFile = null;

	public KfConfigData(String fwName, String fwVersion, String launcherName, String launcherVersion) {
		super(fwName, fwVersion, launcherName, launcherVersion);
	}

	public File getXargsFile() {
		return xargsFile;
	}

	public void setXargsFile(File file) {
		this.xargsFile = file;
	}
}
