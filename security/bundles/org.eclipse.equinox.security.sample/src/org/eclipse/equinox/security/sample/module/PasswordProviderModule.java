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

import java.util.Map;
import javax.crypto.spec.PBEKeySpec;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.spi.LoginModule;
import org.eclipse.equinox.security.auth.credentials.CredentialsFactory;
import org.eclipse.equinox.security.auth.credentials.IPrivateCredential;

public class PasswordProviderModule implements LoginModule {

	private static final String JAVAX_USERNAME = "javax.security.auth.login.name"; //$NON-NLS-1$
	private static final String JAVAX_PASSWORD = "javax.security.auth.login.password"; //$NON-NLS-1$

	private IPrivateCredential credential;

	private Subject subject;
	private Map state;

	public void initialize(Subject subject, CallbackHandler handler, Map state, Map options) {
		this.subject = subject;
		this.state = state;
	}

	public boolean login() {

		String username = (String) state.get(JAVAX_USERNAME);
		char[] password = (char[]) state.get(JAVAX_PASSWORD);

		if (null != username && null != password) {
			PBEKeySpec spec = new PBEKeySpec(password);
			credential = CredentialsFactory.privateCredential(spec, SubjectPasswordProvider.class.getName());
			return true;
		}

		return false;
	}

	public boolean commit() {
		subject.getPrivateCredentials().add(credential);
		return false;
	}

	public boolean logout() {
		return subject.getPrivateCredentials().remove(credential);
	}

	public boolean abort() {
		return false;
	}
}
