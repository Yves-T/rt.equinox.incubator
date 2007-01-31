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
package org.eclipse.equinox.frameworkadmin.examples;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;

import org.eclipse.equinox.frameworkadmin.*;
import org.osgi.framework.*;
import org.osgi.util.tracker.ServiceTracker;

public class EquinoxActivator {
	final static List bundleInfoListSimpleConfigurator = new LinkedList();

	public static final String PROPS_KEY_CONSOLE_PORT = "osgi.console";
	private final String FILTER_OBJECTCLASS = "(" + Constants.OBJECTCLASS + "=" + FrameworkAdmin.class.getName() + ")";
	private String filterFwAdmin;
	private String fwBundle;
	private String equinoxCommonBundle;
	private String coreRuntimeBundle;
	private String updateConfiguratorBundle;
	public File fwHome;

	public File eclipseExe;

	public File cwd;

	public String propsValueConsolePort;

	public File bundlesDir;

	public File fwJar;

	public File configLoc;
	final List bundleInfoListEclipse = new LinkedList();
	final List bundleInfoListWoSimpleConfigurator = new LinkedList();
	final List bundleInfoListWithSimpleConfigurator = new LinkedList();

	private BundleContext context;

	private Process process = null;

	private ServiceTracker fwAdminTracker;

	FrameworkAdmin fwAdmin;

	private InputStreamMonitorThread threadStandardI = null;

	private InputStreamMonitorThread threadErrorI = null;

	EquinoxActivator(BundleContext context, Properties props) {
		this.context = context;
		this.readParameters(props);

		initialieBundlesList();

	}

	void eclipseSaveAndLaunch(List bundleInfoList, boolean backup) throws IOException {
		Manipulator manipulator = fwAdmin.getManipulator();
		ConfigData configData = manipulator.getConfigData();
		LauncherData launcherData = manipulator.getLauncherData();
		// 1. Set Parameters.
		launcherData.setLauncher(eclipseExe);

		launcherData.setJvm(new File(Activator.jvm));
		String[] jvmArgs = {Activator.jvmArgs};
		launcherData.setJvmArgs(jvmArgs);
		launcherData.setFwPersistentDataLocation(configLoc, true);
		launcherData.setFwJar(fwJar);
		launcherData.setFwConfigLocation(configLoc);
		// 2. Set Parameters to ConfigData.
		for (Iterator ite = bundleInfoList.iterator(); ite.hasNext();) {
			BundleInfo bInfo = (BundleInfo) ite.next();
			configData.addBundle(bInfo);
		}
		configData.setBeginningFwStartLevel(5);
		configData.setInitialBundleStartLevel(5);
		configData.setFwDependentProp(PROPS_KEY_CONSOLE_PORT, propsValueConsolePort);
		// 3. Save them.
		manipulator.save(backup);

		// 4. Launch it.
		process = fwAdmin.launch(manipulator, cwd);
		InputStreamMonitorThread.monitorThreadStart(process, threadStandardI, threadErrorI);

	}

	void equinoxSaveAndLaunch(List bundleInfoList, boolean backup) throws IOException {
		Manipulator manipulator = fwAdmin.getManipulator();
		ConfigData configData = manipulator.getConfigData();
		LauncherData launcherData = manipulator.getLauncherData();
		// 1. Set Parameters to LaunchData.
		launcherData.setJvm(new File(Activator.jvm));
		String[] jvmArgs = {Activator.jvmArgs};
		launcherData.setJvmArgs(jvmArgs);
		launcherData.setFwPersistentDataLocation(configLoc, true);
		launcherData.setFwJar(fwJar);
		launcherData.setFwConfigLocation(configLoc);
		// 2. Set Parameters to ConfigData.
		for (Iterator ite = bundleInfoList.iterator(); ite.hasNext();) {
			BundleInfo bInfo = (BundleInfo) ite.next();
			configData.addBundle(bInfo);
		}
		configData.setBeginningFwStartLevel(5);
		configData.setInitialBundleStartLevel(5);
		configData.setFwDependentProp(PROPS_KEY_CONSOLE_PORT, propsValueConsolePort);

		// 3. Expect bundles state.
		BundleInfo[] bInfos = manipulator.getExpectedState();
		System.out.println("ExpectedState:");
		for (int i = 0; i < bInfos.length; i++)
			System.out.println(" " + bInfos[i]);

		// 4. Save them.
		manipulator.save(backup);

		// 5. Launch it.
		process = fwAdmin.launch(manipulator, cwd);
		InputStreamMonitorThread.monitorThreadStart(process, threadStandardI, threadErrorI);

	}

	private String getFullyQualifiedLocation(String BundleJarName) {
		try {
			return (new File(bundlesDir, BundleJarName)).toURL().toExternalForm();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	private void initialieBundlesList() {
		try {
			bundleInfoListEclipse.add(new BundleInfo(fwJar.toURL().toExternalForm(), 0, true));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		bundleInfoListEclipse.add(new BundleInfo(getFullyQualifiedLocation(equinoxCommonBundle), 3, true));
		bundleInfoListEclipse.add(new BundleInfo(getFullyQualifiedLocation(updateConfiguratorBundle), 4, true));
		bundleInfoListEclipse.add(new BundleInfo(getFullyQualifiedLocation(coreRuntimeBundle), BundleInfo.NO_LEVEL, true));
		try {
			bundleInfoListWoSimpleConfigurator.add(new BundleInfo(fwJar.toURL().toExternalForm(), 0, true));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		bundleInfoListWoSimpleConfigurator.add(new BundleInfo(getFullyQualifiedLocation(equinoxCommonBundle), 3, true));
		bundleInfoListWoSimpleConfigurator.add(new BundleInfo(getFullyQualifiedLocation(updateConfiguratorBundle), 4, false));
		bundleInfoListWoSimpleConfigurator.add(new BundleInfo(getFullyQualifiedLocation(coreRuntimeBundle), BundleInfo.NO_LEVEL, false));

		bundleInfoListSimpleConfigurator.add(new BundleInfo(getFullyQualifiedLocation(Activator.frameworkAdminServiceBundle), BundleInfo.NO_LEVEL, false));
		bundleInfoListSimpleConfigurator.add(new BundleInfo(getFullyQualifiedLocation(Activator.simpleConfiguratorBundle), 2, true));

		bundleInfoListWithSimpleConfigurator.addAll(bundleInfoListWoSimpleConfigurator);
		bundleInfoListWithSimpleConfigurator.addAll(bundleInfoListSimpleConfigurator);
	}

	public void launch(Manipulator manipulator, File runtimeCwd) throws IOException {
		process = fwAdmin.launch(manipulator, runtimeCwd);
		InputStreamMonitorThread.monitorThreadStart(process, threadStandardI, threadErrorI);

	}

	private void printoutParameters() {
		System.out.println("######################\nEquinoxActivator");
		System.out.println("fwHome:" + fwHome);
		System.out.println("cwd:" + cwd);
		System.out.println("eclipseExe:" + eclipseExe);
		System.out.println("bundlesDir:" + bundlesDir);
		System.out.println("fwBundle:" + fwBundle);
		System.out.println("fwJar:" + fwJar);
		System.out.println("configLoc:" + configLoc);
		System.out.println("coreRuntimeBundle:" + coreRuntimeBundle);
		System.out.println("updateConfiguratorBundle:" + updateConfiguratorBundle);
		System.out.println("");
	}

	private void readParameters(Properties props) {
		fwHome = new File(Activator.getValue(props, "equinox.home"));
		cwd = new File(Activator.getValue(props, "equinox.cwd"));

		eclipseExe = new File(Activator.getValue(props, "equinox.launcher"));

		bundlesDir = new File(fwHome, Activator.getValue(props, "equinox.bundlesDir"));
		fwBundle = Activator.getValue(props, "equinox.fw");
		equinoxCommonBundle = Activator.getValue(props, "equinox.bundles.common");
		coreRuntimeBundle = Activator.getValue(props, "equinox.bundles.core.runtime");
		updateConfiguratorBundle = Activator.getValue(props, "equinox.bundles.updateconfigurator");

		fwJar = new File(fwHome, fwBundle);
		propsValueConsolePort = Activator.getValue(props, "equinox.console.port");
		configLoc = new File(fwHome, Activator.getValue(props, "equinox.configLoc"));

		String filterFwName = "(" + FrameworkAdmin.SERVICE_PROP_KEY_FW_NAME + "=" + props.getProperty("equinox.fw.name") + ")";
		String filterFwVersion = "(" + FrameworkAdmin.SERVICE_PROP_KEY_FW_VERSION + "=" + props.getProperty("equinox.fw.version") + ")";
		String filterLauncherName = "(" + FrameworkAdmin.SERVICE_PROP_KEY_LAUNCHER_NAME + "=" + props.getProperty("equinox.launcher.name") + ")";
		String filterLauncherVersion = "(" + FrameworkAdmin.SERVICE_PROP_KEY_LAUNCHER_VERSION + "=" + props.getProperty("equinox.launcher.version") + ")";
		filterFwAdmin = "(&" + FILTER_OBJECTCLASS + filterFwName + filterFwVersion + filterLauncherName + filterLauncherVersion + ")";

		printoutParameters();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start() throws InvalidSyntaxException {
		Filter chFilter = context.createFilter(filterFwAdmin);
		fwAdminTracker = new ServiceTracker(this.context, chFilter, null);
		fwAdminTracker.open();
		fwAdmin = (FrameworkAdmin) this.fwAdminTracker.getService();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop() throws Exception {
		this.context = null;
		fwAdminTracker.close();
		fwAdminTracker = null;
		this.fwAdmin = null;
		InputStreamMonitorThread.stopProcess(process, threadStandardI, threadErrorI);

	}

}
