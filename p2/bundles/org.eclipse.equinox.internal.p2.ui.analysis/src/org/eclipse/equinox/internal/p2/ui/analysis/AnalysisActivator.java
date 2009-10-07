package org.eclipse.equinox.internal.p2.ui.analysis;

import org.eclipse.equinox.internal.p2.ui.analysis.model.ForeignProfiles;
import org.eclipse.equinox.internal.provisional.p2.ui.policy.IUViewQueryContext;
import org.eclipse.equinox.internal.provisional.p2.ui.policy.Policy;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class AnalysisActivator extends AbstractUIPlugin {
	private static BundleContext context;
	private static AnalysisActivator plugin;
	public static final String PLUGIN_ID = "org.eclipse.equinox.p2.ui.stuff"; //$NON-NLS-1$

	Policy policy;
	ForeignProfiles knownProfiles;

	public static AnalysisActivator getDefault() {
		return plugin;
	}

	public Policy getPolicy() {
		if (policy == null)
			initializePolicy();
		return policy;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		super.start(bundleContext);
		plugin = this;
		AnalysisActivator.context = bundleContext;
		initializePolicy();
		knownProfiles = new ForeignProfiles();
	}

	void initializePolicy() {
		policy = new Policy();
		policy.setQueryProvider(new AnalysisQueryProvider(policy));

		IUViewQueryContext queryContext = new IUViewQueryContext(IUViewQueryContext.AVAILABLE_VIEW_BY_REPO);
		policy.setQueryContext(queryContext);

	}

	public void stop(BundleContext bundleContext) throws Exception {
		plugin = null;
		super.stop(bundleContext);
		policy = null;
	}

	public ForeignProfiles getKnownProfiles() {
		return knownProfiles;
	}

	public BundleContext getContext() {
		return context;
	}
}
