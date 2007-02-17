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

import java.io.*;
import java.net.URL;
import java.util.Properties;

import org.eclipse.equinox.frameworkadmin.*;
import org.osgi.framework.*;

/**
 * This bundle shows how to use @{link FrameworkAdmin}.
 * 
 * You should set proper values in setting.properties file in this bundle.
 */
public class Activator implements BundleActivator {

	static String frameworkAdminServiceBundle;

	static String simpleConfiguratorBundle;
	static String jvmArgs;
	static String jvm;

	/**
	 * Return value for the key.
	 * If value for the key, throw IllegalStateException.
	 * 	
	 * @param key
	 * @return value 
	 */
	static String getValue(Properties props, String key) {
		String value = props.getProperty(key);
		if (value == null)
			throw new IllegalStateException("\"" + key + "\" should be set.");
		return value;
	}

	EquinoxActivator equinox;

	KfActivator knopflerfish;
	FelixActivator felix;

	private int mode;

	private void convertConfigFromKfToEquinoxAndLaunch() throws IOException {
		// 1. save config for Knopflerfish.
		knopflerfish.save(knopflerfish.configLoc, KfActivator.bundleInfoListWoSimpleConfigurator, false);

		// 2. load config from the config location.
		Manipulator kfManipulator = knopflerfish.fwAdmin.getManipulator();
		LauncherData kfLauncherData = kfManipulator.getLauncherData();
		kfLauncherData.setFwConfigLocation(knopflerfish.configLoc);
		kfManipulator.load();

		// 3. create new instance of manipulator.
		Manipulator eqManipulator = equinox.fwAdmin.getManipulator();
		// 4. copy parameters from the one read from config files for knopflerfish.
		eqManipulator.setConfigData(kfManipulator.getConfigData());
		eqManipulator.setLauncherData(kfManipulator.getLauncherData());
		ConfigData eqConfigData = eqManipulator.getConfigData();
		LauncherData eqLauncherData = eqManipulator.getLauncherData();

		// jvm and jvmargs must not be set for knopflerfish.
		eqLauncherData.setJvm(new File(Activator.jvm));
		String[] jvmArgs = {Activator.jvmArgs};
		eqLauncherData.setJvmArgs(jvmArgs);

		// Overwrite equinox and instance specific parameters.		
		eqLauncherData.setFwJar(equinox.fwJar);
		eqLauncherData.setFwPersistentDataLocation(equinox.configLoc, true);
		eqLauncherData.setFwConfigLocation(equinox.configLoc);

		// for debugging, set console port.
		eqConfigData.setFwDependentProp(EquinoxActivator.PROPS_KEY_CONSOLE_PORT, equinox.propsValueConsolePort);

		// 7. Expect bundles state
		BundleInfo[] bInfos = eqManipulator.getExpectedState();
		System.out.println("ExpectedState:");
		for (int i = 0; i < bInfos.length; i++)
			System.out.println(" " + bInfos[i]);

		// 8 save config for equinox.
		eqManipulator.save(true);

		// 9. launch it.
		equinox.launch(eqManipulator, equinox.cwd);
	}

	private Properties initialize(BundleContext context) throws InvalidSyntaxException, IOException {
		Properties props = new Properties();
		String path = this.getClass().getName();
		path = path.replace('.', '/');
		path = path.substring(0, path.lastIndexOf('/'));
		path = path + "/" + "setting.properties";
		URL url = context.getBundle().getResource(path);
		if (url == null) {
			RuntimeException ex = new IllegalStateException("Error!!:bundle.getEntry(" + "setting.properties" + ")==null. Check if \"" + "setting.properties" + " exists in the Bundle.\" ");
			ex.printStackTrace();
			throw ex;
		}
		InputStream inputStream = url.openStream();
		props.load(inputStream);

		this.readParameters(props);
		return props;
	}

	private void printout() {
		System.out.println("######################\nActivator");
		System.out.println("mode:" + mode);
		System.out.println("jvm:" + jvm);
		System.out.println("jvmArgs:" + jvmArgs);
		System.out.println("simpleConfiguratorBundle:" + simpleConfiguratorBundle);
		System.out.println("frameworkAdminServiceBundle:" + frameworkAdminServiceBundle);
		System.out.println("");
	}

	void readParameters(Properties props) {
		String value = getValue(props, "mode");
		try {
			mode = Integer.parseInt(value);
		} catch (NumberFormatException nfe) {
			throw new IllegalStateException("\"mode\" should be set.", nfe);
		}

		value = props.getProperty("jvm.location");
		jvm = value;
		jvmArgs = props.getProperty("jvm.args");

		simpleConfiguratorBundle = getValue(props, "general.bundles.simpleconfigurator");
		frameworkAdminServiceBundle = getValue(props, "general.bundles.frameworkadmin");
		printout();
	}

	public void start(BundleContext context) throws Exception {
		try {
			Properties props = initialize(context);
			System.out.println("mode = " + mode);
			equinox = new EquinoxActivator(context, props);
			equinox.start();
			switch (mode) {
				case 0 :
					equinox.equinoxSaveAndLaunch(equinox.bundleInfoListWoSimpleConfigurator, false);
					break;
				case 1 :
					equinox.equinoxSaveAndLaunch(equinox.bundleInfoListWithSimpleConfigurator, false);
					break;
				case 2 :
					equinox.eclipseSaveAndLaunch(equinox.bundleInfoListEclipse, false);
					break;
				case 3 :
					knopflerfish = new KfActivator(context, props);
					knopflerfish.start();
					knopflerfish.kfSaveAndLaunch(KfActivator.bundleInfoListWoSimpleConfigurator, false);
					break;
				case 4 :
					knopflerfish = new KfActivator(context, props);
					knopflerfish.start();
					knopflerfish.kfSaveAndLaunch(KfActivator.bundleInfoListWithSimpleConfigurator, false);
					break;
				case 5 :
					knopflerfish = new KfActivator(context, props);
					knopflerfish.start();
					convertConfigFromKfToEquinoxAndLaunch();
					break;
				case 6 :
					felix = new FelixActivator(context, props);
					felix.start();
					felix.felixSaveAndLaunch(FelixActivator.bundleInfoListWoSimpleConfigurator, false);
					break;
				case 7 :
					equinox.equinoxSaveAndGetState(equinox.bundleInfoListWoSimpleConfigurator, false);
					break;
				case 8 :

					equinox.equinoxSaveAndLaunch(equinox.bundleInfoListWithSimpleConfigurator, false);
					equinox.equinoxGetState();
					break;
				case 100 :
					equinox.equinoxGetRunningState();
					break;
				case 101 :
					Manipulator manipulator = equinox.equinoxSetAndSaveForRunningTest();
					equinox.launch(manipulator, equinox.cwd);
					break;
				default :
					break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		if (equinox != null)
			equinox.stop();
		if (knopflerfish != null)
			knopflerfish.stop();
		if (felix != null)
			felix.stop();
	}
}
