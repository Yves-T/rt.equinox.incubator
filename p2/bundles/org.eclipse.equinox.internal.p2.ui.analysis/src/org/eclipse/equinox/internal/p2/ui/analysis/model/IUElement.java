package org.eclipse.equinox.internal.p2.ui.analysis.model;

import org.eclipse.equinox.p2.engine.IProfile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.Iterator;
import java.util.List;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.equinox.internal.p2.director.Slicer;
import org.eclipse.equinox.internal.p2.ui.analysis.AnalysisActivator;
import org.eclipse.equinox.internal.p2.ui.analysis.query.MissingRequirementQuery;
import org.eclipse.equinox.internal.p2.ui.model.IIUElement;
import org.eclipse.equinox.internal.p2.ui.model.ProvElement;
import org.eclipse.equinox.internal.provisional.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.internal.provisional.p2.metadata.IRequiredCapability;
import org.eclipse.equinox.internal.provisional.p2.metadata.query.Collector;
import org.eclipse.equinox.internal.provisional.p2.metadata.query.IQueryable;
import org.osgi.framework.InvalidSyntaxException;

public class IUElement extends ProvElement implements IIUElement {
	private IInstallableUnit iu;
	private IQueryable queryable;
	private IProfile profile;
	private Dictionary properties;
	private boolean artifactChildren, iuChildren;
	private IStatus mark;

	public IUElement(Object parent, IQueryable queryable, IProfile profile, Dictionary properties, IInstallableUnit iu, boolean artifactChildren, boolean iuChildren) {
		super(parent);
		this.iu = iu;
		this.artifactChildren = artifactChildren;
		this.iuChildren = iuChildren;
		this.queryable = queryable;
		this.properties = properties;
		this.profile = profile;
	}

	public Object[] getChildren(Object o) {
		Object[] children = new Object[0];
		Object[] keys = new Object[0];
		Collection reqs = null;
		Collection ius = null;

		if (iuChildren) {
			ius = getIUChildren();
		}
		reqs = getRequirementChildren(ius != null ? ius : new ArrayList());
		if (artifactChildren)
			keys = iu.getArtifacts();

		children = new Object[(ius != null ? ius.size() : 0) + reqs.size() + keys.length];
		int i = 0;

		if (ius != null) {
			Iterator iter = ius.iterator();
			while (iter.hasNext())
				children[i++] = iter.next();
		}
		Iterator iter = reqs.iterator();
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

	public Collection getIUChildren() {
		List ius = null;

		if (iuChildren) {
			Collector ius2 = queryable.query(new AnyRequiredCapabilityQuery(iu.getRequiredCapabilities()), new Collector(), new NullProgressMonitor());
			ius = new ArrayList(ius2.size());

			Iterator iter = ius2.iterator();
			while (iter.hasNext())
				ius.add(new IUElement(this, queryable, profile, properties, (IInstallableUnit) iter.next(), artifactChildren, iuChildren));
		}
		return ius;
	}

	public Collection getRequirementChildren(Collection iuChildren) {
		ArrayList children = new ArrayList();
		if (isMarked()) {
			// find missing requirements
			MissingRequirementQuery query = new MissingRequirementQuery(iu.getRequiredCapabilities(), properties, false);
			queryable.query(query, new Collector(), new NullProgressMonitor());
			IRequiredCapability[] req = query.getMissing();

			children.ensureCapacity(req.length);
			for (int i = 0; i < req.length; i++) {
				try {
					if (req[i].getFilter() == null || AnalysisActivator.getDefault().getContext().createFilter(req[i].getFilter()).match(properties))
						children.add(req[i]);
				} catch (InvalidSyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
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

	public IRequiredCapability[] getRequirements() {
		return iu.getRequiredCapabilities();
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
