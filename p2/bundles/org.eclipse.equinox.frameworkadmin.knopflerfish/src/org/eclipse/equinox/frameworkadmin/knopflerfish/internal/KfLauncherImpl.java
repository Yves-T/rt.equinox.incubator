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
package org.eclipse.equinox.frameworkadmin.knopflerfish.internal;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.equinox.internal.frameworkadmin.utils.SimpleBundlesState;
import org.eclipse.equinox.internal.frameworkadmin.utils.Utils;
import org.eclipse.equinox.internal.provisional.frameworkadmin.*;
import org.osgi.framework.BundleContext;
import org.osgi.service.log.LogService;

public class KfLauncherImpl {
	static String getStringOfCmd(String[] cmdarray) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < cmdarray.length; i++) {
			sb.append(cmdarray[i]);
			sb.append(" ");
		}
		return sb.toString();
	}

	BundleContext context = null;

	KfFwAdminImpl fwAdmin = null;

	KfLauncherImpl(BundleContext context, KfFwAdminImpl fwAdmin) {
		this.context = context;
		this.fwAdmin = fwAdmin;
	}

	public Process launch(Manipulator Manipulator, File cwd) throws IllegalArgumentException, IOException, FrameworkAdminRuntimeException {
		Log.log(LogService.LOG_DEBUG, this, "launch(Manipulator Manipulator, File cwd)", "");
		LauncherData launcherData = Manipulator.getLauncherData();
		if (launcherData.getLauncher() == null)
			return launchInMemory(Manipulator, cwd);
		return launchByLauncher(Manipulator, cwd);
	}

	private Process launchByLauncher(Manipulator Manipulator, File cwd) throws IOException {
		// XXX not supported
		return null;
	}

	private Process launchInMemory(Manipulator Manipulator, File cwd) throws IOException {
		SimpleBundlesState.checkAvailability(fwAdmin);
		LauncherData launcherData = Manipulator.getLauncherData();
		Utils.checkAbsoluteFile(launcherData.getFwJar(), "fwJar");
		Utils.checkAbsoluteDir(cwd, "cwd");
		//		prepareSuitableConfigFile(Manipulator, cwd);

		List cmdList = new LinkedList();
		if (launcherData.getJvm() != null)
			cmdList.add(launcherData.getJvm().getAbsolutePath());
		else
			cmdList.add("java");

		//		if (this.fwConfigInfo.getConfiguratorConfigUrl() != null)
		//			this.Manipulator.addLauncherJvmArgs(new String[] {"-D" + BundleStateConfigurator.PROP_KEY_CONFIGURL + "=" + this.fwConfigInfo.getConfiguratorConfigUrl()});

		String[] jvmArgs = launcherData.getJvmArgs();
		if (jvmArgs != null)
			for (int i = 0; i < jvmArgs.length; i++) {
				String prefix = "-D" + KfConstants.PROP_KF_FW_PERSISTENT_DATA_LOC + "=";
				if (jvmArgs[i].startsWith(prefix)) {
					String value = jvmArgs[i].substring(prefix.length());
					File file = new File(value);
					if (launcherData.getFwPersistentDataLocation() != null) {
						if (!file.isAbsolute())
							file = new File(cwd, value);
						if (!launcherData.getFwPersistentDataLocation().equals(file)) {
							Log.log(LogService.LOG_WARNING, "fwPersistentDataLocation doesn't match jvmArgs[" + i + "]\n" + "\tfwPersistentDataLocation will be used.\n" + "\tfwInstancePrivateArea=" + launcherData.getFwPersistentDataLocation() + "\n\tjvmArgs[" + i + "]=" + jvmArgs[i] + " ");
							continue;
						}
					}
				}
				cmdList.add(jvmArgs[i]);
			}

		if (launcherData.getFwPersistentDataLocation() != null) {
			cmdList.add("-D" + KfConstants.PROP_KF_FW_PERSISTENT_DATA_LOC + "=" + Utils.getRelativePath(launcherData.getFwPersistentDataLocation(), cwd));
		}

		cmdList.add("-jar");
		cmdList.add(Utils.getRelativePath(launcherData.getFwJar(), cwd));

		// -init must be present prior to "-xargs", if required.
		if (launcherData.isClean())
			cmdList.add(KfConstants.XARGS_INIT);

		if (launcherData.getFwConfigLocation() != null) {
			cmdList.add(KfConstants.XARGS_OPTION);
			cmdList.add(Utils.getRelativePath(launcherData.getFwConfigLocation(), cwd));
		}

		String[] cmdarray = new String[cmdList.size()];
		cmdList.toArray(cmdarray);
		Log.log(LogService.LOG_DEBUG, "In CWD = " + cwd + "\n\t" + getStringOfCmd(cmdarray));
		return Runtime.getRuntime().exec(cmdarray, null, cwd);
	}

}
