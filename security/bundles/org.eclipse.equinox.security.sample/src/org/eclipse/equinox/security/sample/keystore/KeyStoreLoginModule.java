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
package org.eclipse.equinox.security.sample.keystore;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyStore;
import java.util.*;
import javax.crypto.spec.PBEKeySpec;
import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import org.eclipse.equinox.security.sample.keystore.nls.SampleMessages;

/**
 * Uses system properties to find where keystore it. Creates it if not found.
 * If login is successful puts keystore credential into the Subject
 */
public class KeyStoreLoginModule implements LoginModule {

	public static final String OPTION_KEYSTORE_URL = "keystoreUrl"; //$NON-NLS-1$
	public static final String OPTION_KEYSTORE_TYPE = "keystoreType"; //$NON-NLS-1$
	private static final String OPTION_KEYSTORE_NONE = "NONE"; //$NON-NLS-1$
	private static final String STATE_SSO_PASSWORD = "SSO_PASSWORD"; //$NON-NLS-1$

	// TBD  should it be in this class? 
	public static final String LOGINMODULE_POINT = "org.eclipse.equinox.security.sample.keystoreLoginModule"; //$NON-NLS-1$

	private URL keyStoreUrl;
	private boolean isUrlRequired = true;

	private String keyStoreType;

	private KeyStore keyStore;
	private boolean isNewKeyStore = false;

	private KeyStoreCredential cred;
	private PBEKeySpec pbeSpec;

	private Subject subject;
	private CallbackHandler handler;
	private Map state;

	public void initialize(Subject loginSubject, CallbackHandler callbackHandler, Map loginState, Map options) {
		this.subject = loginSubject;
		this.handler = callbackHandler;
		this.state = loginState;

		Object url = options.get(OPTION_KEYSTORE_URL);
		if (url instanceof String) {
			if (OPTION_KEYSTORE_NONE.equals(url)) {
				isUrlRequired = false;
				keyStoreUrl = null;
			} else {
				try {
					keyStoreUrl = new URL((String) url);
				} catch (Exception e) {
					throw new IllegalArgumentException(SampleMessages.invalidURL);
				}
			}
		}

		Object type = options.get(OPTION_KEYSTORE_TYPE);
		if (type instanceof String)
			keyStoreType = (String) type;
	}

	public boolean login() throws LoginException {
		try {
			ArrayList callbackList = new ArrayList(3);
			if (isUrlRequired) {
				URLConnection conn = keyStoreUrl.openConnection();
				int length = conn.getContentLength();
				isNewKeyStore = (length <= 0);
			}

			char[] password = null;
			// See if password was passed from a previous LogonModule
			if (state.containsKey(STATE_SSO_PASSWORD)) {
				String s = (String) state.get(STATE_SSO_PASSWORD);
				password = s.toCharArray();
			} else {
				if (isNewKeyStore) {
					callbackList.add(new TextOutputCallback(TextOutputCallback.INFORMATION, SampleMessages.enterPassword));
					callbackList.add(new PasswordCallback(SampleMessages.enterPasswordLabel, false));
					callbackList.add(new PasswordCallback(SampleMessages.confirmPasswordLabel, false));
				} else {
					callbackList.add(new TextOutputCallback(TextOutputCallback.INFORMATION, SampleMessages.enterKeystorePassword));
					callbackList.add(new PasswordCallback(SampleMessages.passwordLabel, false));
				}

				Callback[] callbackArray = (Callback[]) callbackList.toArray(new Callback[] {});
				if (handler == null)
					throw new LoginException(SampleMessages.callbackhandlerUnavailable);
				handler.handle(callbackArray);
				password = ((PasswordCallback) callbackArray[1]).getPassword();
				if (password == null)
					throw new LoginException(SampleMessages.passwordRequired);
				if (isNewKeyStore) {
					char[] confirm = ((PasswordCallback) callbackArray[2]).getPassword();
					if (!Arrays.equals(password, confirm))
						throw new LoginException(SampleMessages.passwordNomatch);
				}
			}
			pbeSpec = new PBEKeySpec(password);
		} catch (IOException e) {
			if (e.getCause() instanceof LoginException)
				throw (LoginException) e.getCause();
			LoginException le = new LoginException();
			le.initCause(e);
			throw le;
		} catch (UnsupportedCallbackException e) {
			LoginException le = new LoginException();
			le.initCause(e);
			throw le;
		}

		try {
			keyStore = KeyStore.getInstance(keyStoreType);
			keyStore.load(isNewKeyStore || !isUrlRequired ? null : keyStoreUrl.openStream(), pbeSpec.getPassword());
			if (isNewKeyStore) {
				File file = new File(keyStoreUrl.getFile());
				keyStore.store(new FileOutputStream(file), pbeSpec.getPassword());
			}
		} catch (Exception e) {
			LoginException le = new LoginException();
			le.initCause(e);
			throw le;
		}
		return true;
	}

	public boolean commit() {
		cred = new KeyStoreCredential(keyStoreType, keyStoreUrl);
		cred.setKeySpec(pbeSpec);
		subject.getPrivateCredentials().add(cred);
		return true;
	}

	// XXX abort() and logout() have identical code - make a clear() method and call it
	public boolean abort() {
		if (subject == null)
			return true;

		Set keys = subject.getPrivateCredentials(KeyStoreCredential.class);
		if (keys != null)
			subject.getPrivateCredentials().remove(cred);
		return true;
	}

	public boolean logout() {
		if (subject == null)
			return true;

		Set keys = subject.getPrivateCredentials(KeyStoreCredential.class);
		if (keys != null)
			subject.getPrivateCredentials().remove(cred);
		return true;
	}
}
