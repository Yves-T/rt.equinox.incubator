package org.eclipse.equinox.internal.p2.ui.analysis.model;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.equinox.internal.p2.engine.ProfileEvent;
import org.eclipse.equinox.internal.p2.ui.analysis.AnalysisActivator;
import org.eclipse.equinox.internal.p2.ui.model.ProfileElement;
import org.eclipse.equinox.internal.provisional.p2.core.eventbus.IProvisioningEventBus;
import org.eclipse.equinox.p2.engine.IProfile;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.query.IQuery;
import org.eclipse.equinox.p2.query.IQueryResult;
import org.eclipse.equinox.p2.query.IQueryable;

public class ForeignProfiles implements IQueryable<IQueryable<IInstallableUnit>> {

	private Map<String, IQueryable<IInstallableUnit>> profileMap = new HashMap<String, IQueryable<IInstallableUnit>>();

	public void addProfile(IProfile profile) {
		AnalysisActivator.getDefault().setQueryContext();
		profileMap.put(profile.getProfileId(), profile);
		broadcastEvent(profile.getProfileId(), ProfileEvent.ADDED);
	}

	public void removeProfile(String[] profileId) {
		for (int i = 0; i < profileId.length; i++) {
			IProfile profile = (IProfile) profileMap.remove(profileId[i]);
			broadcastEvent(profile.getProfileId(), ProfileEvent.REMOVED);
		}
	}

	public IProfile getProfile(ProfileElement element) {
		return (IProfile) profileMap.get(element.getProfileId());
	}

	public IProfile getProfile(String profileid) {
		return (IProfile) profileMap.get(profileid);
	}

	private void broadcastEvent(String profileId, int reason) {
		IProvisioningEventBus eventBus = (IProvisioningEventBus) AnalysisActivator.getDefault().getAgent().getService(IProvisioningEventBus.SERVICE_NAME);
		if (eventBus != null)
			eventBus.publishEvent(new ProfileEvent(profileId, reason));
	}

	public IQueryResult<IQueryable<IInstallableUnit>> query(IQuery<IQueryable<IInstallableUnit>> query, IProgressMonitor monitor) {
		return query.perform(profileMap.values().iterator());
	}
}
