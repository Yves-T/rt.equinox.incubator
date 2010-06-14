package org.eclipse.equinox.internal.p2.ui.analysis.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.equinox.internal.p2.director.Slicer;
import org.eclipse.equinox.internal.p2.ui.ProvUIImages;
import org.eclipse.equinox.internal.p2.ui.analysis.AnalysisHelper;
import org.eclipse.equinox.internal.p2.ui.analysis.query.MissingRequirementQuery;
import org.eclipse.equinox.internal.p2.ui.model.IIUElement;
import org.eclipse.equinox.internal.p2.ui.model.QueriedElement;
import org.eclipse.equinox.p2.engine.IProfile;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.metadata.IRequirement;
import org.eclipse.equinox.p2.query.IQueryResult;
import org.eclipse.equinox.p2.query.IQueryable;

public class IUElement extends QueriedElement implements IIUElement {
	private IInstallableUnit iu;
	private IQueryable<IInstallableUnit> queryable;
	private IProfile profile;
	private Map<String, String> properties;
	private boolean artifactChildren, iuChildren;
	private IStatus mark;

	public IUElement(Object parent, IQueryable<IInstallableUnit> queryable, IProfile profile, IInstallableUnit iu, boolean artifactChildren, boolean iuChildren) {
		super(parent);
		this.iu = iu;
		this.artifactChildren = artifactChildren;
		this.iuChildren = iuChildren;
		this.queryable = queryable;
		this.profile = profile;
		this.properties = profile == null ? Collections.EMPTY_MAP : profile.getProperties();
	}

	public IUElement(Object parent, IQueryable<IInstallableUnit> queryable, IProfile profile, IInstallableUnit iu) {
		this(parent, queryable, profile, iu, false, true);
	}

	public Object[] getChildren(Object o) {
		Object[] children = new Object[0];
		Object[] keys = new Object[0];
		Collection<IRequirement> reqs = null;
		Collection<IUElement> ius = null;

		if (iuChildren) {
			ius = getIUChildren();
		}
		reqs = getRequirementChildren();
		if (artifactChildren)
			keys = ((IInstallableUnit) iu).getArtifacts().toArray();

		children = new Object[(ius != null ? ius.size() : 0) + reqs.size() + keys.length];
		int i = 0;

		if (ius != null) {
			Iterator<IUElement> iter = ius.iterator();
			while (iter.hasNext())
				children[i++] = iter.next();
		}
		Iterator<IRequirement> iter = reqs.iterator();
		while (iter.hasNext())
			children[i++] = new RequirementElement(this, iter.next());

		if (i != 0)
			for (int x = i; x < children.length; x++)
				children[x] = keys[x - i];
		else
			children = keys;

		Arrays.sort(children, new Comparator<Object>() {
			public int compare(Object arg0, Object arg1) {
				if (arg0 != null && arg1 != null)
					return arg0.toString().compareTo(arg1.toString());
				return 0;
			}
		});

		return children;
	}

	private Collection<IUElement> getIUChildren() {
		Collection<IUElement> ius = null;

		if (iuChildren) {
			if (iu.getRequirements().isEmpty())
				return ius;
			IQueryResult<IInstallableUnit> ius2 = queryable.query(AnalysisHelper.createQuery(iu.getRequirements()), new NullProgressMonitor());

			ius = new ArrayList<IUElement>();

			Iterator<IInstallableUnit> iter = ius2.iterator();
			while (iter.hasNext())
				ius.add(new IUElement(this, queryable, profile, (IInstallableUnit) iter.next(), artifactChildren, iuChildren));
		}
		return ius;
	}

	private Collection<IRequirement> getRequirementChildren() {
		List<IRequirement> children = new ArrayList<IRequirement>();
		if (isMarked()) {
			// find missing requirements
			MissingRequirementQuery query = new MissingRequirementQuery(iu.getRequirements(), properties, false);
			queryable.query(query, new NullProgressMonitor());
			IRequirement[] req = query.getMissing();

			for (int i = 0; i < req.length; i++) {
				// TODO: What was the filter doing?
				//	try {
				//		if (req[i].getFilter() == null || AnalysisActivator.getDefault().getContext().createFilter(req[i].getFilter()).match(properties))
				children.add(req[i]);
				//	} catch (InvalidSyntaxException e) {
				//	}
			}
		}
		return children;
	}

	public String getLabel(Object o) {
		return iu.getId(); //$NON-NLS-1$
	}

	public void setChildren(boolean artifact, boolean iu) {
		this.artifactChildren = artifact;
		this.iuChildren = iu;
	}

	public void computeSize(IProgressMonitor monitor) {
	}

	public IInstallableUnit getIU() {
		return iu;
	}

	public Collection<IRequirement> getRequirements() {
		return iu.getRequirements();
	}

	public long getSize() {
		return 0;
	}

	public boolean shouldShowChildren() {
		return artifactChildren && iuChildren;
	}

	public boolean shouldShowSize() {
		return false;
	}

	public boolean shouldShowVersion() {
		return true;
	}

	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		if (adapter == IInstallableUnit.class)
			return iu;
		return super.getAdapter(adapter);
	}

	public IProfile getProfile() {
		return profile;
	}

	public boolean isMarked() {
		if (mark == null) {
			Slicer slicer = new Slicer(queryable, properties, true);
			slicer.slice(new IInstallableUnit[] {iu}, new NullProgressMonitor());

			mark = slicer.getStatus();
		}
		return !mark.isOK();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.equinox.internal.provisional.p2.ui.model.ProvElement#getImageID(java.lang.Object)
	 */
	protected String getImageId(Object obj) {
		return ProvUIImages.IMG_IU;
	}

	public String toString() {
		return iu.toString();
	}

	@Override
	protected int getDefaultQueryType() {
		return 0;
	}
}
