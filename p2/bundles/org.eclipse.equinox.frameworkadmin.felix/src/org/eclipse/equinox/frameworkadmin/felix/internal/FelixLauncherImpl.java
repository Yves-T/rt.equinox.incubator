/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.frameworkadmin.felix.internal;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.equinox.frameworkadmin.*;
import org.eclipse.equinox.internal.frameworkadmin.utils.SimpleBundlesState;
import org.eclipse.equinox.internal.frameworkadmin.utils.Utils;
import org.osgi.framework.BundleContext;
import org.osgi.service.log.LogService;

public class FelixLauncherImpl {
	static String getStringOfCmd(String[] cmdarray) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < cmdarray.length; i++) {
			sb.append(cmdarray[i]);
			sb.append(" ");
		}
		return sb.toString();
	}

	BundleContext context = null;

	FelixFwAdminImpl fwAdmin = null;

	FelixLauncherImpl(BundleContext context, FelixFwAdminImpl fwAdmin) {
		this.context = context;
		this.fwAdmin = fwAdmin;
	}

	public Process launch(Manipulator manipulator, File cwd) throws IllegalArgumentException, IOException, FrameworkAdminRuntimeException {
		Log.log(LogService.LOG_DEBUG, this, "launch(Manipulator manipulator, File cwd)", "");
		LauncherData launcherData = manipulator.getLauncherData();
		if (launcherData.getLauncher() == null)
			return launchInMemory(manipulator, cwd);
		return launchByLauncher(manipulator, cwd);
	}

	private Process launchByLauncher(Manipulator manipulator, File cwd) throws IOException {
		// XXX not supported
		return null;
	}

	private Process launchInMemory(Manipulator manipulator, File cwd) throws IOException {
		SimpleBundlesState.checkAvailability(fwAdmin);
		LauncherData launcherData = manipulator.getLauncherData();
		Utils.checkAbsoluteFile(launcherData.getFwJar(), "fwJar");
		Utils.checkAbsoluteDir(cwd, "cwd");
		//		prepareSuitableConfigFile(manipulator, cwd);

		List cmdList = new LinkedList();
		if (launcherData.getJvm() != null)
			cmdList.add(launcherData.getJvm().getAbsolutePath());
		else
			cmdList.add("java");

		//		if (this.fwConfigInfo.getConfiguratorConfigUrl() != null)
		//			this.manipulator.addLauncherJvmArgs(new String[] {"-D" + BundleStateConfigurator.PROP_KEY_CONFIGURL + "=" + this.fwConfigInfo.getConfiguratorConfigUrl()});

		File fwConfigDir = launcherData.getFwConfigLocation();
		if (fwConfigDir == null)
			fwConfigDir = new File(cwd, FelixConstants.DEFAULT_FW_CONFIGLOCATION);
		File fwConfigFile = new File(fwConfigDir, FelixConstants.DEFAULT_FW_CONFIGFILE);
		File systemPropsFile = new File(fwConfigDir, FelixConstants.DEFAULT_SYSTEMPROP_CONFIGFILE);

		String[] jvmArgs = launcherData.getJvmArgs();
		if (jvmArgs != null)
			for (int i = 0; i < jvmArgs.length; i++) {
				//				String prefix = "-D" + FelixConstants.PROP_FELIX_FWPRIVATE_DIR + "=";
				//				if (launcherData.jvmArgs[i].startsWith(prefix)) {
				//					String value = launcherData.jvmArgs[i].substring(prefix.length());
				//					File file = new File(value);
				//					if (launcherData.fwInstancePrivateArea != null) {
				//						if (!file.isAbsolute())
				//							file = new File(cwd, value);
				//						if (!launcherData.fwInstancePrivateArea.equals(file)) {
				//							Log.log(LogService.LOG_WARNING, "fwInstancePrivateArea doesn't match jvmArgs[" + i + "]\n" + "\tfwInstancePrivateArea will be used.\n" + "\tfwInstancePrivateArea=" + launcherData.fwInstancePrivateArea + "\n\tjvmArgs[" + i + "]=" + launcherData.jvmArgs[i] + " ");
				//							continue;
				//						}
				//					}
				//				}
				cmdList.add(jvmArgs[i]);
			}

		if (launcherData.isClean()) {
			Manipulator tmpHandler = fwAdmin.getManipulator();
			tmpHandler.setConfigData(manipulator.getConfigData());
			tmpHandler.setLauncherData(launcherData);
			boolean fail = false;
			try {
				tmpHandler.load();
			} catch (IOException ioex) {
				fail = true;
			}

			if (!fail) {
				LauncherData tmpLauncherData = tmpHandler.getLauncherData();
				File fwPersistentDataLocation = tmpLauncherData.getFwPersistentDataLocation();
				if (fwPersistentDataLocation != null) {
					if (fwPersistentDataLocation.exists())
						Utils.deleteDir(fwPersistentDataLocation);
					//			fwPrivateArea = fwPrivateArea == null ? new File(cwd, FelixConstants.DEFAULT_FW_PRIVATE_AREA) : fwPrivateArea;
					//			if (fwPrivateArea.exists())
					//				Utils.deleteDir(fwPrivateArea);
				}
			}
		}
		//}

		cmdList.add("-D" + FelixConstants.JVMPROP_FWCONFIGFILE_URL + "=file:" + fwConfigFile);
		cmdList.add("-D" + FelixConstants.JVMPROP_SYSTEMPROPS_CONFIGFILE_URL + "=file:" + systemPropsFile);

		cmdList.add("-jar");
		cmdList.add(Utils.getRelativePath(launcherData.getFwJar(), cwd));

		// cmdList.add(CONSOLE_COMMAND);
		// cmdList.add("9000");
		// cmdList.add(INSTANCE_COMMAND);
		// cmdList.add("C:\\ws");

		String[] cmdarray = new String[cmdList.size()];
		cmdList.toArray(cmdarray);
		Log.log(LogService.LOG_DEBUG, "In CWD = " + cwd + "\n\t" + getStringOfCmd(cmdarray));

		return Runtime.getRuntime().exec(cmdarray, null, cwd);
	}

}
