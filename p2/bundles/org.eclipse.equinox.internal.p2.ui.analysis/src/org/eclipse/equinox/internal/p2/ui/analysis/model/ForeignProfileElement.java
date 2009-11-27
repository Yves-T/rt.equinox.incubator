package org.eclipse.equinox.internal.p2.ui.analysis.model;


import org.eclipse.equinox.internal.p2.ui.ProvUIImages;
import org.eclipse.equinox.internal.p2.ui.QueryProvider;
import org.eclipse.equinox.internal.p2.ui.model.RemoteQueriedElement;
import org.eclipse.equinox.internal.provisional.p2.engine.IProfile;
import org.eclipse.equinox.internal.provisional.p2.metadata.query.IQueryable;

/*
 * Based on org.eclipse.equinox.internal.provisional.p2.ui.model.ProfileElement
 */
public class ForeignProfileElement extends RemoteQueriedElement {
	private ForeignProfile profile;

	public ForeignProfileElement(Object parent, ForeignProfile profile) {
		super(parent);
		this.profile = profile;
	}

	public Object getAdapter(Class adapter) {
		if (adapter == IProfile.class)
			return getQueryable();
		return super.getAdapter(adapter);
	}

	protected String getImageId(Object obj) {
		return ProvUIImages.IMG_PROFILE;
	}

	protected int getDefaultQueryType() {
		return QueryProvider.INSTALLED_IUS;
	}

	public IQueryable getQueryable() {
		return profile;
	}

	public IProfile getProfile() {
		return profile;
	}

	public String getLabel(Object o) {
		return profile.getProfileId();
	}

	public String getProfileId() {
		return profile.getProfileId();
	}

	/*
	 * Overridden to check the children so that profiles
	 * showing in profile views accurately reflect if they
	 * are empty.  We do not cache the children because often
	 * this element is the input of a view and when the view
	 * is refreshed we want to refetch the children.
	 * 
	 * (non-Javadoc)
	 * @see org.eclipse.equinox.internal.p2.ui.model.RemoteQueriedElement#isContainer()
	 */
	public boolean isContainer() {
		return super.getChildren(this).length > 0;
	}
}
