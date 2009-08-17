package org.eclipse.equinox.internal.p2.ui.analysis.model;

import org.eclipse.equinox.internal.p2.ui.model.ProvElement;
import org.eclipse.equinox.internal.provisional.p2.metadata.IRequiredCapability;

public class RequirementElement extends ProvElement {
	private IRequiredCapability capability;

	public RequirementElement(Object parent, IRequiredCapability capability) {
		super(parent);
		this.capability = capability;
	}

	public Object[] getChildren(Object o) {
		return new Object[0];
	}

	public String getLabel(Object o) {
		return capability.toString();
	}

	public String toString() {
		return capability.toString();
	}
}
