package org.eclipse.equinox.internal.p2.ui.analysis;

import org.eclipse.equinox.internal.p2.core.helpers.ServiceHelper;
import org.eclipse.equinox.internal.p2.ui.ProvUI;
import org.eclipse.equinox.internal.p2.ui.analysis.model.ForeignProfiles;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.ui.Policy;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class AnalysisActivator extends AbstractUIPlugin {
	private static BundleContext context;
	private static AnalysisActivator plugin;
	public static final String PLUGIN_ID = "org.eclipse.equinox.p2.ui.stuff"; //$NON-NLS-1$

	private Policy policy;
	private IProvisioningAgent agent;
	private ForeignProfiles knownProfiles;
	private AnalysisQueryProvider provider;

	public static AnalysisActivator getDefault() {
		return plugin;
	}

	public Policy getPolicy() {
		if (policy == null)
			initializePolicy();
		return policy;
	}

	public IProvisioningAgent getAgent() {
		return agent;
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
		knownProfiles = new ForeignProfiles();
		agent = (IProvisioningAgent) ServiceHelper.getService(context, IProvisioningAgent.SERVICE_NAME);

		initializePolicy();
	}

	void initializePolicy() {
		policy = new Policy();
		provider = new AnalysisQueryProvider(policy);
		ProvUI.setQueryProvider(provider);

		// TODO find equivalent
		//IUViewQueryContext queryContext = new IUViewQueryContext(IUViewQueryContext.AVAILABLE_VIEW_BY_REPO);
		//policy.setQueryContext(queryContext);
	}

	public void stop(BundleContext bundleContext) throws Exception {
		plugin = null;
		super.stop(bundleContext);
		policy = null;
		provider = null;
		agent = null;
	}

	public ForeignProfiles getKnownProfiles() {
		return knownProfiles;
	}

	public BundleContext getContext() {
		return context;
	}

	public void setQueryContext() {
		ProvUI.setQueryProvider(provider);
	}
}
