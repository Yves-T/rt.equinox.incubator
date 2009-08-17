package org.eclipse.equinox.internal.p2.ui.analysis.dialogs;

import java.util.Hashtable;
import org.eclipse.equinox.internal.p2.ui.analysis.Messages;
import org.eclipse.equinox.internal.p2.ui.analysis.model.IUElement;
import org.eclipse.equinox.internal.p2.ui.analysis.viewers.AnalysisTreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

public class IURequirementPage extends AbstractAnalysisPropertyPage {
	private AnalysisTreeViewer artifactTreeViewer;
	private Button[] radio = new Button[3];
	private IUElement input;

	protected void getContents(Composite parent) {
		input = new IUElement(null, getProfile(), getProfile(), new Hashtable(getProfile().getProperties()), getIU(), true, false);

		Label label = new Label(parent, SWT.NONE);
		label.setText(Messages.IUAnalysisPage_Requirements);
		label.setLayoutData(getGridData(2));

		artifactTreeViewer = new AnalysisTreeViewer(parent, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		artifactTreeViewer.getControl().setLayoutData(getFullGridData(parent));
		artifactTreeViewer.setInput(input);

		Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
		group.setLayoutData(getGridData(3));
		group.setLayout(new RowLayout(SWT.HORIZONTAL));

		radio[0] = new Button(group, SWT.RADIO);
		radio[0].setText(Messages.IURequirementPage_Artifacts);
		radio[0].setSelection(true);
		radio[0].addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if (((Button) event.widget).getSelection())
					updateInput(true, false);
			}
		});

		radio[1] = new Button(group, SWT.RADIO);
		radio[1].setText(Messages.IURequirementPage_IUs);
		radio[1].addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if (((Button) event.widget).getSelection())
					updateInput(false, true);
			}
		});

		radio[2] = new Button(group, SWT.RADIO);
		radio[2].setText(Messages.IURequirementPage_Both);
		radio[2].addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if (((Button) event.widget).getSelection())
					updateInput(true, true);
			}
		});
	}

	private void updateInput(boolean artifact, boolean iu) {
		input.setChildren(artifact, iu);
		artifactTreeViewer.refresh();
	}
}
