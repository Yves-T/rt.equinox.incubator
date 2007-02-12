/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
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

	private String jobsBundle;
	private String registryBundle;
	private String preferencesBundle;
	private String contenttypeBundle;
	private String applBundle;
	private String osgiServicesBundle;
	private String fwadminExamplesBundle;
	private String fwadminEquinoxBundle;
	//	private String fwadminKnopflerfishBundle;
	//	private String fwadminFelixBundle;

	public File fwHome;

	public File eclipseExe;

	public File cwd;

	public String propsValueConsolePort;

	public File bundlesDir;

	public File fwJar;

	public File configLoc;
	public File configLocForRunningTest;
	final List bundleInfoListEclipse = new LinkedList();
	final List bundleInfoListWoSimpleConfigurator = new LinkedList();
	final List bundleInfoListWithSimpleConfigurator = new LinkedList();
	final List bundleInfoListForRunningTest = new LinkedList();

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

	void equinoxGetState() throws IOException {
		System.out.println("equinoxGetState()");
		Manipulator manipulator = fwAdmin.getManipulator();
		//ConfigData configData = manipulator.getConfigData();
		LauncherData launcherData = manipulator.getLauncherData();
		// 1. getState from the persistently stored data.
		// Set Parameters.
		launcherData.setFwPersistentDataLocation(configLoc, false);
		launcherData.setFwJar(fwJar);
		launcherData.setFwConfigLocation(configLoc);
		manipulator.load();
		BundleInfo[] bInfos = manipulator.getExpectedState();
		System.out.println("ExpectedState:");
		for (int i = 0; i < bInfos.length; i++)
			System.out.println(" " + bInfos[i]);
	}

	/**
	 * For equinox, set parameters and save into config files.
	 * without launching,load config from saved files and
	 * expect bundles state. 
	 * 
	 * @param bundleInfoList
	 * @param backup
	 * @throws IOException
	 */
	void equinoxSaveAndGetState(List bundleInfoList, boolean backup) throws IOException {
		equinoxSetAndSave(bundleInfoList, backup);

		// 4. getState from the persistently stored data.
		Manipulator manipulator = fwAdmin.getManipulator();
		//configData = manipulator.getConfigData();
		LauncherData launcherData = manipulator.getLauncherData();
		// Set Parameters.
		launcherData.setFwPersistentDataLocation(configLoc, false);
		launcherData.setFwJar(fwJar);
		launcherData.setFwConfigLocation(configLoc);
		manipulator.load();
		BundleInfo[] bInfos = manipulator.getExpectedState();
		System.out.println("ExpectedState:");
		for (int i = 0; i < bInfos.length; i++)
			System.out.println(" " + bInfos[i]);
	}

	/**
	 * For equinox, set parameters and save into config files.
	 * 
	 * @param bundleInfoList
	 * @param backup
	 * @return Manipulator object used for saving.
	 * @throws IOException
	 */
	Manipulator equinoxSetAndSave(List bundleInfoList, boolean backup) throws IOException {
		if (configLoc.exists())
			if (configLoc.isFile())
				configLoc.delete();
			else {
				File[] lists = configLoc.listFiles();
				if (lists != null)
					for (int i = 0; i < lists.length; i++) {
						lists[i].delete();
					}
			}
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
		System.out.println("Before Save, ExpectedState:");
		for (int i = 0; i < bInfos.length; i++)
			System.out.println(" " + bInfos[i]);

		// 4. Save them.
		manipulator.save(backup);
		System.out.println("Saved");
		return manipulator;
	}

	/**
	 * For equinox, set parameters and save into config files.
	 * nextly launch by the config. 
	 
	 * @param bundleInfoList
	 * @param backup
	 * @throws IOException
	 */
	Process equinoxSaveAndLaunch(List bundleInfoList, boolean backup) throws IOException {
		Manipulator manipulator = equinoxSetAndSave(bundleInfoList, backup);
		// 5. Launch it.
		process = fwAdmin.launch(manipulator, cwd);
		System.out.println("Launched");
		InputStreamMonitorThread.monitorThreadStart(process, threadStandardI, threadErrorI);
		return process;
	}

	/**
	 * For equinox, set parameters and save into config files.
	 * 
	 * @param bundleInfoList
	 * @param backup
	 * @return Manipulator object used for saving.
	 * @throws IOException
	 */
	Manipulator equinoxSetAndSaveForRunningTest() throws IOException {
		Manipulator manipulator = fwAdmin.getManipulator();
		ConfigData configData = manipulator.getConfigData();
		LauncherData launcherData = manipulator.getLauncherData();
		// 1. Set Parameters to LaunchData.
		launcherData.setJvm(new File(Activator.jvm));
		String[] jvmArgs = {Activator.jvmArgs};
		launcherData.setJvmArgs(jvmArgs);
		launcherData.setFwPersistentDataLocation(this.configLocForRunningTest, true);
		launcherData.setFwJar(fwJar);
		launcherData.setFwConfigLocation(this.configLocForRunningTest);
		// 2. Set Parameters to ConfigData.
		for (Iterator ite = this.bundleInfoListForRunningTest.iterator(); ite.hasNext();) {
			BundleInfo bInfo = (BundleInfo) ite.next();
			configData.addBundle(bInfo);
		}
		configData.setBeginningFwStartLevel(5);
		configData.setInitialBundleStartLevel(5);
		configData.setFwDependentProp(PROPS_KEY_CONSOLE_PORT, propsValueConsolePort);

		// 3. Expect bundles state.
		BundleInfo[] bInfos = manipulator.getExpectedState();
		System.out.println("Before Save, ExpectedState:");
		for (int i = 0; i < bInfos.length; i++)
			System.out.println(" " + bInfos[i]);

		// 4. Save them.
		manipulator.save(false);
		System.out.println("Saved");
		return manipulator;
	}

	/**
	 * 
	 * 
	 * @throws IOException
	 */
	void equinoxGetRunningState() throws IOException {
		//1. get FrameworkAdmin for running system.
		String filter = "(" + FrameworkAdmin.SERVICE_PROP_KEY_RUNNING_SYSTEM_FLAG + "=true)";
		ServiceReference[] references = null;
		try {
			references = context.getServiceReferences(FrameworkAdmin.class.getName(), filter);
		} catch (InvalidSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (references == null)
			throw new IllegalStateException("There is no available FrameworkAdmin for running system.");
		FrameworkAdmin runningFwAdmin = (FrameworkAdmin) context.getService(references[0]);
		Manipulator manipulator = runningFwAdmin.getRunningManipulator();

		System.out.println(manipulator.toString());

		// 3. Expect bundles state.
		BundleInfo[] bInfos = manipulator.getExpectedState();
		System.out.println("Running ExpectedState:");
		for (int i = 0; i < bInfos.length; i++)
			System.out.println(" " + bInfos[i]);

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

		bundleInfoListForRunningTest.add(new BundleInfo(getFullyQualifiedLocation(equinoxCommonBundle), 3, true));
		bundleInfoListForRunningTest.add(new BundleInfo(getFullyQualifiedLocation(this.applBundle), BundleInfo.NO_LEVEL, true));
		bundleInfoListForRunningTest.add(new BundleInfo(getFullyQualifiedLocation(coreRuntimeBundle), BundleInfo.NO_LEVEL, true));
		bundleInfoListForRunningTest.add(new BundleInfo(getFullyQualifiedLocation(this.jobsBundle), 1, true));
		bundleInfoListForRunningTest.add(new BundleInfo(getFullyQualifiedLocation(this.registryBundle), 1, true));
		bundleInfoListForRunningTest.add(new BundleInfo(getFullyQualifiedLocation(this.contenttypeBundle), 1, true));
		bundleInfoListForRunningTest.add(new BundleInfo(getFullyQualifiedLocation(this.preferencesBundle), 1, true));
		bundleInfoListForRunningTest.add(new BundleInfo(getFullyQualifiedLocation(this.osgiServicesBundle)));
		bundleInfoListForRunningTest.add(new BundleInfo(getFullyQualifiedLocation(Activator.frameworkAdminServiceBundle)));
		bundleInfoListForRunningTest.add(new BundleInfo(getFullyQualifiedLocation(this.fwadminEquinoxBundle), 1, true));
		bundleInfoListForRunningTest.add(new BundleInfo(getFullyQualifiedLocation(this.fwadminExamplesBundle), 4, true));
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
		System.out.println("configLocForRunningTest:" + this.configLocForRunningTest);
		System.out.println("jobsBundle:" + this.jobsBundle);
		System.out.println("preferencesBundle:" + this.preferencesBundle);
		System.out.println("applBundle:" + this.applBundle);
		System.out.println("fwadminEquinoxBundle:" + this.fwadminEquinoxBundle);
		System.out.println("fwadminExamplesBundle:" + this.fwadminExamplesBundle);
		System.out.println("osgiServicesBundle:" + this.osgiServicesBundle);
		System.out.println("");
	}

	private void readParameters(Properties props) {
		fwHome = new File(Activator.getValue(props, "equinox.home"));
		cwd = new File(Activator.getValue(props, "equinox.cwd"));

		eclipseExe = new File(Activator.getValue(props, "equinox.launcher"));

		bundlesDir = new File(fwHome, Activator.getValue(props, "equinox.bundlesDir"));
		fwBundle = Activator.getValue(props, "equinox.fw");
		equinoxCommonBundle = Activator.getValue(props, "equinox.bundles.equinox.common");
		coreRuntimeBundle = Activator.getValue(props, "equinox.bundles.core.runtime");
		updateConfiguratorBundle = Activator.getValue(props, "equinox.bundles.update.configurator");

		this.jobsBundle = Activator.getValue(props, "equinox.bundles.core.jobs");
		this.registryBundle = Activator.getValue(props, "equinox.bundles.equinox.registry");
		this.preferencesBundle = Activator.getValue(props, "equinox.bundles.equinox.preferences");;
		this.contenttypeBundle = Activator.getValue(props, "equinox.bundles.core.contenttype");;
		this.osgiServicesBundle = Activator.getValue(props, "equinox.bundles.osgi.services");;
		this.fwadminExamplesBundle = Activator.getValue(props, "equinox.bundles.equinox.frameworkadmin.examples");;
		this.fwadminEquinoxBundle = Activator.getValue(props, "equinox.bundles.equinox.frameworkadmin.equinox");
		this.applBundle = Activator.getValue(props, "equinox.bundles.eclipse.appl");

		//		equinox.bundles.equinox.frameworkadmin.knopflerfish=org.eclipse.equinox.frameworkadmin.knopflerfish_1.0.2.jar
		//		equinox.bundles.equinox.frameworkadmin.felix=org.eclipse.equinox.frameworkadmin.felix_1.0.2.jar

		fwJar = new File(fwHome, fwBundle);
		propsValueConsolePort = Activator.getValue(props, "equinox.console.port");
		configLoc = new File(fwHome, Activator.getValue(props, "equinox.configLoc"));
		configLocForRunningTest = new File(fwHome, Activator.getValue(props, "equinox.configLocForRunningTest"));

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
