package org.eclipse.equinox.internal.p2.ui.analysis.query;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;
import org.eclipse.equinox.internal.provisional.p2.metadata.query.MatchQuery;

public class MissingQuery extends MatchQuery {
	Map ius;

	public MissingQuery(Map ius) {
		this.ius = ius;
	}

	public Collection getMissingIUs() {
		Collection set = new TreeSet();
		Iterator keys = ius.keySet().iterator();
		while (keys.hasNext())
			set.add(keys.next());
		return set;
	}

	public boolean isMatch(Object candidate) {
		ius.remove(candidate);
		return false;
	}
}
