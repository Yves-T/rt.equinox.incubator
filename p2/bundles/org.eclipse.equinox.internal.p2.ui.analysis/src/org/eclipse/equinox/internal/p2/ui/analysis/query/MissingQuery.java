package org.eclipse.equinox.internal.p2.ui.analysis.query;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.query.MatchQuery;

public class MissingQuery extends MatchQuery<IInstallableUnit> {
	Map<IInstallableUnit, Boolean> ius;

	public MissingQuery(Map<IInstallableUnit, Boolean> ius) {
		this.ius = ius;
	}

	public Collection<IInstallableUnit> getMissingIUs() {
		Collection<IInstallableUnit> set = new TreeSet<IInstallableUnit>();
		Iterator<IInstallableUnit> keys = ius.keySet().iterator();
		while (keys.hasNext())
			set.add(keys.next());
		return set;
	}

	public boolean isMatch(IInstallableUnit candidate) {
		ius.remove(candidate);
		return false;
	}
}
