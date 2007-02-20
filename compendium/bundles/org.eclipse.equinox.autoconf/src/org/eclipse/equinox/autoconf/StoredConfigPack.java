/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.equinox.autoconf;

import java.io.Serializable;

/**
 * @author iyamasak
 * 
 */
public class StoredConfigPack implements Serializable {

	protected static final long serialVersionUID = 1082820421820641930L;

	protected final String aliasPid;

	protected String actualPid;

	protected final String fpid;

	protected final String bundleLocation;

	protected final boolean optional;

	public StoredConfigPack(String aliasPid, String actualPid, String fpid, String bundleLocation, boolean optional) {
		this.aliasPid = aliasPid;
		this.actualPid = actualPid;
		this.fpid = fpid;
		this.bundleLocation = bundleLocation;
		this.optional = optional;
	}

	public StoredConfigPack(StoredConfigPack oldConfigPack) {
		this.aliasPid = oldConfigPack.aliasPid;
		this.actualPid = oldConfigPack.actualPid;
		this.fpid = oldConfigPack.fpid;
		this.bundleLocation = oldConfigPack.bundleLocation;
		this.optional = oldConfigPack.optional;
	}

	public String getAliasPid() {
		return aliasPid;
	}

	public String getFpid() {
		return fpid;
	}

	public String getBundleLocation() {
		return bundleLocation;
	}

	public String getActualPid() {
		return this.actualPid;
	}

	public boolean isOptional() {
		return optional;
	}
}
