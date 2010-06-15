package org.eclipse.equinox.internal.p2.ui.analysis.model;

import org.eclipse.equinox.internal.p2.ui.analysis.AnalysisActivator;
import org.eclipse.equinox.internal.p2.ui.model.ProfileElement;
import org.eclipse.equinox.p2.query.IQueryable;

public class ForeignProfileElement extends ProfileElement {

	public ForeignProfileElement(Object parent, String profileId) {
		super(parent, profileId);
	}

	public IQueryable<?> getQueryable() {
		return AnalysisActivator.getDefault().getKnownProfiles().getProfile(getProfileId());
	}
}
