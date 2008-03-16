/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.security.sample.module;

import java.io.*;
import java.security.KeyStore;
import java.util.Map;
import java.util.Properties;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import org.eclipse.equinox.security.sample.AuthAppPlugin;
import org.eclipse.osgi.service.security.TrustEngine;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;

public class TrustEngineLoginModule implements LoginModule {

	private static final String JAVAX_PASSWORD = "javax.security.auth.login.password"; //$NON-NLS-1$

	private KeyStoreTrustEngine engine;
	private char[] password;
	private ServiceRegistration trustEngineReg;
	private Map state;

	public void initialize(Subject subject, CallbackHandler handler, Map state, Map options) {
		this.state = state;
	}

	public boolean login() throws LoginException {

		password = (char[]) state.get(JAVAX_PASSWORD);

		try {
			File dataDirectory = AuthAppPlugin.getBundleContext().getDataFile("");
			File keystoreFile = new File(dataDirectory, "keystore.jks");
			if (!keystoreFile.exists()) {
				keystoreFile.createNewFile();
				KeyStore keystore = KeyStore.getInstance("JKS");
				keystore.load(null, password);
				keystore.store(new FileOutputStream(keystoreFile), password);
			}

			KeyStore keystore = KeyStore.getInstance("JKS");
			keystore.load(new FileInputStream(keystoreFile), password);
			engine = new KeyStoreTrustEngine(keystore, keystoreFile.toURL(), password, "User");
		} catch (Exception e) {
			LoginException le = new LoginException();
			le.initCause(e);
			throw le;
		}

		return true;
	}

	public boolean commit() {

		Properties serviceProps = new Properties();
		serviceProps.put(Constants.SERVICE_RANKING, new Integer(Integer.MAX_VALUE));
		trustEngineReg = AuthAppPlugin.getBundleContext().registerService(TrustEngine.class.getName(), engine, serviceProps);

		return true;
	}

	public boolean logout() {
		if (null != trustEngineReg) {
			trustEngineReg.unregister();
			trustEngineReg = null;
		}
		return true;
	}

	public boolean abort() {
		return true;
	}
}
