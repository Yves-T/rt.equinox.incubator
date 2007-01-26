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

import java.io.*;
import java.net.URL;
import java.util.*;

import org.eclipse.equinox.frameworkadmin.*;
import org.eclipse.equinox.internal.frameworkadmin.utils.Utils;
import org.osgi.service.log.LogService;

public class FelixFwConfigFileParser {
	private static boolean DEBUG = false;

	private Properties getBasicProperties() throws IOException {
		Properties props = new Properties();
		InputStream is = null;

		String entry = Utils.replaceAll(this.getClass().getPackage().getName(), ".", "/") + "/" + FelixConstants.FELIX_BASIC_PROPERTIES_FILE;
		//		String entry = Utils.replaceAll(this.getClass().getName(), ".", "/") + ".class";
		URL url = Activator.context.getBundle().getResource(entry);

		try {
			is = url.openStream();
			props.load(is);
			//	} catch (IOException e) {
			//		throw new ManipulatorException("Fail to read file(URL=" + url + ")", e, ManipulatorException.OTHERS);
		} finally {
			//		try {
			is.close();
			//	} catch (IOException e) {
			//		e.printStackTrace();
			//	}
			is = null;
		}
		return props;
	}

	private Properties getConfigProps(BundleInfo[] bInfos, Manipulator manipulator, boolean relative) throws IOException {
		ConfigData configData = manipulator.getConfigData();
		LauncherData launcherData = manipulator.getLauncherData();

		// validate must be called. therefore, baseLocation must start with "file:"
		// and has absolute file path.

		Properties props = getBasicProperties();

		//		URL baseUrl = null;
		//		if (fwConfigInfo.baseLocation != null) {
		//			try {
		//				baseUrl = new URL(fwConfigInfo.baseLocation);
		//			} catch (MalformedURLException e) {
		//				//Never happen. ignore.
		//				e.printStackTrace();
		//			}
		//			if (baseUrl.getProtocol().equals("file")) {
		//				String urlFileSt = baseUrl.getFile();
		//				if (relative) {
		//					if (fwConfigInfo.fwJar == null || !fwConfigInfo.fwJar.getParentFile().equals(new File(urlFileSt)))
		//						props.setProperty(EquinoxConstants.PROP_SYSPATH, urlFileSt);
		//					else
		//						props.setProperty(EquinoxFwConfigInfo.PROP_ASSUMED_BASE_DIR, urlFileSt);
		//				} else {
		//					props.setProperty(EquinoxConstants.PROP_SYSPATH, urlFileSt);
		//				}
		//			}
		//		}
		props.setProperty(FelixConstants.PROP_BUNDLES_STARTLEVEL, Integer.toString(configData.getInitialBundleStartLevel()));
		props.setProperty(FelixConstants.PROP_INITIAL_STARTLEVEL, Integer.toString(configData.getBeginingFwStartLevel()));
		if (launcherData.getFwPersistentDataLocation() != null)
			props.setProperty(FelixConstants.PROP_FELIX_FWPRIVATE_DIR, launcherData.getFwPersistentDataLocation().getAbsolutePath());

		// Sort by startlevel for each bunldes to be installed and started.
		SortedMap installList = new TreeMap();
		SortedMap startList = new TreeMap();
		for (int i = 0; i < bInfos.length; i++) {
			Integer sL = Integer.valueOf(bInfos[i].getStartLevel());
			if (sL.intValue() == BundleInfo.NO_LEVEL)
				sL = new Integer(configData.getInitialBundleStartLevel());
			if (bInfos[i].isMarkedAsStarted()) {
				List list = (List) startList.get(sL);
				if (list == null) {
					list = new LinkedList();
					startList.put(sL, list);
				}
				list.add(bInfos[i]);
			} else {
				List list = (List) installList.get(sL);
				if (list == null) {
					list = new LinkedList();
					installList.put(sL, list);
				}
				list.add(bInfos[i]);
			}
		}
		//installList and startList are sorted by the key (StartLevel).

		setInstallingBundles(props, installList, FelixConstants.PROP_BUNDLE_INSTALL_PREFIX);
		setInstallingBundles(props, startList, FelixConstants.PROP_BUNDLE_START_PREFIX);

		props = Utils.appendProperties(props, configData.getFwDependentProps());

		//props.setProperty(EquinoxConstants.AOL, EquinoxConstants.AOL);
		return props;
	}

	private boolean isFwDependent(String key) {
		// TODO This algorithm is temporal. 
		if (key.startsWith(FelixConstants.PROP_FELIX_DEPENDENT_PREFIX))
			return true;
		return false;
	}

	void readFwConfigFile(Manipulator manipulator, File configDir) throws IOException {
		ConfigData configData = manipulator.getConfigData();
		LauncherData launcherData = manipulator.getLauncherData();
		File configFile = new File(configDir, FelixConstants.DEFAULT_FW_CONFIGFILE);
		File systemPropsFile = new File(configDir, FelixConstants.DEFAULT_SYSTEMPROP_CONFIGFILE);

		Properties props = new Properties();
		InputStream is = null;
		try {
			is = new FileInputStream(configFile);
			props.load(is);
		} finally {
			//		try {
			if (is != null)
				is.close();
			//	} catch (IOException e) {
			//e.printStackTrace();
			//}
			is = null;
		}

		if (DEBUG)
			Utils.printoutProperties(System.out, "props", props);

		// this.initialize();
		//		if (fwConfigInfo.fwJar == null)
		//			throw new ConfigManipulatorException("fwJar must be set in advance", ConfigManipulatorException.UNSATISFIED_CONDITION);
		for (Enumeration enum = props.keys(); enum.hasMoreElements();) {
			String key = (String) enum.nextElement();
			String value = props.getProperty(key);
			if (key.equals(FelixConstants.PROP_BUNDLES_STARTLEVEL))
				configData.setInitialBundleStartLevel(Integer.parseInt(value));
			else if (key.equals(FelixConstants.PROP_INITIAL_STARTLEVEL))
				configData.setBeginningFwStartLevel(Integer.parseInt(value));
			else if (key.startsWith(FelixConstants.PROP_BUNDLE_INSTALL_PREFIX) || key.startsWith(FelixConstants.PROP_BUNDLE_START_PREFIX)) {
				setInstallingBundles(configData, key, value);
			} else if (key.equals(FelixConstants.PROP_FELIX_FWPRIVATE_DIR))
				launcherData.setFwPersistentDataLocation(new File(value), false);
			else {
				//if (this.isFwDependent(key))
				configData.setFwDependentProp(key, value);
				//else
				//	configData.setFwIndependentProp(key, value);
			}
		}
		Log.log(LogService.LOG_INFO, "Config file(" + configFile.getAbsolutePath() + ") is read successfully.");

		// next System Properties
		props.clear();
		is = null;
		try {
			is = new FileInputStream(systemPropsFile);
			props.load(is);
		} finally {
			if (is != null)
				is.close();
			is = null;
		}

		if (DEBUG)
			Utils.printoutProperties(System.out, "props", props);
		configData.setFwIndependentProps(props);

		Log.log(LogService.LOG_INFO, "System Props file(" + systemPropsFile.getAbsolutePath() + ") is read successfully.");

	}

	void saveConfigs(BundleInfo[] bInfos, Manipulator manipulator, File configDir, boolean backup, boolean relative) throws IOException {
		String header = "This properties were written by " + this.getClass().getName();

		Properties configProps = this.getConfigProps(bInfos, manipulator, relative);
		if (configProps == null || configProps.size() == 0) {
			Log.log(LogService.LOG_WARNING, this, "saveConfigs(File) ", "configProps is empty");
			return;
		}
		Utils.createParentDir(configDir);

		if (DEBUG)
			Utils.printoutProperties(System.out, "configProps", configProps);
		// Properties newProps = reverseProps(configProps);

		File configFile = new File(configDir, FelixConstants.DEFAULT_FW_CONFIGFILE);
		File systemPropsFile = new File(configDir, FelixConstants.DEFAULT_SYSTEMPROP_CONFIGFILE);

		// for config file.
		if (backup)
			if (configFile.exists()) {
				File dest = Utils.getSimpleDataFormattedFile(configFile);
				if (!configFile.renameTo(dest))
					throw new IOException("Fail to rename from (" + configFile + ") to (" + dest + ")");
				Log.log(LogService.LOG_INFO, this, "saveConfigs()", "Succeed to rename from (" + configFile + ") to (" + dest + ")");
			}

		FileOutputStream out = null;
		try {
			out = new FileOutputStream(configFile);
			configProps.store(out, header);
			Log.log(LogService.LOG_INFO, "configProps is stored successfully.");
			//} catch (SecurityException se) {
			//	throw new ManipulatorException("File " + outputFile + " cannot be created because of lack of permission", se, ManipulatorException.OTHERS);
			//} catch (FileNotFoundException fnfe) {
			//	throw new ManipulatorException("File " + outputFile + " cannot be found", fnfe, ManipulatorException.OTHERS);
			//} catch (IOException ioe) {
			//	throw new ManipulatorException("Error occured during writing File " + outputFile, ioe, ManipulatorException.OTHERS);
		} finally {
			//	try {
			out.flush();
			out.close();
			Log.log(LogService.LOG_INFO, "out is closed successfully.");
			//	} catch (IOException e) {
			//		e.printStackTrace();
			//	}
			out = null;
		}
		// for system props file.
		if (backup)
			if (systemPropsFile.exists()) {
				File dest = Utils.getSimpleDataFormattedFile(systemPropsFile);
				if (!systemPropsFile.renameTo(dest))
					throw new IOException("Fail to rename from (" + configFile + ") to (" + dest + ")");
				Log.log(LogService.LOG_INFO, this, "saveConfigs()", "Succeed to rename from (" + configFile + ") to (" + dest + ")");
			}

		out = null;
		try {
			out = new FileOutputStream(systemPropsFile);
			ConfigData configData = manipulator.getConfigData();
			configData.getFwIndependentProps().store(out, header);
			Log.log(LogService.LOG_INFO, "system props are stored successfully.");
		} finally {
			out.flush();
			out.close();
			Log.log(LogService.LOG_INFO, "out is closed successfully.");
			out = null;
		}
	}

	private void setInstallingBundles(ConfigData configData, String key, String value) throws IOException {
		int sl = BundleInfo.NO_LEVEL;
		boolean toBeStarted = false;
		//String location = null;
		try {
			if (key.startsWith(FelixConstants.PROP_BUNDLE_INSTALL_PREFIX)) {
				sl = Integer.parseInt(key.substring(FelixConstants.PROP_BUNDLE_INSTALL_PREFIX.length()));
			} else if (key.startsWith(FelixConstants.PROP_BUNDLE_START_PREFIX)) {
				sl = Integer.parseInt(key.substring(FelixConstants.PROP_BUNDLE_START_PREFIX.length()));
				toBeStarted = true;
			} else
				return;
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Invalid Format of ServiceLevel:key=" + key + " value=" + value, e);
		}

		if (value == null)
			throw new IllegalArgumentException("Invalid Format of ServiceLevel:key=" + key + " value=" + value);
		String[] locations = Utils.getTokens(value, " ");

		for (int i = 0; i < locations.length; i++)
			configData.addBundle((new BundleInfo(locations[i], sl, toBeStarted)));
	}

	private void setInstallingBundles(Properties props, SortedMap bundleList, String keyPrefix) {
		for (Iterator ite = bundleList.keySet().iterator(); ite.hasNext();) {
			Integer sl = (Integer) ite.next();
			List list = (List) bundleList.get(sl);
			String key = keyPrefix + sl.toString();
			StringBuffer sb = new StringBuffer();
			for (Iterator ite2 = list.iterator(); ite2.hasNext();) {
				BundleInfo bInfo = (BundleInfo) ite2.next();
				sb.append(bInfo.getLocation());
				if (ite2.hasNext())
					sb.append(" ");
			}
			props.setProperty(key, sb.toString());
		}
	}

}
