package org.eclipse.equinox.frameworkadmin.examples;

import java.io.*;
import java.net.URL;
import java.util.*;

import org.eclipse.equinox.frameworkadmin.*;

/**
 * This is an example how a Java application configure an framework with bundles and launch it. 
 * 
 * Usage: just 
 *
 */
public class Main {
	private static EquinoxActivator equinox = null;
	private static Properties props = new Properties();

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		initialize();

		String className = props.getProperty("main.factoryclassname");
		FrameworkAdmin fwAdmin = FrameworkAdminFactory.getInstance(className);

		// After instanciating FrameworkAdmin object, completely same code can be used
		// as the case that you get the object from a service registry on OSGi framework.

		List bundleInfoList = equinox.bundleInfoListWithSimpleConfigurator;
		Manipulator manipulator = fwAdmin.getManipulator();

		ConfigData configData = manipulator.getConfigData();
		LauncherData launcherData = manipulator.getLauncherData();

		// 1. Set Parameters to LaunchData.
		launcherData.setJvm(new File(Activator.jvm));
		launcherData.setJvmArgs(new String[] {Activator.jvmArgs});
		launcherData.setFwPersistentDataLocation(equinox.configLoc, true);
		launcherData.setFwJar(equinox.fwJar);
		launcherData.setFwConfigLocation(equinox.configLoc);

		// 2. Set Parameters to ConfigData.
		for (Iterator ite = bundleInfoList.iterator(); ite.hasNext();) {
			BundleInfo bInfo = (BundleInfo) ite.next();
			configData.addBundle(bInfo);
		}
		configData.setBeginningFwStartLevel(6);
		configData.setInitialBundleStartLevel(5);
		configData.setFwDependentProp(EquinoxActivator.PROPS_KEY_CONSOLE_PORT, equinox.propsValueConsolePort);

		try {
			// 3. Save them.
			manipulator.save(false);
			System.out.println("Saved");
			// 4. Launch it.
			Process process = fwAdmin.launch(manipulator, equinox.cwd);
			System.out.println("Launched");
			InputStreamMonitorThread.monitorThreadStart(process, equinox.threadStandardI, equinox.threadErrorI);
			// Remark: How to communicate with launched framework is out of scope of FrameworkAdmin, so far.
		} catch (FrameworkAdminRuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Read properties from "setting.properties" file.
	 * They will be 
	 * 
	 * 
	 */
	private static void initialize() {
		Activator activator = new Activator();

		try {
			String path = Main.class.getName();
			path = (path.replace('.', '/'));
			path = path.substring(0, path.lastIndexOf('/'));
			path = path + "/" + "setting.properties";
			URL url = Main.class.getClassLoader().getResource(path);
			props.load(url.openStream());
			activator.readParameters(props);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		equinox = new EquinoxActivator(props);
	}
}
