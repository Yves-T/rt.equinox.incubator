package org.eclipse.equinox.internal.p2.ui.analysis.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.equinox.internal.p2.core.helpers.ServiceHelper;
import org.eclipse.equinox.internal.p2.engine.ProfileEvent;
import org.eclipse.equinox.internal.p2.ui.analysis.AnalysisActivator;
import org.eclipse.equinox.internal.provisional.p2.core.eventbus.IProvisioningEventBus;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.engine.IProfile;
import org.eclipse.equinox.p2.query.IQuery;
import org.eclipse.equinox.p2.query.IQueryResult;
import org.eclipse.equinox.p2.query.IQueryable;

public class ForeignProfiles implements IQueryable {

	private Map<String, ForeignProfile> profileMap = new HashMap<String, ForeignProfile>();

	public void addProfile(ForeignProfile profile) {
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

	public ForeignProfile getProfile(ForeignProfileElement element) {
		return (ForeignProfile) profileMap.get(element.getProfileId());
	}

	public ForeignProfile getProfile(String profileid) {
		return (ForeignProfile) profileMap.get(profileid);
	}

	public IQueryResult query(IQuery query, IProgressMonitor monitor) {
		List profiles = new ArrayList(profileMap.size());
		Iterator keys = profileMap.keySet().iterator();

		while (keys.hasNext())
			profiles.add(profileMap.get(keys.next()));

		return query.perform(profiles.iterator());
	}

	private void broadcastEvent(String profileId, int reason) {
		IProvisioningAgent agent = (IProvisioningAgent) ServiceHelper.getService(AnalysisActivator.getDefault().getContext(), IProvisioningAgent.SERVICE_NAME);
		IProvisioningEventBus eventBus = (IProvisioningEventBus) agent.getService(IProvisioningEventBus.SERVICE_NAME);
		if (eventBus != null)
			eventBus.publishEvent(new ProfileEvent(profileId, reason));
	}
}
