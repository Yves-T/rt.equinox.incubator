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
package org.eclipse.equinox.frameworkadmin.knopflerfish.internal;

import java.io.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

import org.eclipse.equinox.frameworkadmin.BundleInfo;
import org.eclipse.equinox.frameworkadmin.knopflerfish.KfConfigData;
import org.eclipse.equinox.internal.frameworkadmin.utils.Utils;
import org.osgi.service.log.LogService;

public class KfFwConfigFileParser {
	private final static String HEADER = "# This file was written by ConfigSetter for Knopflerfish.";
	private final static String D = "-D";
	private final static String PROPFILE_PREFIX = "prop.";

	private void addInstallingBundlesList(BundleInfo[] bInfos, KfConfigData configData, List lines) {
		if (bInfos.length == 0)
			return;
		int initialBSL = configData.getInitialBundleStartLevel() == BundleInfo.NO_LEVEL ? 1 : configData.getInitialBundleStartLevel();

		SortedMap bslToList = new TreeMap();
		for (int i = 0; i < bInfos.length; i++) {
			Integer sL = Integer.valueOf(bInfos[i].getStartLevel());
			if (sL.intValue() == BundleInfo.NO_LEVEL)
				sL = new Integer(initialBSL);
			List list = (List) bslToList.get(sL);
			if (list == null) {
				list = new LinkedList();
				bslToList.put(sL, list);
			}
			list.add(bInfos[i]);
		}
		// bslToList is sorted by the key (StartLevel).
		for (Iterator ite = bslToList.keySet().iterator(); ite.hasNext();) {
			Integer sl = (Integer) ite.next();
			lines.add("-initlevel " + sl.toString());
			List list = (List) bslToList.get(sl);
			for (Iterator ite2 = list.iterator(); ite2.hasNext();) {
				BundleInfo bInfo = (BundleInfo) ite2.next();
				try {
					//					String cmd = Utils.getRelativePath(new URL(bInfo.getLocation()), new URL(cInfo.getBaseLocation()));
					//					//KF doesn't support "../".
					//					if (cmd.startsWith("../"))
					//						cmd = bInfo.getLocation();
					String cmd = bInfo.getLocation();

					if (bInfo.isMarkedAsStarted())
						lines.add("-istart " + cmd);
					else
						lines.add("-install " + cmd);
				} catch (IllegalArgumentException e) {
					// Never happen
					e.printStackTrace();
				}
			}
			lines.add("");
		}
		return;
	}

	private String[] getConfigFileLines(BundleInfo[] bInfos, KfConfigData configData, File outputFile) throws IOException {
		//		if (cInfo.getFwJar() == null)
		//			new FwLauncherException("fwHomeDirUrl == null", FwLauncherException.OTHERS);

		List lines = new LinkedList();
		lines.add(HEADER);
		SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd/HH:mm:ss");
		String date = df.format(new Date());
		lines.add("# " + date);
		//if (absolute)
		//		lines.add(D + KfConstants.PROP_ASSUMED_CWD + "=" + Utils.replaceAll(cInfo.getAssumedCwd().getAbsolutePath(), File.separator, "/"));

		handleProperties(configData, outputFile, lines);

		//	KfConfigHandler ch = (KfConfigHandler) cf;
		//-Dorg.knopflerfish.gosg.jars=file:jars/
		// value of org.knopflerfish.gosg.jars must endwith "/".

		//		if (relative) {
		//			String baseUrlSt = cInfo.getBaseLocation();
		//			baseUrlSt = baseUrlSt.endsWith("/") ? baseUrlSt : baseUrlSt + "/";
		//			URL baseUrl = new URL(baseUrlSt);
		//			String assumedCwd = ((KfConfiguratorFwConfigInfo) cInfo).getAssumedCwd().getAbsolutePath();
		//			URL assumedCwdUrl = Utils.getUrl("file", null, assumedCwd);
		//			if (baseUrl.getProtocol().endsWith("file")) {
		//				lines.add(D + "org.knopflerfish.gosg.jars=" + "file:" + Utils.getRelativePath(baseUrl, assumedCwdUrl) + "/");
		//				lines.add(D + KfConfiguratorFwConfigHandler.PROP_ASSUMED_CWD + "=" + assumedCwd + File.separator);
		//			} else
		//				lines.add(D + "org.knopflerfish.gosg.jars=" + baseUrl.toExternalForm());
		//		} else {
		//			URL baseUrl = new URL(cInfo.getBaseLocation());
		//			lines.add(D + "org.knopflerfish.gosg.jars=" + baseUrl.toExternalForm());
		//		}

		//		if (cInfo.isInitial())
		//			lines.add("-init");

		addInstallingBundlesList(bInfos, configData, lines);

		if (configData.getInitialBundleStartLevel() != BundleInfo.NO_LEVEL)
			lines.add("-initlevel " + configData.getInitialBundleStartLevel());
		if (configData.getBeginingFwStartLevel() != BundleInfo.NO_LEVEL)
			lines.add("-startlevel " + configData.getBeginingFwStartLevel());
		String[] ret = new String[lines.size()];
		lines.toArray(ret);
		return ret;
	}

	private void handleProperties(KfConfigData configData, File outputFile, List lines) throws IOException {

		//	File assumedCwd = fwConfigHandler.fwConfigInfo.assumedCwd;
		//if (assumedCwd == null)
		//	throw new FwLauncherException("assumedCwd must be set in advance", FwLauncherException.UNSATISFIED_PREREQUISTE);
		Properties fwIndependentProps = configData.getFwIndependentProps();
		Properties fwDependentProps = configData.getFwDependentProps();
		if (fwDependentProps.size() == 0 && fwIndependentProps.size() == 0)
			return;

		if (configData.getXargsFile() == null)
			// init2.xargs --> PROPFILE_PREFIX +init2.xargs
			configData.setXargsFile(new File(outputFile.getParentFile(), PROPFILE_PREFIX + outputFile.getName()));

		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(configData.getXargsFile()));
			writeCmdLineForProps(fwIndependentProps, bw);
			writeCmdLineForProps(fwDependentProps, bw);
			bw.flush();
			bw.close();
		} finally {
			if (bw != null)
				bw.close();
		}
		Log.log(LogService.LOG_INFO, "Prop Config are stored in file(" + configData.getXargsFile().getAbsolutePath() + ") successfully.");

		// Assume that -xargs will use cwd as base dir.
		try {
			URL targetUrl = Utils.getUrl("file", null, configData.getXargsFile().getAbsolutePath());
			//			URL url = Utils.getUrl("file", null, assumedCwd.getAbsolutePath());
			//			String relativePropXargs = Utils.getRelativePath(targetUrl, url);
			//			lines.add("-xargs " + relativePropXargs);
			lines.add("-xargs " + targetUrl.toExternalForm());
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}

	}

	private boolean isFwDependent(String key) {
		// TODO This algorithm is temporal. 
		if (key.startsWith(KfConstants.PROP_KF_DEPENDENT_PREFIX))
			return true;
		return false;
	}

	private boolean isIncludedInInstallingBundles(KfConfigData configData, String location) {
		BundleInfo[] bInfo = configData.getBundles();
		for (int i = 0; i < bInfo.length; i++) {
			if (bInfo[i].getLocation().equals(location))
				return true;
		}
		return false;
	}

	private void parseCmdLine(KfConfigData configData, String[] lines, File inputFile) throws IOException {
		Log.log(LogService.LOG_DEBUG, this, "parseCmdLine(FwConfigData configData,String[] lines, File inputFile)", "inputFile=" + inputFile.getAbsolutePath());

		int startlevel = KfConstants.DEFAULT_INITIAL_BUNDLE_SL;

		for (int i = 0; i < lines.length; i++) {
			final String line = lines[i].trim();
			StringTokenizer tokenizer = new StringTokenizer(line, " ");
			if (line.startsWith("#"))
				continue;
			if (line.length() == 0)
				continue;
			//			if (line.equals("-init")) {
			//				handler.setInitial(true);
			//			} else 
			if (line.startsWith("-xargs")) {
				if (tokenizer.countTokens() != 2) {
					Log.log(LogService.LOG_WARNING, "Illegal Format:line=" + line + "tokenizer.countTokens()=" + tokenizer.countTokens());
					//	throw new ManipulatorException("Illegal Format:line=" + line + "tokenizer.countTokens()=" + tokenizer.countTokens(), ManipulatorException.FILE_FORMAT_ERROR);
				}
				tokenizer.nextToken();
				String value = tokenizer.nextToken();
				URL url = Utils.formatUrl(value, Utils.getUrl("file", null, inputFile.getAbsolutePath()));
				File file = new File(url.getFile());
				configData.setXargsFile(file);
				this.readFwConfigFile(configData, file);
			} else if (line.startsWith("-initlevel")) {
				if (tokenizer.countTokens() != 2) {
					Log.log(LogService.LOG_WARNING, "Illegal Format:line=" + line);
				}
				tokenizer.nextToken();
				startlevel = Integer.parseInt(tokenizer.nextToken());
			} else if (line.startsWith("-startlevel")) {
				if (tokenizer.countTokens() != 2)
					Log.log(LogService.LOG_WARNING, "Illegal Format:line=" + line);
				tokenizer.nextToken();
				configData.setBeginningFwStartLevel(Integer.parseInt(tokenizer.nextToken()));
			} else if (line.startsWith("-install")) {
				if (tokenizer.countTokens() != 2)
					Log.log(LogService.LOG_WARNING, "Illegal Format:line=" + line);
				tokenizer.nextToken();
				String value = tokenizer.nextToken();
				configData.addBundle(new BundleInfo(value, startlevel, false));
			} else if (line.startsWith("-istart")) {
				if (tokenizer.countTokens() != 2)
					Log.log(LogService.LOG_WARNING, "Illegal Format:line=" + line);
				tokenizer.nextToken();
				String value = tokenizer.nextToken();
				configData.addBundle(new BundleInfo(value, startlevel, true));
			} else if (line.startsWith("-start")) {
				if (tokenizer.countTokens() != 2)
					Log.log(LogService.LOG_WARNING, "Illegal Format:line=" + line);
				tokenizer.nextToken();
				String value = tokenizer.nextToken();
				if (this.isIncludedInInstallingBundles(configData, value))
					configData.addBundle(new BundleInfo(value, startlevel, false));
				else
					Log.log(LogService.LOG_WARNING, "location(" + value + ") is NOT included in InstallingBundles.");
			} else if (line.startsWith("-D")) {
				String tmp = line.substring("-D".length());
				StringTokenizer tok = new StringTokenizer(tmp, "=");
				if (tok.countTokens() != 2)
					Log.log(LogService.LOG_WARNING, "Illegal Format:line=" + line);
				String key = tok.nextToken();
				String value = tok.nextToken();
				if (isFwDependent(key))
					configData.setFwDependentProp(key, value);
				else
					configData.setFwIndependentProp(key, value);
			} else
				Log.log(LogService.LOG_WARNING, "Unsupported Format:line=" + line);
		}

		configData.setInitialBundleStartLevel(startlevel);
	}

	void readFwConfigFile(KfConfigData configData, File inputFile) throws IOException {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(inputFile));
			String line;
			List list = new LinkedList();
			while ((line = br.readLine()) != null) {
				list.add(line);
			}
			String[] lines = new String[list.size()];
			list.toArray(lines);
			this.parseCmdLine(configData, lines, inputFile);
		} finally {
			if (br != null)
				br.close();
		}
	}

	void saveConfigs(BundleInfo[] bInfos, KfConfigData configData, File outputFile, boolean backup) throws IOException {
		if (backup)
			if (outputFile.exists()) {
				File dest = Utils.getSimpleDataFormattedFile(outputFile);
				if (!outputFile.renameTo(dest))
					throw new IOException("Fail to rename from (" + outputFile + ") to (" + dest + ")");
			}
		Utils.createParentDir(outputFile);

		//void saveConfigs(File outputFile, File assumedCwd) throws ConfigManipulatorException {
		//handler.setAssumedCwd(assumedCwd);
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(outputFile));
			String[] lines = this.getConfigFileLines(bInfos, configData, outputFile);
			for (int i = 0; i < lines.length; i++) {
				bw.write(lines[i]);
				bw.newLine();
			}
			bw.flush();
			Log.log(LogService.LOG_INFO, "Config are stored in file(" + outputFile.getAbsolutePath() + ") successfully.");
		} finally {
			if (bw != null)
				bw.close();
		}
	}

	private void writeCmdLineForProps(Properties props, BufferedWriter bw) throws IOException {
		for (Enumeration enum = props.keys(); enum.hasMoreElements();) {
			String key = (String) enum.nextElement();
			String value = props.getProperty(key);
			bw.write(D + key + "=" + value);
			bw.newLine();
		}
	}
}
