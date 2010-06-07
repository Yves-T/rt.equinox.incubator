package org.eclipse.equinox.internal.p2.ui.analysis.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.metadata.IRequirement;
import org.eclipse.equinox.p2.query.MatchQuery;

public class MissingRequirementQuery extends MatchQuery<IInstallableUnit> {

	private Map<IRequirement, Boolean> requirements = new HashMap<IRequirement, Boolean>();
	private Map<String, String> properties;
	private boolean expand;

	public MissingRequirementQuery(Collection<IRequirement> reqs, Map<String, String> properties, boolean expandRequirements) {
		add(reqs);
		this.properties = properties;
		expand = expandRequirements;
	}

	public IRequirement[] getMissing() {
		List<IRequirement> list = new ArrayList<IRequirement>();
		Iterator<IRequirement> iter = requirements.keySet().iterator();
		while (iter.hasNext()) {
			IRequirement key = iter.next();
			if (!((Boolean) requirements.get(key)).booleanValue())
				list.add(key);
		}
		return list.toArray(new IRequirement[list.size()]);
	}

	private void add(Collection<IRequirement> requirements) {
		for (IRequirement req : requirements) {
			// TODO What was the filter doing?
			//			try {
			//				if (this.requirements.get(req) == null && (req.getFilter() == null || AnalysisActivator.getDefault().getContext().createFilter(req.getFilter()).match(properties)))
			this.requirements.put(req, Boolean.FALSE);
			//			} catch (InvalidSyntaxException e) {
			//				//TODO add more
			//			}
		}
	}

	public boolean isMatch(IInstallableUnit candidate) {
		boolean match = false;
		Iterator<IRequirement> iter = requirements.keySet().iterator();
		while (iter.hasNext()) {
			IRequirement req = iter.next();
			if (candidate.satisfies(req)) {
				requirements.put(req, Boolean.TRUE);
				match = true;
			}
		}
		if (expand && match) {
			add(candidate.getRequirements());
		}
		return match;
	}
}
