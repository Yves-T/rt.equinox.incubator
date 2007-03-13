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
import org.eclipse.equinox.internal.frameworkadmin.utils.Utils;
import org.osgi.framework.*;
import org.osgi.util.tracker.ServiceTracker;

public class KfActivator {

	private final static String FILTER_OBJECTCLASS = "(" + Constants.OBJECTCLASS + "=" + FrameworkAdmin.class.getName() + ")";
	static List bundleInfoListWoSimpleConfigurator = new LinkedList();
	static List bundleInfoListWithSimpleConfigurator = new LinkedList();

	static String[] getStringArray(String value) {
		StringTokenizer tokenizer = new StringTokenizer(value, ",");
		List list = new ArrayList(tokenizer.countTokens());
		while (tokenizer.hasMoreTokens()) {
			list.add(tokenizer.nextToken());
		}
		String[] ret = new String[list.size()];
		list.toArray(ret);
		return ret;
	}

	File configLoc;

	private File fwHome;
	File fwJar;
	File cwd;
	private String bundlesDir;
	File fwPersistentDataLoc;

	private String filterFwAdmin;

	private BundleContext context;
	private Process process = null;
	private ServiceTracker fwAdminTracker;

	FrameworkAdmin fwAdmin;
	InputStreamMonitorThread threadStandardI = null;
	InputStreamMonitorThread threadErrorI = null;

	private List bundlesList = new LinkedList();
	private final int initialBundleSl = 7;

	private final int beginningFwSl = 7;

	KfActivator(Properties props) {
		this(null, props);
	}

	KfActivator(BundleContext context, Properties props) {
		this.context = context;
		this.readParameters(props);
		initializeBundlesList();
	}

	private void initializeBundlesList() {

		int sl = 0;
		for (Iterator ite = bundlesList.iterator(); ite.hasNext();) {
			sl++;
			String[] bundles = (String[]) ite.next();
			for (int i = 0; i < bundles.length; i++) {
				try {
					URL url = Utils.getUrl("file", null, (new File(fwHome, bundlesDir + "/" + bundles[i]).getAbsolutePath()));
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
			bundleInfoListWithSimpleConfigurator.add(new BundleInfo(fwJar.toURL().toExternalForm(), 0, true, 0));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	void kfSaveAndLaunch(List bundleList, boolean backup) throws IOException {
		Manipulator manipulator = fwAdmin.getManipulator();
		ConfigData configData = manipulator.getConfigData();
		LauncherData launcherData = manipulator.getLauncherData();

		// 1. Set Parameters to LaunchData.
		launcherData.setJvm(new File(Activator.jvm));
		String[] jvmArgs = {Activator.jvmArgs};
		launcherData.setJvmArgs(jvmArgs);
		launcherData.setFwJar(fwJar);
		launcherData.setFwPersistentDataLocation(fwPersistentDataLoc, true);
		launcherData.setFwConfigLocation(configLoc);

		// 2. Set Parameters to ConfigData.
		configData.setBeginningFwStartLevel(this.beginningFwSl);
		configData.setInitialBundleStartLevel(this.initialBundleSl);
		for (Iterator ite = bundleList.iterator(); ite.hasNext();) {
			BundleInfo bundleInfo = (BundleInfo) ite.next();
			configData.addBundle(bundleInfo);
			//	System.out.println(bundleInfo);
		}
		//	System.out.println("configFile=" + configFile);
		// 4. Save them.
		manipulator.save(backup);

		// 5. Launch it.
		process = fwAdmin.launch(manipulator, cwd);
		InputStreamMonitorThread.monitorThreadStart(process, threadStandardI, threadErrorI);
	}

	public void load(File configFile) {
		Manipulator manipulator = fwAdmin.getManipulator();
		//ConfigData configData = manipulator.getConfigData();
		LauncherData launcherData = manipulator.getLauncherData();
		// 1. set required parameters to load.
		launcherData.setFwConfigLocation(configFile);
		try {
			manipulator.load();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FrameworkAdminRuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
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
		fwHome = new File(Activator.getValue(props, "knopflerfish.home"));
		cwd = new File(Activator.getValue(props, "knopflerfish.cwd"));
		String value = Activator.getValue(props, "knopflerfish.bundlesDir");
		bundlesDir = value.endsWith(File.separator) ? value : value + File.separator;
		configLoc = new File(fwHome, Activator.getValue(props, "knopflerfish.configLoc"));
		fwPersistentDataLoc = new File(fwHome, Activator.getValue(props, "knopflerfish.fwPersistentDataLoc"));
		fwJar = new File(fwHome, Activator.getValue(props, "knopflerfish.fw"));
		for (int i = 1; i < 7; i++) {
			try {
				value = Activator.getValue(props, "knopflerfish.bundles." + String.valueOf(i) + ".start");
				bundlesList.add(getStringArray(value));
			} catch (IllegalStateException e) {
				e.printStackTrace();
			}
		}
		String filterFwName = "(" + FrameworkAdmin.SERVICE_PROP_KEY_FW_NAME + "=" + props.getProperty("knopflerfish.fw.name") + ")";
		String filterFwVersion = "(" + FrameworkAdmin.SERVICE_PROP_KEY_FW_VERSION + "=" + props.getProperty("knopflerfish.fw.version") + ")";
		String filterLauncherName = "(" + FrameworkAdmin.SERVICE_PROP_KEY_LAUNCHER_NAME + "=" + props.getProperty("knopflerfish.launcher.name") + ")";
		String filterLauncherVersion = "(" + FrameworkAdmin.SERVICE_PROP_KEY_LAUNCHER_VERSION + "=" + props.getProperty("knopflerfish.launcher.version") + ")";
		filterFwAdmin = "(&" + FILTER_OBJECTCLASS + filterFwName + filterFwVersion + filterLauncherName + filterLauncherVersion + ")";

		printoutParameters();
	}

	public void save(File configFile, List bundleList, boolean backup) throws IOException {
		Manipulator manipulator = fwAdmin.getManipulator();
		ConfigData configData = manipulator.getConfigData();
		LauncherData launcherData = manipulator.getLauncherData();

		// 1. Set Parameters to LaunchData.
		launcherData.setJvm(new File(Activator.jvm));
		String[] jvmArgs = {Activator.jvmArgs};
		launcherData.setJvmArgs(jvmArgs);
		launcherData.setFwJar(fwJar);
		launcherData.setFwPersistentDataLocation(fwPersistentDataLoc, true);
		launcherData.setFwConfigLocation(configFile);

		// 2. Set Parameters to ConfigData.
		configData.setBeginningFwStartLevel(this.beginningFwSl);
		configData.setInitialBundleStartLevel(this.initialBundleSl);
		for (Iterator ite = bundleList.iterator(); ite.hasNext();) {
			BundleInfo bundleInfo = (BundleInfo) ite.next();
			configData.addBundle(bundleInfo);
			//	System.out.println(bundleInfo);
		}
		//	System.out.println("configFile=" + configFile);
		// 4. Save them.
		manipulator.save(backup);
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
		if (fwAdminTracker != null)
			fwAdminTracker.close();
		fwAdminTracker = null;
		this.fwAdmin = null;
		InputStreamMonitorThread.stopProcess(process, threadStandardI, threadErrorI);
	}
}
