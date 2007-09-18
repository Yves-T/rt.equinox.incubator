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
package org.eclipse.equinox.security.boot;

import java.security.Provider;

import org.eclipse.equinox.internal.security.boot.ProviderServiceInternal;
import org.eclipse.equinox.internal.security.boot.proxy.KeyManagerFactoryProxy;
import org.eclipse.equinox.internal.security.boot.proxy.KeyStoreProxy;
import org.eclipse.equinox.internal.security.boot.proxy.TrustManagerFactoryProxy;

public class ServiceProvider extends Provider {

	private static final long serialVersionUID = -696520082946329858L;

	public ServiceProvider() {
		super("EQUINOX", 0.1, "");
	}

	public void registerService(ProviderServiceInternal providerService) {
		String key = providerService.getType() + "." + providerService.getAlgorithm();
		
		switch (getType(providerService.getType())) {
		   case TYPE_INT_KEYSTORE:
			   KeyStoreProxy.setProviderService(providerService);
			   this.put(key, KeyStoreProxy.class.getName());
			   break;

		   case TYPE_INT_TRUSTMANAGERFACTORY:
			   TrustManagerFactoryProxy.setProviderService(providerService);
			   this.put(key, TrustManagerFactoryProxy.class.getName());
			   break;
			   
		   case TYPE_INT_KEYMANAGERFACTORY:
			   KeyManagerFactoryProxy.setProviderService(providerService);
			   this.put(key, KeyManagerFactoryProxy.class.getName());
			   break;
		
		   default:
			   break;
		}
	}
	
	public void unregisterService(ProviderServiceInternal providerService) {
		//TODO:
	}
	
	private static final int TYPE_INT_UNKNOWN = 0;
	private static final int TYPE_INT_KEYSTORE = 1;
	private static final int TYPE_INT_TRUSTMANAGERFACTORY = 2;
	private static final int TYPE_INT_KEYMANAGERFACTORY = 3;
	
	private static final String TYPE_STR_KEYSTORE = "KeyStore";
	private static final String TYPE_STR_TRUSTMANAGERFACTORY = "TrustManagerFactory";
	private static final String TYPE_STR_KEYMANAGERFACTORY = "KeyManagerFactory";

	private static TypeStrToInt[] s_typeStrToIntMap = {
		new TypeStrToInt( TYPE_STR_KEYSTORE, TYPE_INT_KEYSTORE),
		new TypeStrToInt( TYPE_STR_TRUSTMANAGERFACTORY, TYPE_INT_TRUSTMANAGERFACTORY),
		new TypeStrToInt( TYPE_STR_KEYMANAGERFACTORY, TYPE_INT_KEYMANAGERFACTORY)
	};

	private static int getType(String typeStr) {
		int typeInt = TYPE_INT_UNKNOWN;
		for (int i=0;i<s_typeStrToIntMap.length;i++) {
			if (s_typeStrToIntMap[i].typeStr.equals(typeStr)) {
				typeInt = s_typeStrToIntMap[i].typeInt;
				break;
			}
		}
		return typeInt;
	}
	
	private static class TypeStrToInt {
		String typeStr;
		int typeInt;
		
		TypeStrToInt( String typeStr, int typeInt) {
			this.typeStr = typeStr;
			this.typeInt = typeInt;
		}
	}
}
