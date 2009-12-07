package org.eclipse.equinox.internal.p2.ui.analysis.model;

import org.eclipse.equinox.p2.engine.IProfile;
import org.eclipse.equinox.p2.engine.ProfileEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.equinox.internal.p2.core.helpers.ServiceHelper;
import org.eclipse.equinox.internal.p2.ui.analysis.AnalysisActivator;
import org.eclipse.equinox.internal.provisional.p2.core.eventbus.IProvisioningEventBus;
import org.eclipse.equinox.internal.provisional.p2.metadata.query.Collector;
import org.eclipse.equinox.internal.provisional.p2.metadata.query.IQueryable;
import org.eclipse.equinox.p2.metadata.query.IQuery;

public class ForeignProfiles implements IQueryable {

	private Map profileMap = new HashMap();

	public void addProfile(ForeignProfile profile) {
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

	public Collector query(IQuery query, Collector collector, IProgressMonitor monitor) {
		List profiles = new ArrayList(profileMap.size());
		Iterator keys = profileMap.keySet().iterator();

		while (keys.hasNext())
			profiles.add(profileMap.get(keys.next()));

		query.perform(profiles.iterator(), collector);
		return collector;
	}

	private void broadcastEvent(String profileId, byte reason) {
		((IProvisioningEventBus) ServiceHelper.getService(AnalysisActivator.getDefault().getContext(), IProvisioningEventBus.class.getName())).publishEvent(new ProfileEvent(profileId, reason));
	}
}
