/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.equinox.internal.p2.osgi.actions;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.internal.p2.touchpoint.osgi.GenericOSGiTouchpoint;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.engine.IProfile;
import org.eclipse.equinox.p2.engine.spi.ProvisioningAction;
import org.eclipse.equinox.p2.metadata.*;
import org.eclipse.equinox.p2.repository.artifact.*;

public class CollectAction extends ProvisioningAction {
	public static final String ID = "collect"; //$NON-NLS-1$
	public static final String ARTIFACT_FOLDER = "artifact.folder"; //$NON-NLS-1$
	private IProvisioningAgent agent;

	public IStatus execute(Map<String, Object> parameters) {
		IProfile profile = (IProfile) parameters.get(GenericOSGiTouchpoint.PARM_PROFILE);
		agent = (IProvisioningAgent) parameters.get("agent"); //$NON-NLS-1$
		IInstallableUnit iu = (IInstallableUnit) parameters.get("iu");
		IArtifactRequest[] requests;
		try {
			requests = collect(iu, profile);
		} catch (ProvisionException e) {
			return e.getStatus();
		}

		@SuppressWarnings("unchecked")
		Collection<IArtifactRequest[]> artifactRequests = (Collection<IArtifactRequest[]>) parameters.get(GenericOSGiTouchpoint.PARM_ARTIFACT_REQUESTS);
		artifactRequests.add(requests);
		return Status.OK_STATUS;
	}

	public IStatus undo(Map<String, Object> parameters) {
		// nothing to do the GC usually takes care of that
		return Status.OK_STATUS;
	}

	public boolean isZipped(Collection<ITouchpointData> data) {
		if (data == null || data.size() == 0)
			return false;
		for (ITouchpointData td : data) {
			if (td.getInstruction("zipped") != null) //$NON-NLS-1$
				return true;
		}
		return false;
	}

	public Map<String, String> createArtifactDescriptorProperties(IInstallableUnit installableUnit) {
		Map<String, String> descriptorProperties = null;
		if (isZipped(installableUnit.getTouchpointData())) {
			descriptorProperties = new HashMap<String, String>();
			descriptorProperties.put(CollectAction.ARTIFACT_FOLDER, Boolean.TRUE.toString());
		}
		return descriptorProperties;
	}

	public IArtifactRequest[] collect(IInstallableUnit installableUnit, IProfile profile) throws ProvisionException {
		Collection<IArtifactKey> toDownload = installableUnit.getArtifacts();
		if (toDownload == null || toDownload.isEmpty())
			return IArtifactRepositoryManager.NO_ARTIFACT_REQUEST;

		IArtifactRepository bundlePool = getRepo(profile);

		if (bundlePool == null)
			throw new ProvisionException("no bundle pool");

		List<IArtifactRequest> requests = new ArrayList<IArtifactRequest>(toDownload.size());
		for (IArtifactKey key : toDownload) {
			if (!bundlePool.contains(key)) {
				Map<String, String> repositoryProperties = createArtifactDescriptorProperties(installableUnit);
				requests.add(getArtifactRepositoryManager().createMirrorRequest(key, bundlePool, null, repositoryProperties));
			}
		}

		if (requests.isEmpty())
			return IArtifactRepositoryManager.NO_ARTIFACT_REQUEST;

		IArtifactRequest[] result = (IArtifactRequest[]) requests.toArray(new IArtifactRequest[requests.size()]);
		return result;
	}

	public IArtifactRepositoryManager getArtifactRepositoryManager() {
		return (IArtifactRepositoryManager) agent.getService(IArtifactRepositoryManager.SERVICE_NAME);
	}

	private IArtifactRepository getRepo(IProfile profile) {
		IArtifactRepositoryManager artifactRepoMgr = getArtifactRepositoryManager();
		try {
			final String poolProp = profile.getProperty("org.eclipse.equinox.p2.bundlepool");
			if (poolProp == null)
				return null;
			return artifactRepoMgr.loadRepository(new URI(poolProp), null);
		} catch (ProvisionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}