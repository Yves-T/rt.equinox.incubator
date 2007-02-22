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
	private static FelixActivator felix = null;
	private static KfActivator knopflerfish = null;
	private static Properties props = new Properties();

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		initialize();
		int mode = Integer.parseInt(props.getProperty("main.mode"));

		switch (mode) {
			case 0 :
				launchEquinox();
				break;
			case 1 :
				launchKnopflerfish();
				break;
			case 2 :
				launchFelix();
				break;
			default :
				break;
		}
	}

	private static void launchEquinox() {
		FrameworkAdmin fwAdmin = null;
		String configuratorManipulatorFactoryClassName = props.getProperty("main.configuratorManipulatorFactoryClassName");
		String frameworkAdminFactoryClassName = props.getProperty("main.equinox.frameworkAdminFactoryClassName");

		try {

			//			// Method 1: set both implementation class names into arguments.
			//			fwAdmin = FrameworkAdminFactory.getInstance(frameworkAdminFactoryClassName, configuratorManipulatorFactoryClassName);

			//			// Method 2:	set both magic system properties in advance.	
			//			System.setProperty("org.eclipse.equinox.frameworkadmin.frameworkAdminFactory", frameworkAdminFactoryClassName);
			//			System.setProperty("org.eclipse.equinox.frameworkadmin.configuratorManipulatorFactory", configuratorManipulatorFactoryClassName);//		FrameworkAdmin fwAdmin = FrameworkAdminFactory.getInstance(className);
			//			fwAdmin = FrameworkAdminFactory.getInstance();

			// Method 3: set ConfiguratorManipulatorFactory magic system property in advance.
			//           and specify FrameworkAdminFactoryImplementation as an argument.
			System.setProperty("org.eclipse.equinox.frameworkadmin.configuratorManipulatorFactory", configuratorManipulatorFactoryClassName);//		FrameworkAdmin fwAdmin = FrameworkAdminFactory.getInstance(className);
			fwAdmin = FrameworkAdminFactory.getInstance(frameworkAdminFactoryClassName);
		} catch (InstantiationException e) {
			System.err.println("frameworkAdminFactoryClassName=" + frameworkAdminFactoryClassName);
			System.err.println("configuratorManipulatorFactoryClassName=" + configuratorManipulatorFactoryClassName);
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			System.err.println("frameworkAdminFactoryClassName=" + frameworkAdminFactoryClassName);
			System.err.println("configuratorManipulatorFactoryClassName=" + configuratorManipulatorFactoryClassName);
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			System.err.println("frameworkAdminFactoryClassName=" + frameworkAdminFactoryClassName);
			System.err.println("configuratorManipulatorFactoryClassName=" + configuratorManipulatorFactoryClassName);
			e.printStackTrace();
		}

		if (fwAdmin == null)
			return;

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

	private static void launchKnopflerfish() {
		FrameworkAdmin fwAdmin = null;
		String configuratorManipulatorFactoryClassName = props.getProperty("main.configuratorManipulatorFactoryClassName");
		String frameworkAdminFactoryClassName = props.getProperty("main.knopflerfish.frameworkAdminFactoryClassName");

		try {

			//			// Method 1: set both implementation class names into arguments.
			//			fwAdmin = FrameworkAdminFactory.getInstance(frameworkAdminFactoryClassName, configuratorManipulatorFactoryClassName);

			//			// Method 2:	set both magic system properties in advance.	
			//			System.setProperty("org.eclipse.equinox.frameworkadmin.frameworkAdminFactory", frameworkAdminFactoryClassName);
			//			System.setProperty("org.eclipse.equinox.frameworkadmin.configuratorManipulatorFactory", configuratorManipulatorFactoryClassName);//		FrameworkAdmin fwAdmin = FrameworkAdminFactory.getInstance(className);
			//			fwAdmin = FrameworkAdminFactory.getInstance();

			// Method 3: set ConfiguratorManipulatorFactory magic system property in advance.
			//           and specify FrameworkAdminFactoryImplementation as an argument.
			System.setProperty("org.eclipse.equinox.frameworkadmin.configuratorManipulatorFactory", configuratorManipulatorFactoryClassName);//		FrameworkAdmin fwAdmin = FrameworkAdminFactory.getInstance(className);
			fwAdmin = FrameworkAdminFactory.getInstance(frameworkAdminFactoryClassName);
		} catch (InstantiationException e) {
			System.err.println("frameworkAdminFactoryClassName=" + frameworkAdminFactoryClassName);
			System.err.println("configuratorManipulatorFactoryClassName=" + configuratorManipulatorFactoryClassName);
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			System.err.println("frameworkAdminFactoryClassName=" + frameworkAdminFactoryClassName);
			System.err.println("configuratorManipulatorFactoryClassName=" + configuratorManipulatorFactoryClassName);
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			System.err.println("frameworkAdminFactoryClassName=" + frameworkAdminFactoryClassName);
			System.err.println("configuratorManipulatorFactoryClassName=" + configuratorManipulatorFactoryClassName);
			e.printStackTrace();
		}

		if (fwAdmin == null)
			return;

		// After instanciating FrameworkAdmin object, completely same code can be used
		// as the case that you get the object from a service registry on OSGi framework.

		//List bundleInfoList = felix.bundleInfoListWithSimpleConfigurator;
		List bundleInfoList = KfActivator.bundleInfoListWithSimpleConfigurator;
		Manipulator manipulator = fwAdmin.getManipulator();

		ConfigData configData = manipulator.getConfigData();
		LauncherData launcherData = manipulator.getLauncherData();

		// 1. Set Parameters to LaunchData.
		launcherData.setJvm(new File(Activator.jvm));
		launcherData.setJvmArgs(new String[] {Activator.jvmArgs});
		launcherData.setFwPersistentDataLocation(knopflerfish.fwPersistentDataLoc, true);
		launcherData.setFwJar(knopflerfish.fwJar);
		launcherData.setFwConfigLocation(knopflerfish.configLoc);

		// 2. Set Parameters to ConfigData.
		for (Iterator ite = bundleInfoList.iterator(); ite.hasNext();) {
			BundleInfo bInfo = (BundleInfo) ite.next();
			configData.addBundle(bInfo);
		}
		configData.setBeginningFwStartLevel(7);
		configData.setInitialBundleStartLevel(7);
		//		configData.setFwDependentProp(EquinoxActivator.PROPS_KEY_CONSOLE_PORT, equinox.propsValueConsolePort);

		try {
			// 3. Save them.
			manipulator.save(false);
			System.out.println("Saved");
			// 4. Launch it.
			Process process = fwAdmin.launch(manipulator, knopflerfish.cwd);
			System.out.println("Launched");
			InputStreamMonitorThread.monitorThreadStart(process, knopflerfish.threadStandardI, knopflerfish.threadErrorI);
			// Remark: How to communicate with launched framework is out of scope of FrameworkAdmin, so far.
		} catch (FrameworkAdminRuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void launchFelix() {
		FrameworkAdmin fwAdmin = null;
		String configuratorManipulatorFactoryClassName = props.getProperty("main.configuratorManipulatorFactoryClassName");
		String frameworkAdminFactoryClassName = props.getProperty("main.felix.frameworkAdminFactoryClassName");

		try {

			//			// Method 1: set both implementation class names into arguments.
			//			fwAdmin = FrameworkAdminFactory.getInstance(frameworkAdminFactoryClassName, configuratorManipulatorFactoryClassName);

			//			// Method 2:	set both magic system properties in advance.	
			//			System.setProperty("org.eclipse.equinox.frameworkadmin.frameworkAdminFactory", frameworkAdminFactoryClassName);
			//			System.setProperty("org.eclipse.equinox.frameworkadmin.configuratorManipulatorFactory", configuratorManipulatorFactoryClassName);//		FrameworkAdmin fwAdmin = FrameworkAdminFactory.getInstance(className);
			//			fwAdmin = FrameworkAdminFactory.getInstance();

			// Method 3: set ConfiguratorManipulatorFactory magic system property in advance.
			//           and specify FrameworkAdminFactoryImplementation as an argument.
			System.setProperty("org.eclipse.equinox.frameworkadmin.configuratorManipulatorFactory", configuratorManipulatorFactoryClassName);//		FrameworkAdmin fwAdmin = FrameworkAdminFactory.getInstance(className);
			fwAdmin = FrameworkAdminFactory.getInstance(frameworkAdminFactoryClassName);
		} catch (InstantiationException e) {
			System.err.println("frameworkAdminFactoryClassName=" + frameworkAdminFactoryClassName);
			System.err.println("configuratorManipulatorFactoryClassName=" + configuratorManipulatorFactoryClassName);
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			System.err.println("frameworkAdminFactoryClassName=" + frameworkAdminFactoryClassName);
			System.err.println("configuratorManipulatorFactoryClassName=" + configuratorManipulatorFactoryClassName);
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			System.err.println("frameworkAdminFactoryClassName=" + frameworkAdminFactoryClassName);
			System.err.println("configuratorManipulatorFactoryClassName=" + configuratorManipulatorFactoryClassName);
			e.printStackTrace();
		}

		if (fwAdmin == null)
			return;

		// After instanciating FrameworkAdmin object, completely same code can be used
		// as the case that you get the object from a service registry on OSGi framework.

		//List bundleInfoList = felix.bundleInfoListWithSimpleConfigurator;
		List bundleInfoList = FelixActivator.bundleInfoListWoSimpleConfigurator;
		Manipulator manipulator = fwAdmin.getManipulator();

		ConfigData configData = manipulator.getConfigData();
		LauncherData launcherData = manipulator.getLauncherData();

		// 1. Set Parameters to LaunchData.
		launcherData.setJvm(new File(Activator.jvm));
		launcherData.setJvmArgs(new String[] {Activator.jvmArgs});
		launcherData.setFwPersistentDataLocation(felix.fwPersistentDataLoc, true);
		launcherData.setFwJar(felix.fwJar);
		launcherData.setFwConfigLocation(felix.configLoc);

		// 2. Set Parameters to ConfigData.
		for (Iterator ite = bundleInfoList.iterator(); ite.hasNext();) {
			BundleInfo bInfo = (BundleInfo) ite.next();
			configData.addBundle(bInfo);
		}
		configData.setBeginningFwStartLevel(8);
		configData.setInitialBundleStartLevel(7);
		//		configData.setFwDependentProp(EquinoxActivator.PROPS_KEY_CONSOLE_PORT, equinox.propsValueConsolePort);

		try {
			// 3. Save them.
			manipulator.save(false);
			System.out.println("Saved");
			// 4. Launch it.
			Process process = fwAdmin.launch(manipulator, felix.cwd);
			System.out.println("Launched");
			InputStreamMonitorThread.monitorThreadStart(process, felix.threadStandardI, felix.threadErrorI);
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
		felix = new FelixActivator(props);
		knopflerfish = new KfActivator(props);
	}
}
