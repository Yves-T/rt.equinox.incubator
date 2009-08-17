package org.eclipse.equinox.internal.p2.ui.analysis.dialogs;

import org.eclipse.equinox.internal.p2.ui.admin.ProvAdminUIMessages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class ForeignProfileDialog {
	Text location;

	public ForeignProfileDialog(final Composite parent, ModifyListener listener) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		composite.setLayout(layout);
		GridData data = new GridData();
		data.widthHint = 350;
		composite.setLayoutData(data);

		Label label = new Label(composite, SWT.NONE);
		label.setText("Profile");
		location = new Text(composite, SWT.BORDER);
		location.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		setEditable(location, true, listener);

		Button locationButton = new Button(composite, SWT.PUSH);
		locationButton.setText(ProvAdminUIMessages.ProfileGroup_Browse);
		locationButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				FileDialog dialog = new FileDialog(parent.getShell(), SWT.APPLICATION_MODAL);
				dialog.setFilterExtensions(new String[] {"*.profile;*.profile.gz"});
				String file = dialog.open();
				if (file != null) {
					location.setText(file);
				}
			}
		});
		setEditable(locationButton, true, listener);
	}

	private void setEditable(Control control, boolean editable, ModifyListener listener) {
		if (control instanceof Text) {
			Text text = (Text) control;
			text.setEditable(editable);
			if (listener != null && editable)
				text.addModifyListener(listener);
		} else {
			control.setEnabled(editable);
		}
	}

	public Composite getComposite() {
		if (location == null)
			return null;
		return location.getParent();
	}

	public String getProfilePath() {
		return location.getText();
	}
}
