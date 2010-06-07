package org.eclipse.equinox.internal.p2.ui.analysis.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.equinox.internal.p2.director.Slicer;
import org.eclipse.equinox.internal.p2.metadata.IRequiredCapability;
import org.eclipse.equinox.internal.p2.metadata.InstallableUnit;
import org.eclipse.equinox.internal.p2.ui.analysis.query.MissingRequirementQuery;
import org.eclipse.equinox.internal.p2.ui.model.IIUElement;
import org.eclipse.equinox.internal.p2.ui.model.ProvElement;
import org.eclipse.equinox.p2.engine.IProfile;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.metadata.IRequirement;
import org.eclipse.equinox.p2.query.IQuery;
import org.eclipse.equinox.p2.query.IQueryResult;
import org.eclipse.equinox.p2.query.IQueryable;
import org.eclipse.equinox.p2.query.QueryUtil;

public class IUElement extends ProvElement implements IIUElement {
	private IInstallableUnit iu;
	private IQueryable<IInstallableUnit> queryable;
	private IProfile profile;
	private Map<String, String> properties;
	private boolean artifactChildren, iuChildren;
	private IStatus mark;

	public IUElement(Object parent, IQueryable<IInstallableUnit> queryable, IProfile profile, Map<String, String> properties, IInstallableUnit iu, boolean artifactChildren, boolean iuChildren) {
		super(parent);
		this.iu = iu;
		this.artifactChildren = artifactChildren;
		this.iuChildren = iuChildren;
		this.queryable = queryable;
		this.properties = profile.getProperties();
		this.profile = profile;
	}

	public Object[] getChildren(Object o) {
		Object[] children = new Object[0];
		Object[] keys = new Object[0];
		Collection<IRequirement> reqs = null;
		Collection<IUElement> ius = null;

		if (iuChildren) {
			ius = getIUChildren();
		}
		reqs = getRequirementChildren(ius != null ? ius : new ArrayList());
		if (artifactChildren)
			keys = ((InstallableUnit) iu).getArtifacts().toArray();

		children = new Object[(ius != null ? ius.size() : 0) + reqs.size() + keys.length];
		int i = 0;

		if (ius != null) {
			Iterator<IUElement> iter = ius.iterator();
			while (iter.hasNext())
				children[i++] = iter.next();
		}
		Iterator<IRequirement> iter = reqs.iterator();
		while (iter.hasNext())
			children[i++] = new RequirementElement(this, (IRequiredCapability) iter.next());

		if (i != 0)
			for (int x = i; x < children.length; x++)
				children[x] = keys[x - i];
		else
			children = keys;

		Arrays.sort(children, new Comparator() {
			public int compare(Object arg0, Object arg1) {
				if (arg0 != null && arg1 != null)
					return arg0.toString().compareTo(arg1.toString());
				return 0;
			}
		});

		return children;
	}

	public Collection<IUElement> getIUChildren() {
		List<IUElement> ius = null;

		if (iuChildren) {
			Collection<IRequirement> requirements = iu.getRequirements();
			List<IQuery<IInstallableUnit>> queries = new ArrayList<IQuery<IInstallableUnit>>();
			for (IRequirement req : requirements)
				queries.add(QueryUtil.createMatchQuery(req.getMatches(), new Object[0]));

			IQueryResult<IInstallableUnit> ius2 = queryable.query(QueryUtil.createCompoundQuery(queries, false), new NullProgressMonitor());

			ius = new ArrayList<IUElement>();

			Iterator<IInstallableUnit> iter = ius2.iterator();
			while (iter.hasNext())
				ius.add(new IUElement(this, queryable, profile, properties, (IInstallableUnit) iter.next(), artifactChildren, iuChildren));
		}
		return ius;
	}

	public Collection<IRequirement> getRequirementChildren(Collection iuChildren) {
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
		return iu.getId() + " " + iu.getVersion(); //$NON-NLS-1$
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

	public String toString() {
		return iu.toString();
	}
}
