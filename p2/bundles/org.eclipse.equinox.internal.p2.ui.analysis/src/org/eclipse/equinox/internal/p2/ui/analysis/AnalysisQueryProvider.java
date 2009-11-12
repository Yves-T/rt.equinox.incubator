package org.eclipse.equinox.internal.p2.ui.analysis;

import org.eclipse.equinox.internal.p2.ui.ElementQueryDescriptor;
import org.eclipse.equinox.internal.p2.ui.ProvUI;
import org.eclipse.equinox.internal.p2.ui.QueryProvider;
import org.eclipse.equinox.internal.p2.ui.analysis.model.ForeignProfile;
import org.eclipse.equinox.internal.p2.ui.analysis.model.ForeignProfileElement;
import org.eclipse.equinox.internal.p2.ui.analysis.query.ForeignProfileElementWrapper;
import org.eclipse.equinox.internal.p2.ui.model.IIUElement;
import org.eclipse.equinox.internal.p2.ui.model.QueriedElement;
import org.eclipse.equinox.internal.p2.ui.query.AnyRequiredCapabilityQuery;
import org.eclipse.equinox.internal.p2.ui.query.InstalledIUElementWrapper;
import org.eclipse.equinox.internal.provisional.p2.engine.IProfile;
import org.eclipse.equinox.internal.provisional.p2.engine.IUProfilePropertyQuery;
import org.eclipse.equinox.internal.provisional.p2.metadata.query.Collector;
import org.eclipse.equinox.internal.provisional.p2.metadata.query.IQueryable;
import org.eclipse.equinox.internal.provisional.p2.metadata.query.MatchQuery;
import org.eclipse.equinox.p2.ui.IUViewQueryContext;
import org.eclipse.equinox.p2.ui.Policy;

public class AnalysisQueryProvider extends QueryProvider {
	public static final int IU_ARTIFACTS = 201;
	private Policy policy;

	public AnalysisQueryProvider(Policy policy) {
		this.policy = policy;
	}

	public ElementQueryDescriptor getQueryDescriptor(QueriedElement element) {
		IQueryable queryable = element.getQueryable();
		int queryType = element.getQueryType();
		IUViewQueryContext context = element.getQueryContext();
		if (context == null) {
			context = policy.getQueryContext();
		}
		switch (queryType) {
			case QueryProvider.PROFILES :
				queryable = AnalysisActivator.getDefault().getKnownProfiles();
				return new ElementQueryDescriptor(queryable, new MatchQuery() {
					public boolean isMatch(Object candidate) {
						return ProvUI.getAdapter(candidate, ForeignProfile.class) != null;
					}
				}, new Collector(), new ForeignProfileElementWrapper(null, element));
			case QueryProvider.INSTALLED_IUS :
				ForeignProfile profile = null;
				// Querying of IU's.  We are drilling down into the requirements.
				if (element instanceof IIUElement && context.getShowInstallChildren()) {
					return new ElementQueryDescriptor(queryable, new AnyRequiredCapabilityQuery(((IIUElement) element).getRequirements()), new Collector(), new InstalledIUElementWrapper(queryable, element));
				} else if (element instanceof ForeignProfileElement) {
					profile = AnalysisActivator.getDefault().getKnownProfiles().getProfile(((ForeignProfileElement) element));
				}

				if (profile == null)
					return null;
				return new ElementQueryDescriptor(profile, new IUProfilePropertyQuery(IProfile.PROP_PROFILE_ROOT_IU, Boolean.TRUE.toString()), new Collector(), new InstalledIUElementWrapper(profile, element));
		}
		return null;
	}
}
