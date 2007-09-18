/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.internal.security.boot;

import java.security.Provider;
import java.util.List;
import java.util.Map;

import org.eclipse.equinox.security.boot.ServiceProvider;

public class ProviderServiceInternal {
	
	private Provider provider;
	private ClassLoader classLoader;
	private String type;
	private String algorithm;
	private String className;
	private List aliases;
	private Map attributes;
	
	public ProviderServiceInternal(String type, String algorithm, String className, List aliases, Map attributes) {
		this.type = type;
		this.algorithm = algorithm;
		this.className = className;
		this.aliases = aliases;
		this.attributes = attributes;
	}
	
	public List getAliases() {
		return aliases;
	}
	
	public String getAlgorithm() {
		return algorithm;
	}
	
	public String getAttribute(String name) {
		return (String)attributes.get(name);
	}
	
	public Map getAttributes( ) {
		return attributes;
	}
	
	public String getClassName( ) {
		return className;
	}
	
	public Provider getProvider( ) {
		return provider;
	}
	
	public void setProvider( ServiceProvider provider) {
		this.provider = provider;
	}

	public ClassLoader getClassLoader( ) {
		return classLoader;
	}
	
	public void setClassLoader( ClassLoader classLoader) {
		this.classLoader = classLoader;
	}
	
	public String getType(){
		return type;
	}
	
	public Object newInstance(Object parameter) {
		Object obj = null;
		try {
			Class clazz = classLoader.loadClass(getClassName());
			obj = clazz.newInstance();				
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return obj;
	}

	public boolean supportsParameter(Object parameter) {
		//TODO: Throw.
		return false;
	}
}
