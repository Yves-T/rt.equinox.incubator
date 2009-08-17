package org.eclipse.equinox.internal.p2.ui.analysis.query;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.eclipse.equinox.internal.p2.ui.analysis.AnalysisActivator;
import org.eclipse.equinox.internal.provisional.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.internal.provisional.p2.metadata.IRequiredCapability;
import org.eclipse.equinox.internal.provisional.p2.query.MatchQuery;
import org.osgi.framework.InvalidSyntaxException;

public class MissingRequirementQuery extends MatchQuery {

	private Map requirements = new HashMap();
	private Dictionary properties;
	private boolean expand;

	public MissingRequirementQuery(IRequiredCapability[] reqs, Dictionary properties, boolean expandRequirements) {
		add(reqs);
		this.properties = properties;
		expand = expandRequirements;
	}

	public boolean isMatch(Object object) {
		if (!(object instanceof IInstallableUnit))
			return false;
		IInstallableUnit candidate = (IInstallableUnit) object;

		boolean match = false;
		Iterator iter = requirements.keySet().iterator();
		while (iter.hasNext()) {
			IRequiredCapability req = (IRequiredCapability) iter.next();
			if (candidate.satisfies(req)) {
				requirements.put(req, Boolean.TRUE);
				match = true;
			}
		}
		if (expand && match) {
			add(candidate.getRequiredCapabilities());
		}
		return match;
	}

	public IRequiredCapability[] getMissing() {
		ArrayList list = new ArrayList();
		Iterator iter = requirements.keySet().iterator();
		while (iter.hasNext()) {
			Object key = iter.next();
			if (!((Boolean) requirements.get(key)).booleanValue())
				list.add(key);
		}
		return (IRequiredCapability[]) list.toArray(new IRequiredCapability[list.size()]);
	}

	private void add(IRequiredCapability[] req) {
		for (int i = 0; i < req.length; i++) {
			try {
				if (requirements.get(req[i]) == null && (req[i].getFilter() == null || AnalysisActivator.getDefault().getContext().createFilter(req[i].getFilter()).match(properties)))
					requirements.put(req[i], Boolean.FALSE);
			} catch (InvalidSyntaxException e) {
				//TODO add more
			}
		}
	}

	private void add(IInstallableUnit iu) {

	}
}
