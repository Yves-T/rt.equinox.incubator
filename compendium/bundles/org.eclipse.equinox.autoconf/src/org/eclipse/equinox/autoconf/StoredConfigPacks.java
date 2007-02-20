/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.equinox.autoconf;

import java.io.*;
import java.util.Enumeration;
import java.util.Hashtable;

import org.eclipse.equinox.dp.DpConstants;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;
import org.osgi.service.deploymentadmin.DeploymentPackage;
import org.osgi.service.deploymentadmin.spi.DeploymentSession;
import org.osgi.service.log.LogService;

public class StoredConfigPacks extends ResourceToAliaspidToConfigPack {

	private final BundleContext context;

	public StoredConfigPacks(BundleContext context, DeploymentSession session) throws StreamCorruptedException, IOException, ClassNotFoundException {
		this.context = context;
		DeploymentPackage targetDp = session.getTargetDeploymentPackage();

		if (targetDp.getVersion().equals(Version.emptyVersion)) {
			Log.log(LogService.LOG_DEBUG, this, "Constructor", "new DpInstallation");
			super.resourceToTable = new Hashtable(4);
		} else {
			File inFile = context.getDataFile(getFilePath(targetDp));
			ObjectInputStream ois = null;
			try {
				FileInputStream fis = new FileInputStream(inFile);
				ois = new ObjectInputStream(fis);
				super.resourceToTable = (Hashtable) ois.readObject();
				Log.log(LogService.LOG_INFO, this, "Constructor", "Read data from:" + inFile);
			} catch (FileNotFoundException e) {
				Log.log(LogService.LOG_WARNING, "session file(" + inFile + ") was not found.", e);
				super.resourceToTable = new Hashtable(4);
			} finally {
				if (ois != null)
					ois.close();
			}
		}
	}

	public void refresh() {
		for (Enumeration enumeration = resourceToTable.keys(); enumeration.hasMoreElements();) {
			String resource = (String) enumeration.nextElement();
			Hashtable table = (Hashtable) resourceToTable.get(resource);
			if (table.size() == 0) {
				this.remove(resource);
			}
		}
	}

	private static String getFilePath(DeploymentPackage dp) {
		return DpConstants.DP_LOCATION_PREFIX + dp.getName();
	}

	public void flush(DeploymentSession session) throws IOException {
		Log.log(LogService.LOG_DEBUG, this, "flush(session)", " BEGIN");
		DeploymentPackage sourceDp = session.getSourceDeploymentPackage();
		if (!sourceDp.getVersion().equals(Version.emptyVersion)) {
			File outFile = context.getDataFile(getFilePath(sourceDp));
			ObjectOutputStream oos = null;
			try {
				FileOutputStream fos = new FileOutputStream(outFile);
				oos = new ObjectOutputStream(fos);
				oos.writeObject(getOptimizedTable());
			} finally {
				if (oos != null)
					oos.close();
			}
			return;
		}
		DeploymentPackage targetDp = session.getTargetDeploymentPackage();
		if (!targetDp.getVersion().equals(Version.emptyVersion))
			this.clearOldData(context.getDataFile(getFilePath(targetDp)));
	}

	private void clearOldData(File file) {
		super.clear();
		if (file != null)
			file.delete();
	}

	private Hashtable getOptimizedTable() {
		Log.log(LogService.LOG_DEBUG, this, "getOptimizedTable)", " BEGIN");
		Hashtable newTable = Utils.createHashtable(resourceToTable.size());
		for (Enumeration enumeration = resourceToTable.keys(); enumeration.hasMoreElements();) {
			Object resource = enumeration.nextElement();
			AliaspidToConfigPack aliaspitToConfigPack = (AliaspidToConfigPack) resourceToTable.get(resource);
			newTable.put(resource, aliaspitToConfigPack.getOptimized());
		}
		return newTable;
	}
}
