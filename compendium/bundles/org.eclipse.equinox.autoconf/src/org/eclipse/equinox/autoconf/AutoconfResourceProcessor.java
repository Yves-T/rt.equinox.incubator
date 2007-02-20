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
import java.io.InputStream;
import java.security.PrivilegedActionException;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.equinox.autoconf.parser.AutoconfDataParser;
import org.eclipse.equinox.autoconf.parser.AutoconfMetaTypeMsg;
import org.eclipse.equinox.autoconf.parser.AutoconfDataParser.AttributeHandler;
import org.eclipse.equinox.autoconf.parser.AutoconfDataParser.DesignateHandler;
import org.eclipse.equinox.dp.DpConstants;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.*;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.deploymentadmin.DeploymentPackage;
import org.osgi.service.deploymentadmin.spi.*;
import org.osgi.service.log.LogService;
import org.osgi.service.metatype.*;
import org.osgi.util.tracker.ServiceTracker;
import org.xml.sax.SAXException;

public class AutoconfResourceProcessor implements ResourceProcessor {
	private static final boolean DEBUG = false;

	private static final String ERROR_MSG_NOACTIVESESSION = "ERROR:There is no active session, because it's closed or begin() failed to retrieve PersistentData.";

	private ServiceTracker parserTracker;

	private AutoconfSession activeSession = null;

	private BundleContext context = null;

	private ServiceTracker cmTracker = null;

	private ConfigurationAdmin cmAdmin = null;

	private ServiceTracker metaTypeTracker = null;

	AutoconfResourceProcessor(BundleContext context) {
		super();
		this.context = context;

		cmTracker = new ServiceTracker(context, ConfigurationAdmin.class.getName(), null);

		parserTracker = new ServiceTracker(context, DpConstants.SAXFACTORYNAME, null);
		metaTypeTracker = new ServiceTracker(context, MetaTypeService.class.getName(), null);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.service.deploymentadmin.spi.ResourceProcessor#begin(org.osgi.service.deploymentadmin.spi.DeploymentSession)
	 */
	public void begin(DeploymentSession session) {
		try {
			Log.log(LogService.LOG_INFO, this, "begin(session)", "BEGIN");
			if (activeSession != null)
				/*
				 * According to the Spec, it never
				 * happens.
				 */
				Log.log(LogService.LOG_ERROR, "ERROR:Multiple sessions  have started");
			cmTracker.open();
			parserTracker.open();
			this.activeSession = new AutoconfSession(context, session);

		} catch (PrivilegedActionException e) {
			close();
			Log.log(LogService.LOG_ERROR, this, "begin(session) ", "UNKNOWN_ERROR in AutoconfSession()", e);
		}
		Utils.printoutConditionalPermissions(context);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.service.deploymentadmin.spi.ResourceProcessor#cancel()
	 */
	public void cancel() {
		try {
			Log.log(LogService.LOG_INFO, this, "cancel", " BEGIN");
			if (activeSession == null || !activeSession.isActive()) {
				Log.log(LogService.LOG_ERROR, ERROR_MSG_NOACTIVESESSION);
				return;
			}
			close();
		} catch (RuntimeException ex) {
			ex.printStackTrace();
			throw ex;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.service.deploymentadmin.spi.ResourceProcessor#commit()
	 */
	public void commit() {
		try {
			Log.log(LogService.LOG_INFO, this, "commit()", " BEGIN");

			if (activeSession == null || !activeSession.isActive()) {
				Log.log(LogService.LOG_ERROR, ERROR_MSG_NOACTIVESESSION);
				return;
			}
			printoutCmBundle();

			activeSession.deleteConfigObjects();
			activeSession.createConfigObjects();
			try {
				activeSession.flushStoredConfigPacks();
			} catch (IOException e) {
				Log.log(LogService.LOG_ERROR, "AutoconfResourceProcessor#commit(): Fail to flush ConfigPacks.", e);
			}
			close();
			Log.log(LogService.LOG_INFO, this, "commit()", " END");
		} catch (RuntimeException ex) {
			ex.printStackTrace();
			throw ex;
		}
	}

	private void printoutCmBundle() {
		Bundle[] bundles = context.getBundles();
		if (bundles != null)
			for (int i = 0; i < bundles.length; i++)
				if (Utils.getBundleLocationDoPrivileged(bundles[i]).indexOf("impl.service.cm.jar") != -1) {
					System.out.println(bundles[i] + ":" + bundles[i].getState());
					ServiceReference[] refs = bundles[i].getRegisteredServices();
					for (int j = 0; j < refs.length; j++) {
						System.out.println("refs[" + j + "]=" + refs[j]);
					}
				}
	}

	private void close() {
		try {
			this.close(false);
		} catch (Exception e) {
			Log.log(LogService.LOG_ERROR, "UNEXPECTED EXCEPTION", e);
		}
	}

	private void close(boolean rollback) throws ResourceProcessorException {
		Log.log(LogService.LOG_INFO, this, "close()", " BEGIN");
		if (activeSession != null)
			activeSession.close(rollback);
		activeSession = null;
	}

	public static void printoutServices(BundleContext context, String clazz, boolean all) {
		ServiceReference[] refs = null;
		try {
			if (all) {
				System.out.println("\ngetAllServiceReference(" + clazz + ")");
				refs = context.getAllServiceReferences(clazz, null);

			} else {
				System.out.println("\ngetServiceReference(" + clazz + ")");
				refs = context.getAllServiceReferences(clazz, null);
			}
			if (refs != null)
				for (int i = 0; i < refs.length; i++)
					System.out.println(refs[i]);
			else
				System.out.println("refs==null");
		} catch (InvalidSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.service.deploymentadmin.spi.ResourceProcessor#dropAllResources()
	 */
	public void dropAllResources() throws ResourceProcessorException {

		Log.log(LogService.LOG_INFO, this, "dropAllResources()", " BEGIN");
		if (activeSession == null || !activeSession.isActive()) {
			Log.log(LogService.LOG_ERROR, ERROR_MSG_NOACTIVESESSION);
			return;
		}
		setConfigurationAdmin();
		for (Enumeration keys = activeSession.getCurResourceToAliaspidConfigPack().keys(); keys.hasMoreElements();) {
			String resourceName = (String) keys.nextElement();
			this.dropped(resourceName);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.service.deploymentadmin.spi.ResourceProcessor#dropped(java.lang.String)
	 */
	public void dropped(String resource) throws ResourceProcessorException {
		Log.log(LogService.LOG_INFO, this, "dropped(" + resource + ")", "BEGIN");
		if (activeSession == null || !activeSession.isActive())
			Utils.errorHandler(false, ERROR_MSG_NOACTIVESESSION);
		activeSession.setToBeDeleted(resource);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.service.deploymentadmin.spi.ResourceProcessor#prepare()
	 */
	public void prepare() throws ResourceProcessorException {
		try {
			Log.log(LogService.LOG_INFO, this, "prepare()", " BEGIN");
			if (activeSession == null || !activeSession.isActive())
				Utils.errorHandler(false, ERROR_MSG_NOACTIVESESSION);

			// TODO Should we check no duplicate Configuration objects to set ?
		} catch (RuntimeException ex) {
			ex.printStackTrace();
			throw ex;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.service.deploymentadmin.spi.ResourceProcessor#process(java.lang.String,
	 *      java.io.InputStream)
	 */
	public void process(String name, InputStream stream) throws ResourceProcessorException {
		try {
			Log.log(LogService.LOG_INFO, this, "process(" + name + ")", " BEGIN");

			if (activeSession == null || !activeSession.isActive())
				Utils.errorHandler(false, ERROR_MSG_NOACTIVESESSION);

			SAXParserFactory parserFactory = (SAXParserFactory) parserTracker.getService();
			if (parserFactory == null) {
				String msg = "SAXParserFactory cannot be found.";
				if (DEBUG) {
					Log.log(LogService.LOG_ERROR, "parserFactory == null");
					printoutServices(context, DpConstants.SAXFACTORYNAME, false);
					System.out.println("\n&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&\n");
					printoutServices(context, DpConstants.SAXFACTORYNAME, true);
				}
				throw new ResourceProcessorException(ResourceProcessorException.CODE_OTHER_ERROR, msg);
				// } else {
				// Log.log(LogService.LOG_DEBUG, "parserFactory != null");
			}
			parserFactory.setValidating(true);
			parserFactory.setNamespaceAware(true);

			DeploymentPackage sourceDp = activeSession.getSourceDeploymentPackage();

			AutoconfDataParser parser = new AutoconfDataParser(context, sourceDp, stream, parserFactory, metaTypeTracker);
			Vector designateHandlers = null;
			try {
				designateHandlers = parser.doParse();
			} catch (ParserConfigurationException pce) {
				throw new ResourceProcessorException(ResourceProcessorException.CODE_OTHER_ERROR, "Fail to parse", pce);
			} catch (SAXException saxe) {
				throw new ResourceProcessorException(ResourceProcessorException.CODE_OTHER_ERROR, "Fail to parse", saxe);
			} catch (IOException ioe) {
				throw new ResourceProcessorException(ResourceProcessorException.CODE_OTHER_ERROR, "Fail to parse", ioe);

			}
			setConfigurationAdmin();

			createAliaspidToConfigPack(name, designateHandlers);
			activeSession.setToBeDeletedAndToBeAdded(name);
			Log.log(LogService.LOG_DEBUG, this, "process(" + name + ")", " END");
		} catch (ResourceProcessorException ex) {
			ex.printStackTrace();
			throw ex;
		} catch (RuntimeException ex) {
			ex.printStackTrace();
			throw ex;
		}
	}

	private void setConfigurationAdmin() throws ResourceProcessorException {
		cmAdmin = (ConfigurationAdmin) cmTracker.getService();
		if (cmAdmin == null) {
			if (DEBUG) {
				Log.log(LogService.LOG_ERROR, this, "setConfigurationAdmin()", "There is no ConfigurationAdmin service.");
				printoutServices(context, ConfigurationAdmin.class.getName(), false);
				System.out.println("\n&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&\n");
				printoutServices(context, ConfigurationAdmin.class.getName(), true);
			}
			throw new ResourceProcessorException(ResourceProcessorException.CODE_OTHER_ERROR, "Fail to get ConfigurationAdmin service");
		}
		activeSession.setCmAdmin(cmAdmin);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.service.deploymentadmin.spi.ResourceProcessor#rollback()
	 */
	public void rollback() {

		Log.log(LogService.LOG_INFO, this, "rolleback()", " BEGIN");
		if (activeSession == null || !activeSession.isActive()) {
			Log.log(LogService.LOG_ERROR, "ERROR:There is no active session");
			return;
		}

		try {
			// delete newly created objects in process, if exists.
			close(true);
		} catch (ResourceProcessorException e) {
			Log.log(LogService.LOG_ERROR, this, "rollback", "Fail to close(true)", e);
		}
	}

	private void createAliaspidToConfigPack(String resource, Vector designateHandlers) throws ResourceProcessorException {
		/*
		 * Autoconf Resource Processor must process each Designate elament in
		 * ofrder of appearnce.
		 */
		AliaspidToConfigPack aliaspidToConfigPacks = new AliaspidToConfigPack(designateHandlers.size());
		for (Enumeration enumeration = designateHandlers.elements(); enumeration.hasMoreElements();) {
			DesignateHandler designateHandler = (DesignateHandler) enumeration.nextElement();
			String pid = designateHandler.getPid();
			String fpid = designateHandler.getFpid();
			String bundleLocation = designateHandler.getBundleLocation();
			boolean merge = designateHandler.isMerge();
			boolean optional = designateHandler.isOptional();
			if (DEBUG) {
				System.out.println("pid=" + pid);
				System.out.println("fpid=" + fpid);
				System.out.println("bundleLocation=" + bundleLocation);
			}

			Vector attributeHandlers = designateHandler.getAttributeHandlers();
			Dictionary props = new Hashtable(Utils.createHashtable(attributeHandlers.size()));
			setAttributes(designateHandler, optional, attributeHandlers, props);
			props.put(ConfigurationAdmin.SERVICE_BUNDLELOCATION, bundleLocation);
			// props.put(Constants.SERVICE_PID, pid);
			String bundleSymbolicName = bundleLocation.substring(DpConstants.DP_LOCATION_PREFIX.length());
			DeploymentPackage sourceDp = activeSession.getSourceDeploymentPackage();
			boolean isIncludedInDp = Utils.getBundleFromDpDoPrivileged(sourceDp, bundleSymbolicName) != null ? true : false;
			if (fpid != null) {
				props.put(ConfigurationAdmin.SERVICE_FACTORYPID, fpid);
			} else {
				if (!isIncludedInDp)
					Utils.errorHandler(optional, "Although bundleLocation specified in this Object is NOT included in the same DP," + "FactoryConfiguration is not specified. That is not what OSGi Spec v1.0 allows !!");
			}
			StoredConfigPack configPack = new ConfigPack(resource, pid, props, merge, optional);
			aliaspidToConfigPacks.put(configPack.getAliasPid(), configPack);
		}
		if (DEBUG)
			aliaspidToConfigPacks.printoutConfigPacks();
		activeSession.setAliaspidToConfigPack(resource, aliaspidToConfigPacks);
	}

	/**
	 * @param designateHandler
	 * @param optional
	 * @param attributeHandlers
	 * @param props
	 * @throws ResourceProcessorException
	 */
	private void setAttributes(DesignateHandler designateHandler, boolean optional, Vector attributeHandlers, Dictionary props) throws ResourceProcessorException {
		for (int i = 0; i < attributeHandlers.size(); i++) {
			AttributeHandler attributeHandler = (AttributeHandler) attributeHandlers.get(i);
			String attrAdref = attributeHandler.getAdref();
			String attrKey = attributeHandler.getKey();
			String attrVal = attributeHandler.getContent();
			Vector attrVec = attributeHandler.getValues();
			ObjectClassDefinition ocd = designateHandler.getOcd();
			if (ocd == null) {
				Utils.errorHandler(optional, NLS.bind(AutoconfMetaTypeMsg.INVALID_SYNTAX, "While content is set, cardinality is not 0"));

			}
			AttributeDefinition[] ads = ocd.getAttributeDefinitions(ObjectClassDefinition.ALL);

			// To check cardinality.
			int cardinality = 0;
			int type = -1;
			for (int j = 0; j < ads.length; j++)
				if (ads[j].getID().equals(attrAdref)) {
					cardinality = ads[j].getCardinality();
					type = ads[j].getType();
					break;
				}
			/* Check */
			if (type == -1)
				Utils.errorHandler(optional, NLS.bind(AutoconfMetaTypeMsg.INVALID_SYNTAX, "There is no AttributeDefinitions whose ID equals pid"));

			if (DEBUG) {
				System.out.println("attrAdref=" + attrAdref);
				System.out.println("\tocd.getName()=" + ocd.getName());
				System.out.println("\tocd.getID()=" + ocd.getID());
				System.out.println("\tocd.getDescription()=" + ocd.getDescription());
				System.out.println("\tattrKey=" + attrKey);
				for (int j = 0; j < ads.length; j++) {
					System.out.println("\t\tads[" + j + "].getID()=" + ads[j].getID());
					System.out.println("\t\tads[" + j + "].getName()=" + ads[j].getName());

					System.out.println("\t\tads[" + j + "].getType()=" + getTypeSt(ads[j].getType()));
					System.out.println("\t\tads[" + j + "].getDescription()=" + ads[j].getDescription());
					System.out.println("\t\tads[" + j + "].getCardinality()=" + ads[j].getCardinality());
				}
			}

			if (attrVec == null) {
				if (cardinality != 0)
					Utils.errorHandler(optional, NLS.bind(AutoconfMetaTypeMsg.INVALID_SYNTAX, "While content is set, cardinality is not 0"));
				props.put(attrKey, getValueObj(type, attrVal));
			} else {
				if (cardinality == 0)
					Utils.errorHandler(optional, NLS.bind(AutoconfMetaTypeMsg.INVALID_SYNTAX, "While content is not set, cardinality is 0."));
				setVectorOrArrayTpProps(props, attrKey, attrVec, cardinality, type);
			}
		}
	}

	/**
	 * @param aliasConfig
	 * @param attrKey
	 * @param attrVec
	 * @param cardinality
	 * @param type
	 */
	private void setVectorOrArrayTpProps(Dictionary aliasConfig, String attrKey, Vector attrVec, int cardinality, int type) {
		if (cardinality < 0) { // Vector
			Vector newVector = new Vector(attrVec.size());
			for (int k = 0; k < attrVec.size(); k++)
				newVector.add(getValueObj(type, (String) attrVec.get(k)));
			aliasConfig.put(attrKey, newVector);
		} else { // Array
			Object[] newArray = getEmptyArray(type, attrVec.size());
			for (int k = 0; k < attrVec.size(); k++)
				newArray[k] = getValueObj(type, (String) attrVec.get(k));
			aliasConfig.put(attrKey, newArray);
			if (DEBUG) {
				System.err.println("newArray.length=" + newArray.length);
				for (int k = 0; k < attrVec.size(); k++) {
					System.err.println("k=" + k);
					Object obj = getValueObj(type, (String) attrVec.get(k));
					System.err.println("obj=" + obj + ":" + obj.getClass().getName());
					System.err.println("newArray[]:" + newArray.toString());
				}
			}
		}
	}

	private static String getTypeSt(int type) {
		switch (type) {
			case AttributeDefinition.BOOLEAN :
				return Boolean.class.getName();
			case AttributeDefinition.BYTE :
				return Byte.class.getName();
			case AttributeDefinition.CHARACTER :
				return Character.class.getName();
			case AttributeDefinition.DOUBLE :
				return Double.class.getName();
			case AttributeDefinition.FLOAT :
				return Float.class.getName();
			case AttributeDefinition.INTEGER :
				return Integer.class.getName();
			case AttributeDefinition.LONG :
				return Long.class.getName();
			case AttributeDefinition.SHORT :
				return Short.class.getName();
			case AttributeDefinition.STRING :
				return String.class.getName();
			default :
				// TODO error
				return null;
		}
	}

	private static Object[] getEmptyArray(int type, int size) {
		// System.err.println("type=" + type + ", size=" + size);
		Object[] array = null;
		switch (type) {
			case AttributeDefinition.BOOLEAN :
				array = new Boolean[size];
				break;
			case AttributeDefinition.BYTE :
				array = new Byte[size];
				break;
			case AttributeDefinition.CHARACTER :
				array = new Character[size];
				break;
			case AttributeDefinition.DOUBLE :
				array = new Double[size];
				break;
			case AttributeDefinition.FLOAT :
				array = new Float[size];
				break;
			case AttributeDefinition.INTEGER :
				array = new Integer[size];
				break;
			case AttributeDefinition.LONG :
				array = new Long[size];
				break;
			case AttributeDefinition.SHORT :
				array = new Short[size];
				break;
			case AttributeDefinition.STRING :
				array = new String[size];
				break;
			default :
		}
		return array;
	}

	private static Object getValueObj(int type, String valueSt) {
		// System.err.println("type=" + type + ", valueSt=" + valueSt);
		switch (type) {
			case AttributeDefinition.BOOLEAN :
				return Boolean.valueOf(valueSt);
			case AttributeDefinition.BYTE :
				return Byte.valueOf(valueSt);
			case AttributeDefinition.CHARACTER :
				return new Character(valueSt.charAt(0));
			case AttributeDefinition.DOUBLE :
				return Double.valueOf(valueSt);
			case AttributeDefinition.FLOAT :
				return Float.valueOf(valueSt);
			case AttributeDefinition.INTEGER :
				return Integer.valueOf(valueSt);
			case AttributeDefinition.LONG :
				return Long.valueOf(valueSt);
			case AttributeDefinition.SHORT :
				return new Short(Short.parseShort(valueSt));
				// return .valueOf(valueSt);
				// return Short.valueOf(valueSt);
			case AttributeDefinition.STRING :
				return valueSt;
			default :
				return null;
		}
	}

}
