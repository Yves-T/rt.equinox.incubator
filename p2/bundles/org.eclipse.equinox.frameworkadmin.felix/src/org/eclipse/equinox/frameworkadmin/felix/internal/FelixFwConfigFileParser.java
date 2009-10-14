/*******************************************************************************
 * Copyright (c) 2007, 2009 IBM Corporation and others.
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
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.equinox.internal.frameworkadmin.utils.Utils;
import org.eclipse.equinox.internal.provisional.frameworkadmin.*;

public class FelixFwConfigFileParser {

	/*
	 * Read and return a properties file from the given file.
	 */
	private static Properties readProperties(File file) throws IOException {
		Properties result = new Properties();
		InputStream input = null;
		try {
			input = new BufferedInputStream(new FileInputStream(file));
			result.load(input);
		} finally {
			if (input != null)
				try {
					input.close();
				} catch (IOException e) {
					// ignore
				}
		}
		return result;
	}

	/*
	 * Write the given properties object to the specified file.
	 */
	private static void writeProperties(Properties properties, File file) {
		FileOutputStream output = null;
		try {
			output = new FileOutputStream(file);
			properties.store(output, null);
		} catch (IOException e) {
			// TODO
			e.printStackTrace();
		} finally {
			if (output != null) {
				try {
					output.flush();
				} catch (IOException e) {
					// ignore	
				}
				try {
					output.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
	}

	private Properties getBasicProperties() throws IOException {
		Properties props = new Properties();
		InputStream input = null;

		String entry = Utils.replaceAll(this.getClass().getPackage().getName(), ".", "/") + "/" + FelixConstants.FELIX_BASIC_PROPERTIES_FILE;

		URL url = null;
		if (Activator.getContext() == null)
			url = this.getClass().getClassLoader().getResource(entry);
		else
			url = Activator.getContext().getBundle().getResource(entry);

		if (url != null) {
			try {
				input = url.openStream();
				props.load(input);
			} finally {
				if (input != null)
					try {
						input.close();
					} catch (IOException e) {
						// ignore
					}
			}
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
			Integer startLevel = Integer.valueOf(bInfos[i].getStartLevel());
			if (startLevel.intValue() == BundleInfo.NO_LEVEL)
				startLevel = new Integer(configData.getInitialBundleStartLevel());
			if (bInfos[i].isMarkedAsStarted()) {
				List list = (List) startList.get(startLevel);
				if (list == null) {
					list = new LinkedList();
					startList.put(startLevel, list);
				}
				list.add(bInfos[i]);
			} else {
				List list = (List) installList.get(startLevel);
				if (list == null) {
					list = new LinkedList();
					installList.put(startLevel, list);
				}
				list.add(bInfos[i]);
			}
		}
		//installList and startList are sorted by the key (StartLevel).

		setInstallingBundles(props, installList, FelixConstants.PROP_BUNDLE_INSTALL_PREFIX);
		setInstallingBundles(props, startList, FelixConstants.PROP_BUNDLE_START_PREFIX);

		props = Utils.appendProperties(props, configData.getProperties());

		//props.setProperty(EquinoxConstants.AOL, EquinoxConstants.AOL);
		return props;
	}

	/*
	 * Read and process the framework configuration files. For Felix this is the 
	 * config.properties file and the system.properties file.
	 */
	void readFwConfigFile(Manipulator manipulator, File configDir) throws IOException {
		ConfigData configData = manipulator.getConfigData();
		LauncherData launcherData = manipulator.getLauncherData();
		File configFile = new File(configDir, FelixConstants.DEFAULT_FW_CONFIGFILE);
		File systemPropsFile = new File(configDir, FelixConstants.DEFAULT_SYSTEMPROP_CONFIGFILE);

		// Load the properties from the config.properties file.
		if (configFile.exists()) {
			Properties configProperties = readProperties(configFile);
			
			for (Enumeration enumeration = configProperties.keys(); enumeration.hasMoreElements();) {
				String key = (String) enumeration.nextElement();
				String value = configProperties.getProperty(key);
				if (key.equals(FelixConstants.PROP_BUNDLES_STARTLEVEL))
					configData.setInitialBundleStartLevel(Integer.parseInt(value));
				else if (key.equals(FelixConstants.PROP_INITIAL_STARTLEVEL))
					configData.setBeginningFwStartLevel(Integer.parseInt(value));
				else if (key.startsWith(FelixConstants.PROP_BUNDLE_INSTALL_PREFIX) || key.startsWith(FelixConstants.PROP_BUNDLE_START_PREFIX)) {
					setInstallingBundles(configData, key, value);
				} else if (key.equals(FelixConstants.PROP_FELIX_FWPRIVATE_DIR))
					launcherData.setFwPersistentDataLocation(new File(value), false);
				else {
					configData.setProperty(key, value);
				}
			}
		}

		// Next load the system.properties file.
		if (systemPropsFile.exists()) {
			Properties systemProperties = readProperties(systemPropsFile);
			for (Enumeration e = systemProperties.keys(); e.hasMoreElements();) {
				String key = (String) e.nextElement();
				String value = systemProperties.getProperty(key);
				configData.setProperty(key, value);
			}
		}
	}

	void saveConfigs(BundleInfo[] bInfos, Manipulator manipulator, File configDir, boolean backup, boolean relative) throws IOException {
		Properties configProps = getConfigProps(bInfos, manipulator, relative);
		if (configProps == null || configProps.size() == 0) {
			return;
		}

		// for config file.
		Utils.createParentDir(configDir);
		File configFile = new File(configDir, FelixConstants.DEFAULT_FW_CONFIGFILE);
		if (backup)
			if (configFile.exists()) {
				File dest = Utils.getSimpleDataFormattedFile(configFile);
				if (!configFile.renameTo(dest))
					throw new IOException("Fail to rename from (" + configFile + ") to (" + dest + ")");
			}
		writeProperties(configProps, configFile);

		// for system props file.
		File systemPropsFile = new File(configDir, FelixConstants.DEFAULT_SYSTEMPROP_CONFIGFILE);
		if (backup)
			if (systemPropsFile.exists()) {
				File dest = Utils.getSimpleDataFormattedFile(systemPropsFile);
				if (!systemPropsFile.renameTo(dest))
					throw new IOException("Fail to rename from (" + configFile + ") to (" + dest + ")");
			}
		ConfigData configData = manipulator.getConfigData();
		writeProperties(configData.getProperties(), systemPropsFile);
	}

	private void setInstallingBundles(ConfigData configData, String key, String value) {
		int startLevel = BundleInfo.NO_LEVEL;
		boolean toBeStarted = false;
		try {
			if (key.startsWith(FelixConstants.PROP_BUNDLE_INSTALL_PREFIX)) {
				startLevel = Integer.parseInt(key.substring(FelixConstants.PROP_BUNDLE_INSTALL_PREFIX.length()));
			} else if (key.startsWith(FelixConstants.PROP_BUNDLE_START_PREFIX)) {
				startLevel = Integer.parseInt(key.substring(FelixConstants.PROP_BUNDLE_START_PREFIX.length()));
				toBeStarted = true;
			} else
				return;
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Invalid Format of ServiceLevel:key=" + key + " value=" + value, e);
		}

		if (value == null)
			throw new IllegalArgumentException("Invalid Format of ServiceLevel:key=" + key + " value=" + value);
		String[] locations = Utils.getTokens(value, " ");

		for (int i = 0; i < locations.length; i++) {
			try {
				configData.addBundle((new BundleInfo(URIUtil.fromString(locations[i]), startLevel, toBeStarted)));
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void setInstallingBundles(Properties props, SortedMap bundleList, String keyPrefix) {
		for (Iterator ite = bundleList.keySet().iterator(); ite.hasNext();) {
			Integer startLevel = (Integer) ite.next();
			List list = (List) bundleList.get(startLevel);
			String key = keyPrefix + startLevel.toString();
			StringBuffer buffer = new StringBuffer();
			for (Iterator ite2 = list.iterator(); ite2.hasNext();) {
				BundleInfo bInfo = (BundleInfo) ite2.next();
				buffer.append(bInfo.getLocation());
				if (ite2.hasNext())
					buffer.append(' ');
			}
			props.setProperty(key, buffer.toString());
		}
	}

}
