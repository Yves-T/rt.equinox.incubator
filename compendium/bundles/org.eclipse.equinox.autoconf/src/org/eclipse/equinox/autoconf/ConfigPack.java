/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.equinox.autoconf;

import java.util.Dictionary;

import org.osgi.framework.Constants;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

public class ConfigPack extends StoredConfigPack {

	private static final long serialVersionUID = 0L;

	private Dictionary props;

	private final String resource;

	private final boolean merge;

	private Configuration configuration; // Configuration object to be updated. It might be newlyF created.

	private boolean created = false;// if configuration was newly created, true; This flag will be used in rollback.

	public ConfigPack(String resource, String aliasPid, Dictionary props, boolean merge, boolean optional) {
		super(aliasPid, (String) props.get(Constants.SERVICE_PID), (String) props.get(ConfigurationAdmin.SERVICE_FACTORYPID), (String) props.get(ConfigurationAdmin.SERVICE_BUNDLELOCATION), optional);
		this.resource = resource;
		this.props = props;
		this.merge = merge;
	}

	public ConfigPack(String resource, StoredConfigPack configPack) {
		super(configPack.aliasPid, configPack.actualPid, configPack.fpid, configPack.bundleLocation, configPack.optional);
		this.resource = resource;
		this.props = null;
		this.merge = true;
	}

	public Dictionary getProps() {
		return props;
	}

	public boolean isMerge() {
		return merge;
	}

	public String getResource() {
		return resource;
	}

	public void setProps(Dictionary aliasProps) {
		this.props = aliasProps;
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
		actualPid = configuration.getPid();
	}

	public boolean isCreated() {
		return created;
	}

	public void setCreated(boolean created) {
		this.created = created;
	}

}
