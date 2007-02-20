/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.equinox.autoconf;

import java.util.Enumeration;
import java.util.Hashtable;

public class ResourceToAliaspidToConfigPack {

	/**
	 * resource -> AliaspidToConfigPack
	 */
	protected Hashtable resourceToTable = null;

	public ResourceToAliaspidToConfigPack() {
		resourceToTable = new Hashtable(4);
	}

	public AliaspidToConfigPack put(String resource, AliaspidToConfigPack configPacks) {
		return (AliaspidToConfigPack) resourceToTable.put(resource, configPacks);
	}

	public AliaspidToConfigPack getByResourceName(String resource) {
		return (AliaspidToConfigPack) resourceToTable.get(resource);
	}

	public AliaspidToConfigPack remove(String resource) {
		return (AliaspidToConfigPack) resourceToTable.remove(resource);
	}

	public void clear() {
		resourceToTable.clear();
	}

	public Enumeration keys() {
		return this.resourceToTable.keys();
	}
}
