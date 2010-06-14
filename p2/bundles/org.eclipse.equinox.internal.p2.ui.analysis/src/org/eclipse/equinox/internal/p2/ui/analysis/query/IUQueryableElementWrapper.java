package org.eclipse.equinox.internal.p2.ui.analysis.query;

import java.net.URI;
import org.eclipse.equinox.internal.p2.ui.analysis.AnalysisHelper;
import org.eclipse.equinox.internal.p2.ui.model.MetadataRepositoryElement;
import org.eclipse.equinox.internal.p2.ui.model.ProfileElement;
import org.eclipse.equinox.internal.p2.ui.model.QueriedElementWrapper;
import org.eclipse.equinox.p2.engine.IProfile;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.query.IQueryable;

public class IUQueryableElementWrapper extends QueriedElementWrapper {

	public IUQueryableElementWrapper(IQueryable<IInstallableUnit> queryable, Object parent) {
		super(queryable, parent);
	}

	protected boolean shouldWrap(Object match) {
		return match instanceof IQueryable<?> || match instanceof URI;
	}

	/**
	 * Transforms the item to a UI element
	 */
	protected Object wrap(Object item) {
		if (item instanceof IProfile)
			return super.wrap(new ProfileElement(parent, ((IProfile) item).getProfileId()));
		//return super.wrap(new ForeignProfileElement(parent, (IProfile) item));
		else if (item instanceof URI)
			return super.wrap(new MetadataRepositoryElement(parent, (URI) item, AnalysisHelper.getMetadataRepositoryManager().isEnabled((URI) item)));
		else
			return null;
	}
}
