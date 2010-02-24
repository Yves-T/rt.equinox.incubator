/*******************************************************************************
 * Copyright (c) 2008-2009 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.equinox.internal.p2.osgi.actions;

import org.eclipse.equinox.internal.p2.touchpoint.osgi.Activator;
import org.eclipse.equinox.internal.p2.touchpoint.osgi.ServiceHelper;
import org.eclipse.equinox.p2.core.ProvisionException;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.p2.engine.IProfile;
import org.eclipse.equinox.p2.metadata.IArtifactKey;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepositoryManager;
import org.eclipse.equinox.p2.repository.artifact.IFileArtifactRepository;

public class Util {
	public static IStatus createError(String message) {
		return createError(message, null);
	}

	public static IStatus createError(String message, Exception e) {
		return new Status(IStatus.ERROR, Activator.ID, message, e);
	}

	public static File getArtifactFile(IArtifactKey artifactKey, IProfile profile) {
		IArtifactRepositoryManager artifactRepoMgr = (IArtifactRepositoryManager) ServiceHelper.getService(Activator.ctx, IArtifactRepositoryManager.class.getName());
		IFileArtifactRepository repo;
		try {
			repo = (IFileArtifactRepository) artifactRepoMgr.loadRepository(new URI(profile.getProperty("org.eclipse.equinox.p2.bundlepool")), null);
		} catch (ProvisionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return repo.getArtifactFile(artifactKey);
	}
	
}
