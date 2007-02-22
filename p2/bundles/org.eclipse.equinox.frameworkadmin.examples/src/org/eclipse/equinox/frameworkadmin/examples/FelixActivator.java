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
import java.net.URL;
import java.util.*;

import org.eclipse.equinox.frameworkadmin.*;
import org.osgi.framework.*;
import org.osgi.util.tracker.ServiceTracker;

public class FelixActivator {

	private final static String FILTER_OBJECTCLASS = "(" + Constants.OBJECTCLASS + "=" + FrameworkAdmin.class.getName() + ")";
	static List bundleInfoListWoSimpleConfigurator = new LinkedList();
	List bundleInfoListWithSimpleConfigurator = new LinkedList();
	private File fwHome;

	File fwJar;
	File configLoc;
	File cwd;
	private String bundlesDir;

	File fwPersistentDataLoc;

	private String filterFwAdmin;
	private List bundlesList = new LinkedList();

	private BundleContext context;
	private Process process = null;
	private ServiceTracker fwAdminTracker;
	private FrameworkAdmin fwAdmin;

	InputStreamMonitorThread threadStandardI = null;
	InputStreamMonitorThread threadErrorI = null;
	private final int initialBundleSl = 7;

	private final int beginningFwSl = 7;

	FelixActivator(Properties props) {
		this(null, props);
	}

	FelixActivator(BundleContext context, Properties props) {
		this.context = context;
		this.context = context;
		this.readParameters(props);
		initializeBundlesList();
	}

	void felixSaveAndLaunch(List bundleList, boolean backup) throws InvalidSyntaxException, IOException {
		Manipulator manipulator = fwAdmin.getManipulator();
		ConfigData configData = manipulator.getConfigData();
		LauncherData launcherData = manipulator.getLauncherData();

		// 1. Set Parameters to LaunchData.
		launcherData.setFwJar(fwJar);
		launcherData.setFwConfigLocation(configLoc);
		launcherData.setFwPersistentDataLocation(fwPersistentDataLoc, true);

		// 2. Set Parameters to ConfigData.
		configData.setBeginningFwStartLevel(this.beginningFwSl);
		configData.setInitialBundleStartLevel(this.initialBundleSl);
		for (Iterator ite = bundleList.iterator(); ite.hasNext();) {
			BundleInfo bundleInfo = (BundleInfo) ite.next();
			configData.addBundle(bundleInfo);
		}
		manipulator.save(backup);
		launcherData.setJvm(new File(Activator.jvm));
		String[] jvmArgs = {Activator.jvmArgs};
		launcherData.setJvmArgs(jvmArgs);
		launcherData.setFwPersistentDataLocation(fwPersistentDataLoc, true);
		launcherData.setFwConfigLocation(configLoc);
		process = fwAdmin.launch(manipulator, cwd);
		InputStreamMonitorThread.monitorThreadStart(process, threadStandardI, threadErrorI);
	}

	private void initializeBundlesList() {

		int sl = 0;
		for (Iterator ite = bundlesList.iterator(); ite.hasNext();) {
			sl++;
			String[] bundles = (String[]) ite.next();
			for (int i = 0; i < bundles.length; i++) {
				URL url;
				try {
					url = new URL("file", null, (new File(fwHome, bundlesDir + "/" + bundles[i]).getAbsolutePath()));
					bundleInfoListWoSimpleConfigurator.add(new BundleInfo(url.toExternalForm(), sl, true));
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		bundleInfoListWithSimpleConfigurator.addAll(EquinoxActivator.bundleInfoListSimpleConfigurator);

		bundleInfoListWithSimpleConfigurator.addAll(bundleInfoListWoSimpleConfigurator);
		try {
			bundleInfoListWithSimpleConfigurator.add(new BundleInfo(fwJar.toURL().toExternalForm(), 1, true, 0));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void printoutParameters() {
		System.out.println("######################\nFelixActivator");
		System.out.println("fwHome:" + fwHome);
		System.out.println("cwd:" + cwd);
		System.out.println("bundlesDir:" + bundlesDir);
		System.out.println("fwJar:" + fwJar);
		System.out.println("configLoc:" + configLoc);
		System.out.println("fwPersistentDataLoc:" + fwPersistentDataLoc);
		System.out.println("");
	}

	private void readParameters(Properties props) {
		fwHome = new File(Activator.getValue(props, "felix.home"));
		cwd = fwHome;
		String value = Activator.getValue(props, "felix.bundlesDir");
		bundlesDir = value.endsWith(File.separator) ? value : value + File.separator;
		configLoc = new File(fwHome, Activator.getValue(props, "felix.configLoc"));
		fwPersistentDataLoc = new File(fwHome, Activator.getValue(props, "felix.fwPersistentDataLoc"));
		fwJar = new File(fwHome, Activator.getValue(props, "felix.fw"));
		for (int i = 1; i < 3; i++) {
			try {
				value = Activator.getValue(props, "felix.bundles." + String.valueOf(i) + ".start");
				bundlesList.add(KfActivator.getStringArray(value));
			} catch (IllegalStateException e) {
				e.printStackTrace();
			}
		}
		String filterFwName = "(" + FrameworkAdmin.SERVICE_PROP_KEY_FW_NAME + "=" + props.getProperty("felix.fw.name") + ")";
		String filterFwVersion = "(" + FrameworkAdmin.SERVICE_PROP_KEY_FW_VERSION + "=" + props.getProperty("felix.fw.version") + ")";
		String filterLauncherName = "(" + FrameworkAdmin.SERVICE_PROP_KEY_LAUNCHER_NAME + "=" + props.getProperty("felix.launcher.name") + ")";
		String filterLauncherVersion = "(" + FrameworkAdmin.SERVICE_PROP_KEY_LAUNCHER_VERSION + "=" + props.getProperty("felix.launcher.version") + ")";
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

	public void stop() throws Exception {
		this.context = null;
		fwAdminTracker.close();
		fwAdminTracker = null;
		this.fwAdmin = null;
		InputStreamMonitorThread.stopProcess(process, threadStandardI, threadErrorI);
	}
}
