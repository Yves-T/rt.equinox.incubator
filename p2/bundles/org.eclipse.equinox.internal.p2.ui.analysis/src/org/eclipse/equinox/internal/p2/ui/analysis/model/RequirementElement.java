package org.eclipse.equinox.internal.p2.ui.analysis.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.equinox.internal.p2.director.SimplePlanner;
import org.eclipse.equinox.internal.p2.metadata.IRequiredCapability;
import org.eclipse.equinox.internal.p2.ui.ProvUIImages;
import org.eclipse.equinox.internal.p2.ui.analysis.IUPropertiesView.IUProperties;
import org.eclipse.equinox.internal.p2.ui.model.ProvElement;
import org.eclipse.equinox.p2.engine.IProfile;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.metadata.IRequirement;
import org.eclipse.equinox.p2.query.IQueryable;
import org.eclipse.equinox.p2.query.MatchQuery;

public class RequirementElement extends ProvElement {
	private IRequirement capability;

	public RequirementElement(Object parent, IRequirement capability) {
		super(parent);
		this.capability = capability;
	}

	public String getVersion() {
		if (capability instanceof IRequiredCapability) {
			return ((IRequiredCapability) capability).getRange().toString();
		}
		return "";
	}

	public Object[] getChildren(Object o) {
		List<Object> children = new ArrayList<Object>();
		if (capability instanceof IRequiredCapability) {
			IRequiredCapability c = (IRequiredCapability) capability;
			children.add(new PropertyPairElement("Greedy", Boolean.toString(c.isGreedy())));
			children.add(new PropertyPairElement("Min", String.valueOf(c.getMin())));
			children.add(new PropertyPairElement("Max", String.valueOf(c.getMax())));
		}
		if (getParent(o) instanceof IUProperties) {
			@SuppressWarnings("unchecked")
			IQueryable<IInstallableUnit> queryable = (IQueryable<IInstallableUnit>) ((IUProperties) getParent(o)).getElement().getQueryable();
			Iterator<IInstallableUnit> iter = queryable.query(new OwnsRequirementQuery(capability), new NullProgressMonitor()).iterator();
			while (iter.hasNext())
				children.add(new IUElement(this, queryable, queryable instanceof IProfile ? SimplePlanner.createSelectionContext(((IProfile) queryable).getProperties()) : Collections.EMPTY_MAP, iter.next(), false, false));
		}
		return children.toArray();
	}

	public String getLabel(Object o) {
		if (capability instanceof IRequiredCapability)
			return ((IRequiredCapability) capability).getName();
		return capability.toString();
	}

	public String toString() {
		return capability.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.equinox.internal.provisional.p2.ui.model.ProvElement#getImageID(java.lang.Object)
	 */
	protected String getImageId(Object obj) {
		return ProvUIImages.IMG_DISABLED_IU;
	}

	private static class OwnsRequirementQuery extends MatchQuery<IInstallableUnit> {
		private IRequirement requirement;

		public OwnsRequirementQuery(IRequirement req) {
			requirement = req;
		}

		@Override
		public boolean isMatch(IInstallableUnit candidate) {
			for (IRequirement req : candidate.getRequirements())
				if (req.equals(requirement))
					return true;
			return false;
		}
	}

	public static class PropertyPairElement {
		String a, b;

		private PropertyPairElement(String a, String b) {
			this.a = a;
			this.b = b;
		}

		public String getProperty() {
			return a;
		}

		public String getValue() {
			return b;
		}
	}
}
