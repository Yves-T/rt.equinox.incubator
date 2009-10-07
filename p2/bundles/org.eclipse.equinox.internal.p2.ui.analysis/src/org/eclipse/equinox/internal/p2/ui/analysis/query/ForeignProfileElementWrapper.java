package org.eclipse.equinox.internal.p2.ui.analysis.query;

import org.eclipse.equinox.internal.p2.ui.analysis.model.ForeignProfile;
import org.eclipse.equinox.internal.p2.ui.analysis.model.ForeignProfileElement;
import org.eclipse.equinox.internal.p2.ui.model.QueriedElementWrapper;

public class ForeignProfileElementWrapper extends QueriedElementWrapper {

	public ForeignProfileElementWrapper(ForeignProfile queryable, Object parent) {
		super(queryable, parent);
	}

	protected boolean shouldWrap(Object match) {
		if ((match instanceof ForeignProfile))
			return true;
		return false;
	}

	/**
	 * Transforms the item to a UI element
	 */
	protected Object wrap(Object item) {
		return super.wrap(new ForeignProfileElement(parent, (ForeignProfile) item));
	}
}
