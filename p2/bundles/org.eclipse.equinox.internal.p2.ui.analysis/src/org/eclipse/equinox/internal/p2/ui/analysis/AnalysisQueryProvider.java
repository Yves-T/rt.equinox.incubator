package org.eclipse.equinox.internal.p2.ui.analysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.eclipse.equinox.internal.p2.ui.ElementQueryDescriptor;
import org.eclipse.equinox.internal.p2.ui.ProvUI;
import org.eclipse.equinox.internal.p2.ui.QueryProvider;
import org.eclipse.equinox.internal.p2.ui.analysis.model.ForeignProfile;
import org.eclipse.equinox.internal.p2.ui.analysis.model.ForeignProfileElement;
import org.eclipse.equinox.internal.p2.ui.analysis.query.ForeignProfileElementWrapper;
import org.eclipse.equinox.internal.p2.ui.model.IIUElement;
import org.eclipse.equinox.internal.p2.ui.model.QueriedElement;
import org.eclipse.equinox.internal.p2.ui.query.IUViewQueryContext;
import org.eclipse.equinox.internal.p2.ui.query.InstalledIUElementWrapper;
import org.eclipse.equinox.p2.engine.IProfile;
import org.eclipse.equinox.p2.engine.query.IUProfilePropertyQuery;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.metadata.IRequirement;
import org.eclipse.equinox.p2.query.Collector;
import org.eclipse.equinox.p2.query.IQuery;
import org.eclipse.equinox.p2.query.IQueryable;
import org.eclipse.equinox.p2.query.MatchQuery;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.eclipse.equinox.p2.ui.Policy;
import org.eclipse.equinox.p2.ui.ProvisioningUI;

public class AnalysisQueryProvider extends QueryProvider {
	public static final int IU_ARTIFACTS = 201;
	private Policy policy;
	private ProvisioningUI ui;

	public AnalysisQueryProvider(Policy policy) {
		super(ProvisioningUI.getDefaultUI());
		this.policy = policy;
	}

	public ElementQueryDescriptor getQueryDescriptor(QueriedElement element) {
		IQueryable<?> queryable = element.getQueryable();
		int queryType = element.getQueryType();
		IUViewQueryContext context = element.getQueryContext();
		if (context == null) {
			context = ProvUI.getQueryContext(policy);
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
					Collection<IRequirement> requirements = ((IIUElement) element).getRequirements();

					List<IQuery<IInstallableUnit>> queries = new ArrayList<IQuery<IInstallableUnit>>();

					Iterator<IRequirement> iter = requirements.iterator();

					while (iter.hasNext()) {
						queries.add(QueryUtil.createMatchQuery(iter.next().getMatches(), new Object[] {}));
					}

					return new ElementQueryDescriptor(queryable, QueryUtil.createCompoundQuery(queries, false), new Collector(), new InstalledIUElementWrapper(queryable, element));
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
