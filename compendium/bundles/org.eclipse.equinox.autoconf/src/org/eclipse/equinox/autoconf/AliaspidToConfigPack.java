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
import java.util.*;

import org.osgi.service.log.LogService;

public class AliaspidToConfigPack implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2715880699638761412L;

	/* aliasPid (String) -> ConfigPack */
	private Hashtable aliasPidToConfigPack;

	public AliaspidToConfigPack(int size) {
		aliasPidToConfigPack = Utils.createHashtable(size);
	}

	public void clear() {
		aliasPidToConfigPack.clear();
	}

	public Enumeration keys() {
		return aliasPidToConfigPack.keys();
	}

	public Enumeration elements() {
		return aliasPidToConfigPack.elements();
	}

	public boolean isEmpty() {
		return aliasPidToConfigPack.isEmpty();
	}

	public int size() {
		return aliasPidToConfigPack.size();
	}

	public void printoutConfigPacks() {
		System.out.println("\nprintoutConfigPacks()");
		for (Enumeration enumeration = aliasPidToConfigPack.elements(); enumeration.hasMoreElements();) {
			ConfigPack configPack = (ConfigPack) enumeration.nextElement();;
			Dictionary aliasProps = configPack.getProps();
			System.out.println(Utils.getMsgStOfConfiguration(aliasProps) + "\n");
		}
	}

	public boolean containsKey(String pid) {
		return aliasPidToConfigPack.containsKey(pid);
	}

	public boolean containsValue(StoredConfigPack configPack) {
		return aliasPidToConfigPack.containsValue(configPack);
	}

	public StoredConfigPack getByAliasPid(String pid) {
		return (StoredConfigPack) aliasPidToConfigPack.get(pid);
	}

	public StoredConfigPack put(String pid, StoredConfigPack configPack) {
		return (StoredConfigPack) aliasPidToConfigPack.put(pid, configPack);
	}

	public StoredConfigPack remove(String pid) {
		return (StoredConfigPack) aliasPidToConfigPack.remove(pid);
	}

	public AliaspidToConfigPack getOptimized() {
		Log.log(LogService.LOG_DEBUG, this, "getOptimized()", " BEGIN");
		AliaspidToConfigPack newTable = new AliaspidToConfigPack(this.size());
		for (Enumeration enumeration = this.keys(); enumeration.hasMoreElements();) {
			String aliasPid = (String) enumeration.nextElement();
			StoredConfigPack oldConfigPack = (StoredConfigPack) this.getByAliasPid(aliasPid);
			StoredConfigPack newConfigPack = new StoredConfigPack(oldConfigPack);
			newTable.put(aliasPid, newConfigPack);
		}
		return newTable;
	}

}
