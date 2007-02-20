/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.equinox.autoconf.parser;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import javax.xml.parsers.*;

import org.eclipse.equinox.autoconf.Log;
import org.eclipse.equinox.autoconf.Utils;
import org.eclipse.equinox.dp.DpConstants;
import org.eclipse.equinox.metatype.AttributeDefinitionImpl;
import org.eclipse.equinox.metatype.ObjectClassDefinitionImpl;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.deploymentadmin.DeploymentPackage;
import org.osgi.service.deploymentadmin.spi.ResourceProcessorException;
import org.osgi.service.log.LogService;
import org.osgi.service.metatype.*;
import org.osgi.util.tracker.ServiceTracker;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Meta XML Data Parser
 * 
 * TODO Refine the implementation.
 * 
 * The classes in this package are implemented referring
 * org.eclipse.equinox.metatype plugin.  This needs library of 
 * equinox.metatype plugin jar which a patch is applied to version 1.0.0
 * (as of 2007.Feb.19) in lib/ directory. The patch is stored in doc/ directory.
 * This implementatin must be refined.
 *  
 */
public class AutoconfDataParser {
	private static final boolean DEBUG = false;

	protected static final String METADATA = "MetaData"; //$NON-NLS-1$

	protected static final String LOCALIZATION = "localization"; //$NON-NLS-1$

	protected static final String OCD = "OCD"; //$NON-NLS-1$

	protected static final String ICON = "Icon"; //$NON-NLS-1$

	protected static final String AD = "AD"; //$NON-NLS-1$

	protected static final String CARDINALITY = "cardinality"; //$NON-NLS-1$

	protected static final String OPTION = "Option"; //$NON-NLS-1$

	protected static final String LABEL = "label"; //$NON-NLS-1$

	protected static final String VALUE = "value"; //$NON-NLS-1$

	protected static final String MIN = "min"; //$NON-NLS-1$

	protected static final String MAX = "max"; //$NON-NLS-1$

	protected static final String TYPE = "type"; //$NON-NLS-1$

	protected static final String SIZE = "size"; //$NON-NLS-1$

	protected static final String ID = "id"; //$NON-NLS-1$

	protected static final String NAME = "name"; //$NON-NLS-1$

	protected static final String DESCRIPTION = "description"; //$NON-NLS-1$

	protected static final String RESOURCE = "resource"; //$NON-NLS-1$

	protected static final String PID = "pid"; //$NON-NLS-1$

	protected static final String DEFAULT = "default"; //$NON-NLS-1$

	protected static final String ADREF = "adref"; //$NON-NLS-1$

	protected static final String CONTENT = "content"; //$NON-NLS-1$

	protected static final String FACTORY = "factoryPid"; //$NON-NLS-1$

	protected static final String BUNDLE = "bundle"; //$NON-NLS-1$

	protected static final String OPTIONAL = "optional"; //$NON-NLS-1$

	protected static final String OBJECT = "Object"; //$NON-NLS-1$

	protected static final String OCDREF = "ocdref"; //$NON-NLS-1$

	protected static final String ATTRIBUTE = "Attribute"; //$NON-NLS-1$

	protected static final String DESIGNATE = "Designate"; //$NON-NLS-1$

	protected static final String MERGE = "merge"; //$NON-NLS-1$

	protected static final String REQUIRED = "required"; //$NON-NLS-1$

	protected static final String INTEGER = "Integer"; //$NON-NLS-1$

	protected static final String STRING = "String"; //$NON-NLS-1$

	protected static final String FLOAT = "Float"; //$NON-NLS-1$

	protected static final String DOUBLE = "Double"; //$NON-NLS-1$

	protected static final String BYTE = "Byte"; //$NON-NLS-1$

	protected static final String LONG = "Long"; //$NON-NLS-1$

	protected static final String CHAR = "Char"; //$NON-NLS-1$

	protected static final String BOOLEAN = "Boolean"; //$NON-NLS-1$

	protected static final String SHORT = "Short"; //$NON-NLS-1$

	// protected Bundle _dp_bundle;
	// protected URL _dp_url;
	protected SAXParserFactory parserFactory;

	private final ServiceTracker metaTypeTracker;

	protected XMLReader xmlReader;

	// DesignateHanders in DataParser class
	Vector designateHandlers = new Vector(7);

	// ObjectClassDefinitions in DataParser class w/ corresponding reference
	// keys
	// OCD_ID to OCDImpl
	Hashtable ocdIdToOCDimpl = new Hashtable(7);

	// // pid to ObjectClassDefinitions in DataParser class as a Hashtable
	// protected Hashtable _dp_pid_to_OCDs_ = new Hashtable(7);
	// Localization in DataParser class
	String localization;

	InputStream inputStream;

	BundleContext context = null;

	DeploymentPackage sourceDp = null;

	/*
	 * Constructor of class DataParser.
	 */
	public AutoconfDataParser(BundleContext context, DeploymentPackage sourceDp, InputStream inputStream, SAXParserFactory parserFactory, ServiceTracker metaTypeTracker) {
		this.context = context;
		this.sourceDp = sourceDp;
		this.inputStream = inputStream;
		this.parserFactory = parserFactory;
		this.metaTypeTracker = metaTypeTracker;
		parserFactory.setValidating(false);
	}

	/**
	 * Main method to parse specific MetaData file.
	 * 
	 * @return Vector of designatedHandlers, which includes OCDs retrieved from
	 *         MetaTypeService.
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 */
	public Vector doParse() throws ParserConfigurationException, SAXException, IOException {

		SAXParser saxParser = parserFactory.newSAXParser();
		xmlReader = saxParser.getXMLReader();
		xmlReader.setContentHandler(new RootHandler());
		xmlReader.setErrorHandler(new MyErrorHandler(System.err));
		// InputStream is = _dp_url.openStream();
		InputSource isource = new InputSource(inputStream);
		// Log.log(LogService.LOG_INFO, "Starting to parse " + _dp_url);
		// //$NON-NLS-1$
		xmlReader.parse(isource);

		return designateHandlers;
	}

	/*
	 * Convert String for expected data type.
	 */
	static Object convert(String value, int type) {

		if (value == null) {
			return null;
		}

		switch (type) {
			case AttributeDefinition.STRING :
				// Both the min and max of STRING are Integers.
				return new Integer(value);
			case AttributeDefinition.LONG :
				return new Long(value);
			case AttributeDefinition.INTEGER :
				return new Integer(value);
			case AttributeDefinition.SHORT :
				return new Short(value);
			case AttributeDefinition.CHARACTER :
				return new Character(value.charAt(0));
			case AttributeDefinition.BYTE :
				return new Byte(value);
			case AttributeDefinition.DOUBLE :
				return new Double(value);
			case AttributeDefinition.FLOAT :
				return new Float(value);
			case AttributeDefinition.BIGINTEGER :
				try {
					Class bigIntClazz = Class.forName("java.math.BigInteger");//$NON-NLS-1$
					Constructor bigIntConstructor = bigIntClazz.getConstructor(new Class[] {String.class});
					return bigIntConstructor.newInstance(new Object[] {value});
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
					return null;
				} catch (SecurityException e) {
					e.printStackTrace();
					return null;
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
					return null;
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
					return null;
				} catch (InstantiationException e) {
					e.printStackTrace();
					return null;
				} catch (IllegalAccessException e) {
					e.printStackTrace();
					return null;
				} catch (InvocationTargetException e) {
					e.printStackTrace();
					return null;
				}
			case AttributeDefinition.BIGDECIMAL :
				try {
					Class bigDecimalClazz = Class.forName("java.math.BigDecimal");//$NON-NLS-1$
					Constructor bigDecimalConstructor = bigDecimalClazz.getConstructor(new Class[] {String.class});
					return bigDecimalConstructor.newInstance(new Object[] {value});
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
					return null;
				} catch (SecurityException e) {
					e.printStackTrace();
					return null;
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
					return null;
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
					return null;
				} catch (InstantiationException e) {
					e.printStackTrace();
					return null;
				} catch (IllegalAccessException e) {
					e.printStackTrace();
					return null;
				} catch (InvocationTargetException e) {
					e.printStackTrace();
					return null;
				}
			case AttributeDefinition.BOOLEAN :
				return new Boolean(value);
			default :
				// Unknown data type
				return null;
		}
	}

	/**
	 * Abstract of all Handlers.
	 */
	protected class AbstractHandler extends DefaultHandler {

		protected ContentHandler docHandler;

		protected boolean parsedDataValid = true;

		public AbstractHandler(ContentHandler parentHandler) {

			this.docHandler = parentHandler;
			xmlReader.setContentHandler(this);
		}

		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

			throw new SAXException(NLS.bind(AutoconfMetaTypeMsg.UNEXPECTED_ELEMENT, qName));
		}

		public void characters(char[] buf, int start, int end) throws SAXException {

			String s = new String(buf, start, end).trim();
			if (s.length() > 0) {
				throw new SAXException(NLS.bind(AutoconfMetaTypeMsg.UNEXPECTED_TEXT, s));
			}
		}

		/**
		 * Called when this element and all elements nested into it have been
		 * handled.
		 */
		protected void finished() throws SAXException {
			// do nothing by default
		}

		public void endElement(String namespaceURI, String localName, String qName) throws SAXException {

			finished();
			// Let parent resume handling SAX events
			xmlReader.setContentHandler(docHandler);
		}
	}

	/**
	 * Handler for the root element.
	 */
	protected class RootHandler extends DefaultHandler {

		public RootHandler() {
			super();
		}

		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

			if (DEBUG)
				Log.log(LogService.LOG_DEBUG, "BEGIN RootHandler:startElement():" //$NON-NLS-1$
						+ qName);
			String name = getName(localName, qName);
			if (name.equalsIgnoreCase(METADATA)) {
				new MetaDataHandler(this).init(name, attributes);
			} else {
				Log.log(LogService.LOG_WARNING, NLS.bind(AutoconfMetaTypeMsg.UNEXPECTED_ELEMENT, name));
			}
			if (DEBUG)
				Log.log(LogService.LOG_DEBUG, "END   RootHandler:startElement():" //$NON-NLS-1$
						+ qName);
		}

		public void setDocumentLocator(Locator locator) {
			// do nothing
		}
	}

	/**
	 * Handler for the MetaData element.
	 */
	protected class MetaDataHandler extends AbstractHandler {

		public MetaDataHandler(ContentHandler handler) {
			super(handler);
			if (DEBUG)
				Log.log(LogService.LOG_DEBUG, "Here is MetaDataHandler():MetaDataHandler(ContentHandler handler)"); //$NON-NLS-1$

		}

		public void init(String name, Attributes attributes) throws SAXException {

			if (DEBUG)
				Log.log(LogService.LOG_DEBUG, "Here is MetaDataHandler():init()"); //$NON-NLS-1$
			localization = attributes.getValue(LOCALIZATION);
			if (localization == null) {
				// Not a problem, because LOCALIZATION is an optional attribute.
			}
			// The global variable "_dp_localization" will be used within
			// OcdHandler and AttributeDefinitionHandler later.
		}

		public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {

			if (DEBUG)
				Log.log(LogService.LOG_DEBUG, "Here is MetaDataHandler:startElement():" //$NON-NLS-1$
						+ qName);
			String name = getName(localName, qName);
			if (name.equalsIgnoreCase(DESIGNATE)) {
				DesignateHandler designateHandler = new DesignateHandler(this);
				designateHandler.init(name, atts);
				if (designateHandler.parsedDataValid) {
					designateHandlers.addElement(designateHandler);
				}
			} else if (name.equalsIgnoreCase(OCD)) {
				OcdHandler ocdHandler = new OcdHandler(this);
				ocdHandler.init(name, atts, ocdIdToOCDimpl);
			} else {
				Log.log(LogService.LOG_WARNING, NLS.bind(AutoconfMetaTypeMsg.UNEXPECTED_ELEMENT, name));
			}
		}

		protected void finished() throws SAXException {

			if (DEBUG)
				Log.log(LogService.LOG_DEBUG, "Here is MetaDataHandler():finished()"); //$NON-NLS-1$
			if (designateHandlers.size() == 0) {
				// Schema defines at least one DESIGNATE is required.
				parsedDataValid = false;
				Log.log(LogService.LOG_ERROR, this, "finished()", //$NON-NLS-1$
						NLS.bind(AutoconfMetaTypeMsg.MISSING_ELEMENT, DESIGNATE));
				return;
			}
			Enumeration designateHandlerKeys = designateHandlers.elements();
			while (designateHandlerKeys.hasMoreElements()) {
				DesignateHandler designateHandler = (DesignateHandler) designateHandlerKeys.nextElement();

				ObjectClassDefinitionImpl aocd = (ObjectClassDefinitionImpl) ocdIdToOCDimpl.get(designateHandler.getOcdRef());
				if (aocd != null) {
					designateHandler.ocd = aocd;//					
					// if (designateHandler.fpid == null) {
					// aocd.setType(ObjectClassDefinitionImpl.PID);
					// _dp_pid_to_OCDs_.put(designateHandler.pid, aocd);
					// } else {
					// aocd.setType(ObjectClassDefinitionImpl.FPID);
					// _dp_pid_to_OCDs_.put(designateHandler.fpid, aocd);
					// }
				} else {
					// TODO try to get MetaTypeService and get OCD from it.
					metaTypeTracker.open();
					MetaTypeService metaType = (MetaTypeService) metaTypeTracker.getService();
					if (metaType != null) {
						ObjectClassDefinition ocd;
						ocd = getOcdFromMetatype(designateHandler, metaType);
						if (ocd != null) {
							designateHandler.ocd = aocd;
							// if (designateHandler.fpid == null) {
							// aocd = new ObjectClassDefinitionImpl(ocd,
							// ObjectClassDefinitionImpl.PID);
							// _dp_pid_to_OCDs_.put(designateHandler.pid, aocd);
							// } else {
							// aocd = new ObjectClassDefinitionImpl(ocd,
							// ObjectClassDefinitionImpl.FPID);
							// _dp_pid_to_OCDs_.put(designateHandler.fpid,
							// aocd);
							// }
							return;
						}
					}
					Log.log(LogService.LOG_ERROR, this, "finished()", //$NON-NLS-1$
							NLS.bind(AutoconfMetaTypeMsg.OCD_ID_NOT_FOUND, designateHandler.getOcdRef()));
				}
			}
		}

		/**
		 * @param designateHandler
		 * @param metaType
		 * @throws ResourceProcessorException
		 */
		private ObjectClassDefinition getOcdFromMetatype(DesignateHandler designateHandler, MetaTypeService metaType) {
			// retrieve of OCD by using MetaTypeService.
			String bundleLocation = designateHandler.bundleLocation;
			Bundle targetBundle = null;
			if (designateHandler.fpid == null) { // Search among bundles in
				// the same DP.
				if (!bundleLocation.startsWith(DpConstants.DP_LOCATION_PREFIX)) {
					Log.log(LogService.LOG_ERROR, "BundleLocation (=" + bundleLocation + ") must start with " + DpConstants.DP_LOCATION_PREFIX);
					return null;
				}
				String bundleSymbolicName = bundleLocation.substring(DpConstants.DP_LOCATION_PREFIX.length());
				targetBundle = Utils.getBundleFromDpDoPrivileged(sourceDp, bundleSymbolicName);
			} else { // Search among bundles in the same DP.
				Bundle[] bundles = context.getBundles();
				for (int i = 0; i < bundles.length; i++) {
					String location = Utils.getBundleLocationDoPrivileged(bundles[i]);
					if (location.equals(bundleLocation)) {
						targetBundle = bundles[i];
						break;
					}
				}
			}
			MetaTypeInformation metaInfo = metaType.getMetaTypeInformation(targetBundle);
			if (metaInfo != null) {
				String[] pids = metaInfo.getPids();
				// String[] fpids = metaInfo.getFactoryPids();
				// String[] locales = metaInfo.getLocales();
				for (int i = 0; i < pids.length; i++) {
					ObjectClassDefinition ocd = metaInfo.getObjectClassDefinition(pids[i], null);
					if (ocd.getID().equals(designateHandler.getOcdRef())) {
						return ocd;
					}
				}
			}
			return null;
		}
	}

	/**
	 * Handler for the ObjectClassDefinition element.
	 */
	protected class OcdHandler extends AbstractHandler {

		Hashtable _parent_OCDs_hashtable;

		// This ID "_refID" is only used for reference by Designate element,
		// not the PID or FPID of this OCD.
		String _refID;

		ObjectClassDefinitionImpl _ocd;

		Vector _ad_vector = new Vector(7);

		public OcdHandler(ContentHandler handler) {
			super(handler);
		}

		public void init(String name, Attributes atts, Hashtable ocds_hashtable) throws SAXException {

			if (DEBUG)
				Log.log(LogService.LOG_DEBUG, "Here is OcdHandler():init()"); //$NON-NLS-1$
			_parent_OCDs_hashtable = ocds_hashtable;

			String ocd_name_val = atts.getValue(NAME);
			// if (ocd_name_val == null) {
			// _isParsedDataValid = false;
			// Log.log(LogService.LOG_ERROR, this, "init(String, Attributes,
			// Hashtable)", //$NON-NLS-1$
			// NLS.bind(MetaTypeMsg.MISSING_ATTRIBUTE, NAME, name));
			// return;
			// }

			String ocd_description_val = atts.getValue(DESCRIPTION);
			if (ocd_description_val == null) {
				// Not a problem, because DESCRIPTION is an optional
				// attribute.
			}

			_refID = atts.getValue(ID);
			if (_refID == null) {
				parsedDataValid = false;
				Log.log(LogService.LOG_ERROR, this, "init(String, Attributes, Hashtable)", //$NON-NLS-1$
						NLS.bind(AutoconfMetaTypeMsg.MISSING_ATTRIBUTE, ID, name));
				return;
			}

			_ocd = new ObjectClassDefinitionImpl(ocd_name_val, ocd_description_val, _refID, localization);

		}

		public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {

			if (DEBUG)
				Log.log(LogService.LOG_DEBUG, "Here is OcdHandler:startElement():" //$NON-NLS-1$
						+ qName);
			if (!parsedDataValid)
				return;

			String name = getName(localName, qName);
			if (name.equalsIgnoreCase(AD)) {
				AttributeDefinitionHandler attributeDefHandler = new AttributeDefinitionHandler(this);
				attributeDefHandler.init(name, atts, _ad_vector);
				// } else if (name.equalsIgnoreCase(ICON)) {
				// IconHandler iconHandler = new IconHandler(this);
				// iconHandler.init(name, atts);
				// if (iconHandler._isParsedDataValid) {
				// // Because XML schema allows at most one icon for
				// // one OCD, if more than one icons are read from
				// // MetaData, then only the final icon will be kept.
				// _ocd.setIcon(iconHandler._icon);
				// }
			} else {
				Log.log(LogService.LOG_WARNING, NLS.bind(AutoconfMetaTypeMsg.UNEXPECTED_ELEMENT, name));
			}

		}

		protected void finished() throws SAXException {

			if (DEBUG)
				Log.log(LogService.LOG_DEBUG, "BEGIN OcdHandler():finished()"); //$NON-NLS-1$
			if (!parsedDataValid)
				return;

			if (_ad_vector.size() == 0) {
				// Schema defines at least one AD is required.
				parsedDataValid = false;
				Log.log(LogService.LOG_ERROR, this, "finished()", //$NON-NLS-1$
						NLS.bind(AutoconfMetaTypeMsg.MISSING_ELEMENT, AD, _refID));
				return;
			}
			// OCD gets all parsed ADs.
			Enumeration adKey = _ad_vector.elements();
			while (adKey.hasMoreElements()) {
				AttributeDefinitionImpl ad = (AttributeDefinitionImpl) adKey.nextElement();
				_ocd.addAttributeDefinition(ad, ad.isRequired());
			}

			_parent_OCDs_hashtable.put(_refID, _ocd);
			if (DEBUG)
				Log.log(LogService.LOG_DEBUG, "END   OcdHandler():finished()"); //$NON-NLS-1$

		}
	}

	// /**
	// * Handler for the Icon element.
	// */
	// protected class IconHandler extends AbstractHandler {
	//
	// Icon _icon;
	//
	// public IconHandler(ContentHandler handler) {
	// super(handler);
	// }
	//
	// public void init(String name, Attributes atts) throws SAXException {
	//
	// Log.log(LogService.LOG_DEBUG, "Here is IconHandler:init()");
	// //$NON-NLS-1$
	// String icon_resource_val = atts.getValue(RESOURCE);
	// if (icon_resource_val == null) {
	// _isParsedDataValid = false;
	// Log.log(LogService.LOG_ERROR, this, "init(String, Attributes)",
	// //$NON-NLS-1$
	// NLS.bind(MetaTypeMsg.MISSING_ATTRIBUTE, RESOURCE, name));
	// return;
	// }
	//
	// String icon_size_val = atts.getValue(SIZE);
	// if (icon_size_val == null) {
	// // Not a problem, because SIZE is an optional attribute.
	// icon_size_val = "0"; //$NON-NLS-1$
	// } else if (icon_size_val.equalsIgnoreCase("")) { //$NON-NLS-1$
	// icon_size_val = "0"; //$NON-NLS-1$
	// }
	//
	// _icon = new Icon(icon_resource_val, Integer.parseInt(icon_size_val),
	// _dp_bundle);
	// }
	// }

	/**
	 * Handler for the Attribute element.
	 */
	protected class AttributeDefinitionHandler extends AbstractHandler {

		AttributeDefinition ad;

		int dataType;

		Vector parentADsVector;

		Vector optionLabelVector = new Vector(7);

		Vector optionValueVector = new Vector(7);

		public AttributeDefinitionHandler(ContentHandler handler) {
			super(handler);
		}

		public void init(String name, Attributes atts, Vector ad_vector) throws SAXException {

			if (DEBUG)
				Log.log(LogService.LOG_DEBUG, "Here is AttributeDefinitionHandler():init()"); //$NON-NLS-1$
			parentADsVector = ad_vector;

			String ad_name_val = atts.getValue(NAME);
			if (ad_name_val == null) {
				// Not a problem, because NAME is an optional attribute.
			}

			String ad_description_val = atts.getValue(DESCRIPTION);
			if (ad_description_val == null) {
				// Not a problem, because DESCRIPTION is an optional attribute.
			}

			String ad_id_val = atts.getValue(ID);
			if (ad_id_val == null) {
				parsedDataValid = false;
				Log.log(LogService.LOG_ERROR, this, "init(String, Attributes, Vector)", //$NON-NLS-1$
						NLS.bind(AutoconfMetaTypeMsg.MISSING_ATTRIBUTE, ID, name));
				return;
			}

			String ad_type_val = atts.getValue(TYPE);
			if (ad_type_val.equalsIgnoreCase(STRING)) {
				dataType = AttributeDefinition.STRING;
			} else if (ad_type_val.equalsIgnoreCase(LONG)) {
				dataType = AttributeDefinition.LONG;
			} else if (ad_type_val.equalsIgnoreCase(DOUBLE)) {
				dataType = AttributeDefinition.DOUBLE;
			} else if (ad_type_val.equalsIgnoreCase(FLOAT)) {
				dataType = AttributeDefinition.FLOAT;
			} else if (ad_type_val.equalsIgnoreCase(INTEGER)) {
				dataType = AttributeDefinition.INTEGER;
			} else if (ad_type_val.equalsIgnoreCase(BYTE)) {
				dataType = AttributeDefinition.BYTE;
			} else if (ad_type_val.equalsIgnoreCase(CHAR)) {
				dataType = AttributeDefinition.CHARACTER;
			} else if (ad_type_val.equalsIgnoreCase(BOOLEAN)) {
				dataType = AttributeDefinition.BOOLEAN;
			} else if (ad_type_val.equalsIgnoreCase(SHORT)) {
				dataType = AttributeDefinition.SHORT;
			} else {
				parsedDataValid = false;
				Log.log(LogService.LOG_ERROR, this, "init(String, Attributes, Vector)", //$NON-NLS-1$
						NLS.bind(AutoconfMetaTypeMsg.MISSING_ATTRIBUTE, TYPE, name));
				return;

			}

			String ad_cardinality_str = atts.getValue(CARDINALITY);
			int ad_cardinality_val = 0;
			if (ad_cardinality_str == null) {
				// Not a problem, because CARDINALITY is an optional attribute.
				// And the default value is 0.
			} else {
				ad_cardinality_val = Integer.parseInt(ad_cardinality_str);
			}

			String ad_min_val = atts.getValue(MIN);
			if (ad_min_val == null) {
				// Not a problem, because MIN is an optional attribute.
			}

			String ad_max_val = atts.getValue(MAX);
			if (ad_max_val == null) {
				// Not a problem, because MAX is an optional attribute.
			}

			String ad_defaults_str = atts.getValue(DEFAULT);
			if (ad_defaults_str == null) {
				// For AutoConfiguration, default value can be null.
				// // Not a problem, because DEFAULT is an optional attribute.
				// if (ad_cardinality_val == 0) {
				// // But when it is not assigned, CARDINALITY cannot be '0'.
				// parsedDataValid = false;
				// Log.log(LogService.LOG_ERROR, this, "init(String, Attributes,
				// Vector)", //$NON-NLS-1$
				// AutoconfMetaTypeMsg.NULL_DEFAULTS);
				// return;
				// }
			}

			String ad_required_val = atts.getValue(REQUIRED);
			if (ad_required_val == null) {
				// Not a problem, because REQUIRED is an optional attribute.
				// And the default value is 'true'.
				ad_required_val = Boolean.TRUE.toString();
			}

			ad = new AttributeDefinitionImpl(ad_id_val, ad_name_val, ad_description_val, dataType, ad_cardinality_val, convert(ad_min_val, dataType), convert(ad_max_val, dataType), Boolean.valueOf(ad_required_val).booleanValue(), localization);
			//
			// if (ad_cardinality_val == 0) {
			// // Attribute DEFAULT has one and only one occurance.
			// _ad.setDefaultValue(new String[] {ad_defaults_str}, false);
			// } else {
			// // Attribute DEFAULT is a comma delimited list.
			// _ad.setDefaultValue(ad_defaults_str, false);
			// }
		}

		public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {

			if (DEBUG)
				Log.log(LogService.LOG_DEBUG, "Here is AttributeDefinitionHandler:startElement():" //$NON-NLS-1$
						+ qName);
			if (!parsedDataValid)
				return;

			String name = getName(localName, qName);
			// if (name.equalsIgnoreCase(OPTION)) {
			// OptionHandler optionHandler = new OptionHandler(this);
			// optionHandler.init(name, atts);
			// if (optionHandler._isParsedDataValid) {
			// // Only add valid Option
			// _optionLabel_vector.addElement(optionHandler._label_val);
			// _optionValue_vector.addElement(optionHandler._value_val);
			// }
			// } else {
			if (name != null && name.length() != 0)
				Log.log(LogService.LOG_WARNING, NLS.bind(AutoconfMetaTypeMsg.UNEXPECTED_ELEMENT, name));

		}

		protected void finished() throws SAXException {

			if (DEBUG)
				Log.log(LogService.LOG_DEBUG, "BEGIN is AttributeDefinitionHandler():finished()"); //$NON-NLS-1$
			if (!parsedDataValid)
				return;

			// _ad.setOption(_optionLabel_vector, _optionValue_vector, true);
			parentADsVector.addElement(ad);
		}
	}

	// /**
	// * Handler for the Option element.
	// */
	// protected class OptionHandler extends AbstractHandler {
	//
	// String _label_val;
	// String _value_val;
	//
	// public OptionHandler(ContentHandler handler) {
	// super(handler);
	// }
	//
	// public void init(String name, Attributes atts) throws SAXException {
	//
	// Log.log(LogService.LOG_DEBUG, "Here is OptionHandler:init()");
	// //$NON-NLS-1$
	// _label_val = atts.getValue(LABEL);
	// if (_label_val == null) {
	// _isParsedDataValid = false;
	// Log.log(LogService.LOG_ERROR, this, "init(String, Attributes)",
	// //$NON-NLS-1$
	// NLS.bind(MetaTypeMsg.MISSING_ATTRIBUTE, LABEL, name));
	// return;
	// }
	//
	// _value_val = atts.getValue(VALUE);
	// if (_value_val == null) {
	// _isParsedDataValid = false;
	// Log.log(LogService.LOG_ERROR, this, "init(String, Attributes)",
	// //$NON-NLS-1$
	// NLS.bind(MetaTypeMsg.MISSING_ATTRIBUTE, VALUE, name));
	// return;
	// }
	// }
	// }

	/**
	 * Handler for the Designate element.
	 */
	public class DesignateHandler extends AbstractHandler {

		String pid = null;

		String fpid = null;

		String bundleLocation = null;

		boolean optional = false;

		boolean merge = false;

		// String ocdref;

		ObjectHandler objectHandler;

		ObjectClassDefinition ocd;

		public DesignateHandler(ContentHandler handler) {
			super(handler);
		}

		public void init(String name, Attributes atts) throws SAXException {

			if (DEBUG)
				Log.log(LogService.LOG_DEBUG, "Here is DesignateHandler():init()"); //$NON-NLS-1$
			pid = atts.getValue(PID);
			if (pid == null) {
				parsedDataValid = false;
				Log.log(LogService.LOG_ERROR, this, "init(String, Attributes)", //$NON-NLS-1$
						NLS.bind(AutoconfMetaTypeMsg.MISSING_ATTRIBUTE, PID, name));
				return;
			}
			fpid = atts.getValue(FACTORY);

			bundleLocation = atts.getValue(BUNDLE);
			if (bundleLocation == null) {
				// For Autoconfig, bundlelocation is mandatory
				parsedDataValid = false;
				Log.log(LogService.LOG_ERROR, this, "init(String, Attributes)", //$NON-NLS-1$
						NLS.bind(AutoconfMetaTypeMsg.MISSING_ATTRIBUTE, BUNDLE, name));
				return;
			}

			String optional_str = atts.getValue(OPTIONAL);
			if (optional_str == null) {
				// Not a problem, because OPTIONAL is an optional attribute.
				// The default value is "false".
				optional = false;
			} else {
				optional = Boolean.valueOf(optional_str).booleanValue();
			}

			String merge_str = atts.getValue(MERGE);
			if (merge_str == null) {
				// Not a problem, because MERGE is an optional attribute.
				// The default value is "false".
				merge = false;
			} else {
				merge = Boolean.valueOf(merge_str).booleanValue();
			}
		}

		public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {

			if (DEBUG)
				Log.log(LogService.LOG_DEBUG, "Here is DesignateHandler:startElement():" //$NON-NLS-1$
						+ qName);
			if (!parsedDataValid)
				return;

			String name = getName(localName, qName);
			if (name.equalsIgnoreCase(OBJECT)) {
				ObjectHandler objectHandler = new ObjectHandler(this);
				objectHandler.init(name, atts);
				if (objectHandler.parsedDataValid) {
					// ocdref = objectHandler.ocdref;
					this.objectHandler = objectHandler;
				}
			} else {
				Log.log(LogService.LOG_WARNING, NLS.bind(AutoconfMetaTypeMsg.UNEXPECTED_ELEMENT, name));
			}
		}

		public String getPid() {
			return pid;
		}

		public String getFpid() {
			return fpid;
		}

		public Vector getAttributeHandlers() {
			return objectHandler.attributeHandlers;
		}

		public String getBundleLocation() {
			return bundleLocation;
		}

		public boolean isMerge() {
			return merge;
		}

		public boolean isOptional() {
			return optional;
		}

		public String getOcdRef() {
			return objectHandler.ocdref;
		}

		public ObjectClassDefinition getOcd() {
			return ocd;
		}
	}

	/**
	 * Handler for the Object element.
	 */
	protected class ObjectHandler extends AbstractHandler {

		// Referenced OCD ID
		String ocdref;

		Vector attributeHandlers;

		public ObjectHandler(ContentHandler handler) {
			super(handler);
		}

		public void init(String name, Attributes atts) throws SAXException {

			if (DEBUG)
				Log.log(LogService.LOG_DEBUG, "Here is ObjectHandler():init()"); //$NON-NLS-1$
			ocdref = atts.getValue(OCDREF);
			if (ocdref == null) {
				parsedDataValid = false;
				Log.log(LogService.LOG_ERROR, this, "init(String, Attributes)", //$NON-NLS-1$
						NLS.bind(AutoconfMetaTypeMsg.MISSING_ATTRIBUTE, OCDREF, name));
				return;
			}
		}

		public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {

			if (DEBUG)
				Log.log(LogService.LOG_DEBUG, "Here is ObjectHandler:startElement():" //$NON-NLS-1$
						+ qName);
			if (!parsedDataValid)
				return;

			String name = getName(localName, qName);
			if (DEBUG)
				Log.log(LogService.LOG_DEBUG, "Here is ObjectHandler:startElement():name=" //$NON-NLS-1$
						+ name);
			if (name.equalsIgnoreCase(ATTRIBUTE)) {
				if (DEBUG)
					Log.log(LogService.LOG_DEBUG, "Here is ObjectHandler:startElement():Attribute matched" //$NON-NLS-1$
							+ qName);
				AttributeHandler attributeHandler = new AttributeHandler(this);
				attributeHandler.init(name, atts);
				if (attributeHandler.parsedDataValid) {
					if (attributeHandlers == null)
						attributeHandlers = new Vector(3);
					attributeHandlers.addElement(attributeHandler);
				}
			} else {
				Log.log(LogService.LOG_WARNING, NLS.bind(AutoconfMetaTypeMsg.UNEXPECTED_ELEMENT, name));
			}
		}
	}

	/**
	 * Handler for the Attribute element.
	 * 
	 * This Handler is only used by RFC94.
	 */
	public class AttributeHandler extends AbstractHandler {

		String adref;

		String content;

		/** keyToSetProp will be used as a key of Properties for Configuration. */
		String keyToSetProp;

		/**
		 * values will be used as a value of Properties for Configuration. It
		 * might be an Object, a Vector, an Array according to referring OCD's
		 * AD.
		 */
		Vector valueHandlers;

		public AttributeHandler(ContentHandler handler) {
			super(handler);
		}

		public void init(String name, Attributes atts) throws SAXException {

			if (DEBUG)
				Log.log(LogService.LOG_DEBUG, "Here is AttributeHandler():init()"); //$NON-NLS-1$
			adref = atts.getValue(ADREF);
			if (adref == null) {
				parsedDataValid = false;
				Log.log(LogService.LOG_ERROR, this, "init(String, Attributes)", //$NON-NLS-1$
						NLS.bind(AutoconfMetaTypeMsg.MISSING_ATTRIBUTE, ADREF, name));
				return;
			}

			content = atts.getValue(CONTENT);
			if (content != null)
				Log.log(LogService.LOG_DEBUG, "Here is AttributeHandler:init():content=" //$NON-NLS-1$
						+ content);

			String attrName = atts.getValue(NAME);

			if (content == null && attrName != null) {
				parsedDataValid = false;
				Log.log(LogService.LOG_ERROR, this, "init(String, Attributes)", //$NON-NLS-1$
						"content == null && name != null");
				return;
				// } else if (content != null && keyToSetProp == null) {
				// parsedDataValid = false;
				// Log.log(LogService.LOG_ERROR, this, "init(String,
				// Attributes)", //$NON-NLS-1$
				// "content != null && name == null");
				// return;
			}
			if (attrName != null)
				keyToSetProp = attrName;
			else
				keyToSetProp = adref;
			if (DEBUG)
				Log.log(LogService.LOG_DEBUG, "Here is AttributeHandler:init():key=" //$NON-NLS-1$
						+ keyToSetProp);

		}

		public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {

			if (DEBUG)
				Log.log(LogService.LOG_DEBUG, "Here is AttributeHandler:startElement():" //$NON-NLS-1$
						+ qName);
			if (!parsedDataValid)
				return;

			String name = getName(localName, qName);
			if (name.equalsIgnoreCase(VALUE)) {
				if (DEBUG)
					Log.log(LogService.LOG_DEBUG, "Here is AttributeHandler:startElement():value matched" //$NON-NLS-1$
							+ qName);
				if (valueHandlers == null)
					valueHandlers = new Vector(3);
				SimpleValueHandler valueHandler = new SimpleValueHandler(this);
				valueHandler.init(name, atts, valueHandlers);
			} else {
				Log.log(LogService.LOG_WARNING, NLS.bind(AutoconfMetaTypeMsg.UNEXPECTED_ELEMENT, name));
			}
		}

		protected void finished() throws SAXException {

			if (DEBUG)
				Log.log(LogService.LOG_DEBUG, "Here is AttributeHandler():finished()"); //$NON-NLS-1$
			if (!parsedDataValid)
				return;

			if (content != null && valueHandlers != null) {
				parsedDataValid = false;
				Log.log(LogService.LOG_ERROR, this, "init(String, Attributes)", //$NON-NLS-1$
						NLS.bind(AutoconfMetaTypeMsg.INVALID_SYNTAX, "Both content and values are set."));
				return;
			}

			if (adref == null) {
				parsedDataValid = false;
				// Schema defines at least one OBJECT is required.
				Log.log(LogService.LOG_ERROR, this, "finished()", //$NON-NLS-1$
						NLS.bind(AutoconfMetaTypeMsg.MISSING_ELEMENT, OBJECT, adref));
				return;
			}

			// if(_valueHandlers != null){
			// Enumeration enum = _valueHandlers.elements();
			// while(enum.hasMoreElements()){
			// ValueHandler handler = (ValueHandler) enum.nextElement();
			// handler._value_val;
			// }
			// }
		}

		public String getAdref() {
			return adref;
		}

		public String getContent() {
			return content;
		}

		public String getKey() {
			return keyToSetProp;
		}

		public Vector getValues() {
			return valueHandlers;
		}
	}

	/**
	 * Handler for the Simple Value element.
	 */
	private class SimpleValueHandler extends AbstractHandler {

		StringBuffer buffer = new StringBuffer();

		Vector parentValueVector;

		String elementName;

		public SimpleValueHandler(ContentHandler handler) {
			super(handler);
		}

		public void init(String name, Attributes atts, Vector valueVector) throws SAXException {

			if (DEBUG)
				Log.log(LogService.LOG_DEBUG, "Here is SimpleValueHandler():init()"); //$NON-NLS-1$
			elementName = name;
			parentValueVector = valueVector;
		}

		protected void finished() throws SAXException {

			if (DEBUG)
				Log.log(LogService.LOG_DEBUG, "Here is SimpleValueHandler():finished()"); //$NON-NLS-1$
			if (parentValueVector != null) {
				parentValueVector.addElement(buffer.toString());
			}
		}

		public void characters(char buf[], int offset, int len) throws SAXException {

			if (DEBUG)
				Log.log(LogService.LOG_DEBUG, "Here is SimpleValueHandler(" //$NON-NLS-1$
						+ elementName + "):characters():[" //$NON-NLS-1$
						+ new String(buf, offset, len) + "]"); //$NON-NLS-1$
			buffer.append(new String(buf, offset, len));
		}
	}

	// /**
	// * Handler for the Value element.
	// */
	// protected class ValueHandler extends AbstractHandler {
	// String _value_val;
	//
	// public ValueHandler(ContentHandler handler) {
	// super(handler);
	// }
	//
	// public void init(String name, Attributes atts) throws SAXException {
	//
	// Log.log(LogService.LOG_DEBUG, "Here is ValueHandler():init()");
	// //$NON-NLS-1$
	// if (atts != null && atts.getLength() != 0) {
	// _isParsedDataValid = false;
	// /*TODO*/
	// Log.log(LogService.LOG_ERROR, this, "init(String, Attributes)",
	// //$NON-NLS-1$
	// MetaTypeMsg.INVALID_ATTRIBUTE);
	// return;
	// }
	// }
	//
	// public void startElement(String uri, String localName, String qName,
	// Attributes atts) throws SAXException {
	//
	// Log.log(LogService.LOG_DEBUG, "Here is ValueHandler:startElement():"
	// //$NON-NLS-1$
	// + qName);
	// if (!_isParsedDataValid)
	// return;
	// _value_val = atts.getValue(0);
	// Log.log(LogService.LOG_DEBUG, "ValueHandler:startElement(): value="
	// //$NON-NLS-1$
	// + _value_val);
	// }
	// }

	/**
	 * Error Handler to report errors and warnings
	 */
	protected static class MyErrorHandler implements ErrorHandler {

		/** Error handler output goes here */
		private PrintStream _out;

		public MyErrorHandler(PrintStream out) {
			this._out = out;
		}

		/**
		 * Returns a string describing parse exception details
		 */
		private String getParseExceptionInfo(SAXParseException spe) {
			String systemId = spe.getSystemId();
			if (systemId == null) {
				systemId = "null"; //$NON-NLS-1$
			}
			String info = "URI=" + systemId + //$NON-NLS-1$
					" Line=" + spe.getLineNumber() + //$NON-NLS-1$
					": " + spe.getMessage(); //$NON-NLS-1$

			return info;
		}

		// The following methods are standard SAX ErrorHandler methods.
		// See SAX documentation for more info.

		public void warning(SAXParseException spe) throws SAXException {
			_out.println("Warning: " + getParseExceptionInfo(spe)); //$NON-NLS-1$
		}

		public void error(SAXParseException spe) throws SAXException {
			String message = "Error: " + getParseExceptionInfo(spe); //$NON-NLS-1$
			throw new SAXException(message);
		}

		public void fatalError(SAXParseException spe) throws SAXException {
			String message = "Fatal Error: " + getParseExceptionInfo(spe); //$NON-NLS-1$
			throw new SAXException(message);
		}
	}

	public static String getName(String localName, String qName) {
		if (localName != null && localName.length() > 0) {
			return localName;
		}

		int nameSpaceIndex = qName.indexOf(":"); //$NON-NLS-1$
		return nameSpaceIndex == -1 ? qName : qName.substring(nameSpaceIndex + 1);
	}

	public Vector get_dp_designateHandlers() {
		return designateHandlers;
	}
}
