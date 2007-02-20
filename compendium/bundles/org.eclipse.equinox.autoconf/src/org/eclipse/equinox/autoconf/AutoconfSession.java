/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.equinox.autoconf;

import java.io.IOException;
import java.security.*;
import java.util.*;

import org.osgi.framework.*;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.deploymentadmin.DeploymentPackage;
import org.osgi.service.deploymentadmin.spi.DeploymentSession;
import org.osgi.service.deploymentadmin.spi.ResourceProcessorException;
import org.osgi.service.log.LogService;

public class AutoconfSession {
	private final DeploymentSession session;

	/* resourceName->AliaspidToConfigPack */
	private StoredConfigPacks curStoredConfigPacks = null;

	/*
	 * This table contains infos retrieved from resource processed in source Dp.
	 * Caution : the values to be actually set in Configuration objects will be
	 * not same as them, because of FactoryConfiguration or "merge" option.
	 * resourceName->AliaspidToConfigPack
	 */
	private ResourceToAliaspidToConfigPack newConfigPacks = new ResourceToAliaspidToConfigPack();

	/*
	 * List of ConfigPacks to be deleted in next commit(). This object will be
	 * set in dropped(), droppedAllResources(). In addition, process() might
	 * change this list.
	 */
	private List toBeDeletedConfigPacks = new Vector();

	/*
	 * List of ConfigPacks to be added in next commit(). In addition, process()
	 * changes this list.
	 */
	private List toBeAddedConfigPacks = new Vector();

	private final int operationTypes;

	public static final int INSTALL = 0;

	public static final int UPDATE = 1;

	public static final int UNINSTALL = 2;

	private boolean active = false;

	private ConfigurationAdmin cmAdmin = null;

	AutoconfSession(BundleContext context, DeploymentSession session) throws PrivilegedActionException {
		super();
		this.session = session;
		DeploymentPackage targetDp = session.getTargetDeploymentPackage();
		DeploymentPackage sourceDp = session.getSourceDeploymentPackage();

		if (sourceDp.getVersion().equals(Version.emptyVersion)) {
			if (!targetDp.getVersion().equals(Version.emptyVersion))
				this.operationTypes = UNINSTALL;
			else {
				Log.log(LogService.LOG_ERROR, "Both of Source & Target DP has emptyVersion");
				this.operationTypes = UNINSTALL;
			}
		} else if (targetDp.getVersion().equals(Version.emptyVersion))
			this.operationTypes = INSTALL;
		else
			this.operationTypes = UPDATE;
		active = true;

		/* open File and retrieve data of the DP. */
		final BundleContext bc = context;
		final DeploymentSession se = session;

		curStoredConfigPacks = (StoredConfigPacks) AccessController.doPrivileged(new PrivilegedExceptionAction() {
			public java.lang.Object run() throws Exception {
				return new StoredConfigPacks(bc, se);
			}
		});

	}

	public void deactivate() {
		active = false;
	}

	public DeploymentSession getSession() {
		return session;
	}

	public void setAliaspidToConfigPack(String resource, AliaspidToConfigPack configPacks) {
		this.newConfigPacks.put(resource, configPacks);
	}

	/**
	 * @param resource
	 * @throws ResourceProcessorException
	 */
	public void setToBeDeletedAndToBeAdded(String resource) throws ResourceProcessorException {
		AliaspidToConfigPack curAliaspidToConfigPack = curStoredConfigPacks.getByResourceName(resource);
		AliaspidToConfigPack newAliaspidToConfigPack = newConfigPacks.getByResourceName(resource);
		/*
		 * Configuration objects, which was included in a resource of
		 * TargetPackage (old) but not in the same resource of SourcePackage,
		 * should be deleted in commit().
		 */
		if (curAliaspidToConfigPack != null)
			for (Enumeration enumeration = curAliaspidToConfigPack.elements(); enumeration.hasMoreElements();) {
				StoredConfigPack curStoredConfigPack = (StoredConfigPack) enumeration.nextElement();
				String curAliasPid = curStoredConfigPack.getAliasPid();
				StoredConfigPack newConfigPack = (StoredConfigPack) newAliaspidToConfigPack.getByAliasPid(curAliasPid);
				if (newConfigPack == null) {
					String curActualPid = curStoredConfigPack.getActualPid();
					String bundleLocation = curStoredConfigPack.getBundleLocation();
					Configuration configuration = null;
					try {
						configuration = cmAdmin.getConfiguration(curActualPid, bundleLocation);
					} catch (Exception e) {
						// Log.log(LogService.LOG_DEBUG, null, e);
						String msg = "Fail to cmAdmin.getConfiguration.";
						Utils.errorHandler(curStoredConfigPack.isOptional(), msg, e);
						continue;
					}
					ConfigPack configPack = new ConfigPack(resource, curStoredConfigPack);
					configPack.setConfiguration(configuration);
					toBeDeletedConfigPacks.add(configPack);
				}
			}
		/*
		 * To list up Configuration objects, which whould be added or updated in
		 * commit().
		 */

		if (newAliaspidToConfigPack != null)
			for (Enumeration enumeration = newAliaspidToConfigPack.elements(); enumeration.hasMoreElements();) {
				ConfigPack newConfigPack = (ConfigPack) enumeration.nextElement();
				Configuration configuration = null;
				String fpid = newConfigPack.getFpid();
				String newAliasPid = newConfigPack.getAliasPid();
				String bundleLocation = newConfigPack.getBundleLocation();
				if (fpid != null) {// FactoryConfiguration
					StoredConfigPack curConfigPack = null;
					if (curAliaspidToConfigPack != null)
						curConfigPack = curAliaspidToConfigPack.getByAliasPid(newAliasPid);
					if (curConfigPack == null) {
						// a first-time installation of the factory
						// configuration.
						try {
							configuration = cmAdmin.createFactoryConfiguration(fpid, bundleLocation);
							newConfigPack.setConfiguration(configuration);
							newConfigPack.setCreated(true);
						} catch (Exception e) {
							String msg = "Fail to cmAdmin.createConfiguration.";
							Utils.errorHandler(curConfigPack.isOptional(), msg, e);
							continue;
						}
					} else {
						// The factory configuration already exists from a
						// previous Autoconf resource installation.
						String curActualPid = curConfigPack.getActualPid();
						try {
							configuration = cmAdmin.getConfiguration(curActualPid, bundleLocation);
							newConfigPack.setConfiguration(configuration);
							newConfigPack.setCreated(false);
						} catch (Exception e) {
							String msg = "Fail to cmAdmin.getConfiguration.";
							Utils.errorHandler(curConfigPack.isOptional(), msg, e);
							continue;
						}
					}
				} else { // not FactoryConfiguration
					try {
						configuration = cmAdmin.getConfiguration(newAliasPid, bundleLocation);
						newConfigPack.setConfiguration(configuration);
						String filterSt = "(&(" + Constants.SERVICE_PID + "=" + newAliasPid + ")(" + ConfigurationAdmin.SERVICE_BUNDLELOCATION + "=" + bundleLocation + "))";
						boolean newCreated = cmAdmin.listConfigurations(filterSt) == null ? true : false;
						newConfigPack.setCreated(newCreated);
					} catch (Exception e) {
						String msg = "Fail to cmAdmin.getConfiguration.";
						Utils.errorHandler(newConfigPack.isOptional(), msg, e);
						continue;
					}
				}
				toBeAddedConfigPacks.add(newConfigPack);
			}
	}

	public void setToBeDeleted(String resource) throws ResourceProcessorException {
		AliaspidToConfigPack aliaspidToConfigPack = curStoredConfigPacks.getByResourceName(resource);

		if (aliaspidToConfigPack == null) {
			Log.log(LogService.LOG_WARNING, "AutoconfSession#setToBeDeleted(" + resource + "): There is no such resource to be removed.");
			return;
		}
		for (Enumeration enumeration = aliaspidToConfigPack.keys(); enumeration.hasMoreElements();) {
			StoredConfigPack storedConfigPack = aliaspidToConfigPack.getByAliasPid((String) enumeration.nextElement());
			String curActualPid = storedConfigPack.getActualPid();
			String bundleLocation = storedConfigPack.getBundleLocation();
			Configuration configuration = null;
			try {
				configuration = cmAdmin.getConfiguration(curActualPid, bundleLocation);
			} catch (Exception e) {
				String msg = "Fail to getConfiguration.";
				Utils.errorHandler(storedConfigPack.isOptional(), msg, e);
				continue;
			}
			ConfigPack configPack = new ConfigPack(resource, storedConfigPack);
			configPack.setConfiguration(configuration);
			toBeDeletedConfigPacks.add(configPack);
		}
	}

	public void deleteConfigObjects() {
		// First, delete Configuration objects to be removed.
		for (Iterator it = toBeDeletedConfigPacks.listIterator(); it.hasNext();) {
			ConfigPack configPack = (ConfigPack) it.next();
			String resource = configPack.getResource();
			Configuration conf = configPack.getConfiguration();
			try {
				conf.delete();
			} catch (IOException e) {
				/*
				 * XXX: conf.delete() might be failed. But rp.commit() cannot
				 * throw RPException. It means, AutoConfiguration rp might act
				 * depending on its optional flag.
				 * 
				 * Therefore, conf.delete() might be called in
				 * process(),dropAllresources() or prepared(). In that case,
				 * rollback will be more complicated.
				 */
				Log.log(LogService.LOG_ERROR, "Fail to delete Configuration", e);
			}
			// remove elements regarding deleted objects
			AliaspidToConfigPack curAliaspidToConfigPack = curStoredConfigPacks.getByResourceName(resource);
			if (curAliaspidToConfigPack != null) {
				curAliaspidToConfigPack.remove(configPack.getAliasPid());
			}
		}
		toBeDeletedConfigPacks.clear();
	}

	/**
	 * This method is called only from commit(). Therefore, all Exceptions must
	 * be not thrown but logged, and this method should keep on going as
	 * possible.
	 * 
	 * @param dpToResourceAliaspidConfigPacks
	 * @throws IOException
	 */
	public void createConfigObjects() {
		for (int i = 0; i < toBeAddedConfigPacks.size(); i++) {
			ConfigPack configPack = (ConfigPack) toBeAddedConfigPacks.get(i);
			Configuration conf = configPack.getConfiguration();
			try {
				Dictionary aliasProps = configPack.getProps();
				if (configPack.isMerge()) {
					Dictionary currentProps = conf.getProperties();

					/*
					 * add properties that do not exist in the current
					 * configuration object's properties.
					 */
					for (Enumeration enumeration = aliasProps.keys(); enumeration.hasMoreElements();) {
						String key = (String) enumeration.nextElement();
						if (currentProps.get(key) == null) {
							currentProps.put(key, aliasProps.get(key));
						}
					}
					conf.update(currentProps);
					configPack.setProps(currentProps);
				} else {
					/* replace all properties of configuration object. */
					conf.update(aliasProps);
				}
				AliaspidToConfigPack curAliasToConfigPack = curStoredConfigPacks.getByResourceName(configPack.getResource());
				if (curAliasToConfigPack == null) {
					curAliasToConfigPack = new AliaspidToConfigPack(4);
					curStoredConfigPacks.put(configPack.getResource(), curAliasToConfigPack);
				}
				curAliasToConfigPack.put(configPack.getAliasPid(), configPack);
			} catch (IOException e) {
				/*
				 * XXX: conf.delete() might be failed. But rp.commit() cannot
				 * throw RPException. It means, AutoConfiguration rp might act
				 * depending on its optional flag.
				 * 
				 * Therefore, conf.delete() might be called in
				 * process(),dropAllresources() or prepared(). In that case,
				 * rollback will be more complicated.
				 */
				Log.log(LogService.LOG_ERROR, "Fail to update Configuration", e);
			}
		}
		toBeAddedConfigPacks.clear();
	}

	public void close(boolean rollback) throws ResourceProcessorException {
		if (rollback)
			for (Iterator it = toBeAddedConfigPacks.iterator(); it.hasNext();) {
				ConfigPack configPack = (ConfigPack) it.next();
				if (configPack.isCreated())
					try {
						configPack.getConfiguration().delete();
					} catch (IOException e) {
						Utils.errorHandler(configPack.isOptional(), "Fail to Configuration.delete()", e);
						continue;
					}
			}

		newConfigPacks.clear();
		toBeAddedConfigPacks.clear();
		toBeDeletedConfigPacks.clear();
		active = false;
	}

	public DeploymentPackage getTargetDeploymentPackage() {
		return this.session.getTargetDeploymentPackage();
	}

	public DeploymentPackage getSourceDeploymentPackage() {
		return this.session.getSourceDeploymentPackage();
	}

	public void setCmAdmin(ConfigurationAdmin cmAdmin) {
		this.cmAdmin = cmAdmin;
	}

	public int getOperationTypes() {
		return operationTypes;
	}

	public boolean isActive() {
		return active;
	}

	public StoredConfigPacks getCurResourceToAliaspidConfigPack() {
		return curStoredConfigPacks;
	}

	public void flushStoredConfigPacks() throws IOException {
		try {
			AccessController.doPrivileged(new PrivilegedExceptionAction() {
				public java.lang.Object run() throws Exception {
					curStoredConfigPacks.flush(session);
					return null;
				}
			});
		} catch (PrivilegedActionException pae) {
			active = false;
			Exception e = pae.getException();
			if (e instanceof IOException)
				throw (IOException) e;
			if (e instanceof RuntimeException)
				throw (RuntimeException) e;
			Log.log(LogService.LOG_WARNING, "Unexpected wrapped exception; not either IOException or RuntimeException", pae);
		}
	}

}