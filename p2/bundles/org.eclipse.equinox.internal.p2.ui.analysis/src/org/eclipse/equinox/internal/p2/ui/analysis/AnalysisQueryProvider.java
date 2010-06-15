package org.eclipse.equinox.internal.p2.ui.analysis;

import org.eclipse.equinox.internal.p2.ui.ElementQueryDescriptor;
import org.eclipse.equinox.internal.p2.ui.ProvUI;
import org.eclipse.equinox.internal.p2.ui.QueryProvider;
import org.eclipse.equinox.internal.p2.ui.analysis.query.IUElementWrapper;
import org.eclipse.equinox.internal.p2.ui.analysis.query.IUQueryableElementWrapper;
import org.eclipse.equinox.internal.p2.ui.model.CategoryElement;
import org.eclipse.equinox.internal.p2.ui.model.IIUElement;
import org.eclipse.equinox.internal.p2.ui.model.MetadataRepositoryElement;
import org.eclipse.equinox.internal.p2.ui.model.ProfileElement;
import org.eclipse.equinox.internal.p2.ui.model.QueriedElement;
import org.eclipse.equinox.internal.p2.ui.query.CategoryElementWrapper;
import org.eclipse.equinox.internal.p2.ui.query.IUViewQueryContext;
import org.eclipse.equinox.p2.engine.IProfile;
import org.eclipse.equinox.p2.engine.query.IUProfilePropertyQuery;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.metadata.expression.ExpressionUtil;
import org.eclipse.equinox.p2.metadata.expression.IExpression;
import org.eclipse.equinox.p2.query.Collector;
import org.eclipse.equinox.p2.query.IQuery;
import org.eclipse.equinox.p2.query.IQueryable;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.eclipse.equinox.p2.ui.Policy;
import org.eclipse.equinox.p2.ui.ProvisioningUI;

public class AnalysisQueryProvider extends QueryProvider {
	public static final int IU_ARTIFACTS = 201;
	public static final int IU_QUERYABLES = 128;
	public static final int IU_REQUIREMENTS = 256;
	private Policy policy;

	public AnalysisQueryProvider(Policy policy) {
		super(ProvisioningUI.getDefaultUI());
		this.policy = policy;
	}

	public ElementQueryDescriptor getQueryDescriptor(QueriedElement element) {
		int queryType = element.getQueryType();
		IUViewQueryContext context = element.getQueryContext();
		if (context == null) {
			context = ProvUI.getQueryContext(policy);
		}
		IQueryable<?> queryable = element.getQueryable();

		switch (element.getQueryType()) {
			case AnalysisQueryProvider.IU_QUERYABLES :
				return new ElementQueryDescriptor(queryable, QueryUtil.createMatchQuery(IQueryable.class, ExpressionUtil.TRUE_EXPRESSION), new Collector(), new IUQueryableElementWrapper(null, element));
			case QueryProvider.PROFILES :
				return new ElementQueryDescriptor(AnalysisActivator.getDefault().getKnownProfiles(), QueryUtil.createMatchQuery(IProfile.class, ExpressionUtil.TRUE_EXPRESSION), new Collector<IInstallableUnit>(), new IUQueryableElementWrapper(null, element));
			case QueryProvider.INSTALLED_IUS :
				// Querying of IU's.  We are drilling down into the requirements.
				if (element instanceof IIUElement && context.getShowInstallChildren()) {
					if (((IIUElement) element).getRequirements().isEmpty())
						return null;
					return new ElementQueryDescriptor(queryable, AnalysisHelper.createQuery(((IIUElement) element).getRequirements()), new Collector<Object>(), new IUElementWrapper((IQueryable<IInstallableUnit>) queryable, element));
				} else if (element instanceof ProfileElement) {
					IProfile profile = AnalysisActivator.getDefault().getKnownProfiles().getProfile(((ProfileElement) element).getProfileId());
					if (profile == null)
						break;
					return new ElementQueryDescriptor(profile, new IUProfilePropertyQuery(IProfile.PROP_PROFILE_ROOT_IU, Boolean.TRUE.toString()), new Collector<IInstallableUnit>(), new IUElementWrapper(profile, element));
				}
			case QueryProvider.AVAILABLE_IUS :
				if (element instanceof MetadataRepositoryElement) {
					return new ElementQueryDescriptor(queryable, QueryUtil.createIUCategoryQuery(), new Collector<Object>(), new CategoryElementWrapper(queryable, element));
				} else if (element instanceof CategoryElement) {
					IExpression matchesRequirementsExpression = ExpressionUtil.parse("$0.exists(r | this ~= r)"); //$NON-NLS-1$
					IQuery<IInstallableUnit> memberOfCategoryQuery = QueryUtil.createMatchQuery(matchesRequirementsExpression, ((CategoryElement) element).getRequirements());
					return new ElementQueryDescriptor(queryable, memberOfCategoryQuery, new Collector<IInstallableUnit>(), new IUElementWrapper((IQueryable<IInstallableUnit>) queryable, element));
				}
			case IU_REQUIREMENTS :
				//				if (element instanceof IUProperties) {
				//					return new ElementQueryDescriptor(queryable, null, new Collector<Object>(), null);
				//				}
		}
		return super.getQueryDescriptor(element);
	}
}
