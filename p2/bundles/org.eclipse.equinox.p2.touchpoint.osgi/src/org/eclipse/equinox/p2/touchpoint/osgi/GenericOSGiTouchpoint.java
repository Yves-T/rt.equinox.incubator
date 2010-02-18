/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.equinox.p2.touchpoint.osgi;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.p2.engine.IProfile;
import org.eclipse.equinox.p2.engine.spi.Touchpoint;
import org.eclipse.equinox.p2.osgi.actions.Util;
import org.osgi.framework.BundleException;

public class GenericOSGiTouchpoint extends Touchpoint {
	public static final String BUNDLES_TO_REMOVE = "org.eclipse.equinox.p2.osgi.toRemove";
	public static final String BUNDLES_TO_ADD = "org.eclipse.equinox.p2.osgi.toAdd";
	public static final String PARM_PROFILE = "profile"; //$NON-NLS-1$
	public static final String PARM_IU = "iu"; //$NON-NLS-1$
	public static final String PARM_ARTIFACT_REQUESTS = "artifactRequests"; //$NON-NLS-1$
	public static final String PARM_OPERAND = "operand"; //$NON-NLS-1$
	public static final String PARM_BUNDLE = "bundle"; //$NON-NLS-1$
	
	//TODO This needs to be put in some sort of table mapped by Profile, otherwise we can run into problems when multiple threads are performing changes
	public static Collection<String> toAdd = new HashSet<String>();
	public static Collection<String> toRemove = new HashSet<String>();

	/** NOT API -- this is for backwards compatibility only */
	public String qualifyAction(String actionId) {
		if (actionId.equals("collect"))
			return "org.eclipse.equinox.p2.touchpoint.osgi.collect";
		if (actionId.equals("installBundle"))
			return "org.eclipse.equinox.p2.touchpoint.osgi.installBundle";
		return actionId;
	}

	public IStatus initializePhase(IProgressMonitor monitor, IProfile profile, String phaseId, Map<String,Object> touchpointParameters) {
		touchpointParameters.put(BUNDLES_TO_ADD, toAdd);
		touchpointParameters.put(BUNDLES_TO_REMOVE, toRemove);
		return Status.OK_STATUS;
	}

	public IStatus completePhase(IProgressMonitor monitor, IProfile profile, String phaseId, Map<String,Object> touchpointParameters) {
		return Status.OK_STATUS;
	}


	public IStatus prepare(IProfile profile) {
		return Status.OK_STATUS;
	}

	public IStatus commit(IProfile profile) {
		for (String id : toAdd) {
			try {
				Activator.ctx.installBundle(id);
			} catch (BundleException e) {
				return Util.createError("Failing installation", e);
			}
		}
		return Status.OK_STATUS;
	}

	public IStatus rollback(IProfile profile) {
		toAdd.clear();
		toRemove.clear();
		return Status.OK_STATUS;
	}


}
