/*******************************************************************************
 * Copyright (c) 2008-2009 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.equinox.p2.osgi.actions;

import java.io.File;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.internal.provisional.p2.engine.IProfile;
import org.eclipse.equinox.p2.engine.spi.ProvisioningAction;
import org.eclipse.equinox.p2.metadata.IArtifactKey;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.touchpoint.osgi.GenericOSGiTouchpoint;

public class InstallBundleAction extends ProvisioningAction {
	public static final String ID = "installBundle"; //$NON-NLS-1$

	public IStatus execute(Map parameters) {
		return InstallBundleAction.installBundle(parameters);
	}

	public IStatus undo(Map parameters) {
		//Nothing to do because the action only take effect on commit 
		return Status.OK_STATUS;
	}

	public static IStatus installBundle(Map parameters) {
		IProfile profile = (IProfile) parameters.get(GenericOSGiTouchpoint.PARM_PROFILE);
		IInstallableUnit iu = (IInstallableUnit) parameters.get(GenericOSGiTouchpoint.PARM_IU);

		IArtifactKey[] artifacts = iu.getArtifacts();
		if (artifacts == null || artifacts.length == 0) {
			return Status.OK_STATUS;
		}
		
		IArtifactKey artifactKey = artifacts[0];
		if (artifactKey == null)
			throw new IllegalArgumentException("No matching artifact");

		final File bundleFile = Util.getArtifactFile(artifactKey, profile);
		if (bundleFile == null || !bundleFile.exists())
			return Util.createError("The file can not be found for " + artifactKey);

		((Set) parameters.get(GenericOSGiTouchpoint.BUNDLES_TO_ADD)).add("reference:" + bundleFile.toURI().toString());
		return Status.OK_STATUS;
	}
}