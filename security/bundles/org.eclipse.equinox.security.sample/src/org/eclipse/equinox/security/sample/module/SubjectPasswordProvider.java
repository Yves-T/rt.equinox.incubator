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

import java.security.AccessControlContext;
import java.security.AccessController;
import java.util.Iterator;
import java.util.Set;
import javax.crypto.spec.PBEKeySpec;
import javax.security.auth.Subject;
import org.eclipse.equinox.security.auth.credentials.IPrivateCredential;
import org.eclipse.equinox.security.storage.provider.IPreferencesContainer;
import org.eclipse.equinox.security.storage.provider.PasswordProvider;

public class SubjectPasswordProvider extends PasswordProvider {

	public PBEKeySpec getPassword(IPreferencesContainer container, int passwordType) {

		AccessControlContext context = AccessController.getContext();
		Subject subject = Subject.getSubject(context);

		Set set = subject.getPrivateCredentials(IPrivateCredential.class);

		for (Iterator it = set.iterator(); it.hasNext();) {
			IPrivateCredential cred = (IPrivateCredential) it.next();
			if (SubjectPasswordProvider.class.getName().equals(cred.getProviderID()))
				return cred.getPrivateKey();
		}
		return null;
	}
}
