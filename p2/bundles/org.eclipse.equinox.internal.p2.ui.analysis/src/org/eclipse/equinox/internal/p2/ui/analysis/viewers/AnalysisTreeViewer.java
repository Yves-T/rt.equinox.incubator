package org.eclipse.equinox.internal.p2.ui.analysis.viewers;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyDialogAction;

public class AnalysisTreeViewer extends TreeViewer {

	public AnalysisTreeViewer(Tree tree) {
		super(tree);
		initialize();
	}

	public AnalysisTreeViewer(Composite parent) {
		super(parent);
		initialize();
	}

	public AnalysisTreeViewer(Composite parent, int style) {
		super(parent, style);
		initialize();
	}

	private void initialize() {
		setContentProvider(new AnalysisContentProvider());
		setLabelProvider(new AnalysisLabelProvider());
		addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				IAction action = getDoubleClickAction();
				if (action != null && action.isEnabled())
					action.run();
			}
		});
	}

	private IAction getDoubleClickAction() {
		PropertyDialogAction action = new PropertyDialogAction(PlatformUI.getWorkbench().getActiveWorkbenchWindow(), this);

		return action;
	}

	public Object[] getSelected() {
		IStructuredSelection selection = (IStructuredSelection) getSelection();
		return selection.toArray();
	}
}