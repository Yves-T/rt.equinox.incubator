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
import org.eclipse.equinox.internal.security.boot.proxy.*;

public class ServiceProvider extends Provider {

	private static class TypeStrToInt {
		public String typeStr;
		public int typeInt;

		public TypeStrToInt(String typeStr, int typeInt) {
			this.typeStr = typeStr;
			this.typeInt = typeInt;
		}
	}

	private static final long serialVersionUID = -696520082946329858L;

	private static final int TYPE_INT_UNKNOWN = 0;
	private static final int TYPE_INT_KEYSTORE = 1;
	private static final int TYPE_INT_TRUSTMANAGERFACTORY = 2;
	private static final int TYPE_INT_KEYMANAGERFACTORY = 3;

	private static final String TYPE_STR_KEYSTORE = "KeyStore"; //$NON-NLS-1$
	private static final String TYPE_STR_TRUSTMANAGERFACTORY = "TrustManagerFactory"; //$NON-NLS-1$
	private static final String TYPE_STR_KEYMANAGERFACTORY = "KeyManagerFactory"; //$NON-NLS-1$

	private static TypeStrToInt[] typeMap = {new TypeStrToInt(TYPE_STR_KEYSTORE, TYPE_INT_KEYSTORE), // 
			new TypeStrToInt(TYPE_STR_TRUSTMANAGERFACTORY, TYPE_INT_TRUSTMANAGERFACTORY), //  
			new TypeStrToInt(TYPE_STR_KEYMANAGERFACTORY, TYPE_INT_KEYMANAGERFACTORY) // 
	};

	public ServiceProvider() {
		super("EQUINOX", 0.1, ""); //$NON-NLS-1$//$NON-NLS-2$
	}

	public void registerService(ProviderServiceInternal providerService) {
		String key = providerService.getType() + "." + providerService.getAlgorithm(); //$NON-NLS-1$

		switch (getType(providerService.getType())) {
			case TYPE_INT_KEYSTORE :
				KeyStoreProxy.setProviderService(providerService);
				put(key, KeyStoreProxy.class.getName());
				break;
			case TYPE_INT_TRUSTMANAGERFACTORY :
				TrustManagerFactoryProxy.setProviderService(providerService);
				put(key, TrustManagerFactoryProxy.class.getName());
				break;
			case TYPE_INT_KEYMANAGERFACTORY :
				KeyManagerFactoryProxy.setProviderService(providerService);
				put(key, KeyManagerFactoryProxy.class.getName());
				break;
			default :
				break;
		}
	}

	public void unregisterService(ProviderServiceInternal providerService) {
		//TODO:
	}

	private int getType(String typeStr) {
		for (int i = 0; i < typeMap.length; i++) {
			if (typeMap[i].typeStr.equals(typeStr))
				return typeMap[i].typeInt;
		}
		return TYPE_INT_UNKNOWN;
	}

}
