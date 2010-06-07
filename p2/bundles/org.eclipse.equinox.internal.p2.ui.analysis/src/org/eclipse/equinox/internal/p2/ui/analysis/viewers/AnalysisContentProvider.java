package org.eclipse.equinox.internal.p2.ui.analysis.viewers;

import java.util.Collection;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.equinox.internal.p2.ui.viewers.ProvElementContentProvider;
import org.eclipse.jface.viewers.TreeViewer;

public class AnalysisContentProvider extends ProvElementContentProvider {
	TreeViewer viewer;

	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof TreeElement)
			return ((TreeElement) parentElement).getChildren();
		else if (parentElement instanceof Collection)
			return ((Collection) parentElement).toArray();
		else if (parentElement instanceof IStatus)
			return ((IStatus) parentElement).getChildren();
		return super.getChildren(parentElement);
	}

	public boolean hasChildren(Object element) {
		if (element instanceof TreeElement)
			return ((TreeElement) element).hasChildren();
		else if (element instanceof Collection)
			return !((Collection) element).isEmpty();
		else if (element instanceof IStatus)
			return ((IStatus) element).getChildren().length > 0;
		return super.hasChildren(element);
	}

	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}
}