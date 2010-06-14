package org.eclipse.equinox.internal.p2.ui.analysis.query;

import org.eclipse.equinox.internal.p2.ui.analysis.model.IUElement;
import org.eclipse.equinox.internal.p2.ui.model.QueriedElementWrapper;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.query.IQueryable;

public class IUElementWrapper extends QueriedElementWrapper {

	public IUElementWrapper(IQueryable<IInstallableUnit> queryable, Object parent) {
		super(queryable, parent);
	}

	protected boolean shouldWrap(Object o) {
		return o instanceof IInstallableUnit;
	}

	protected Object wrap(Object o) {
		if (o instanceof IInstallableUnit)
			return new IUElement(parent, (IQueryable<IInstallableUnit>) queryable, null, (IInstallableUnit) o);
		return super.wrap(o);
	}
}
