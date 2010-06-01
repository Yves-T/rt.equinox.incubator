package org.eclipse.equinox.demo.file.association;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

public class OpenFileView extends ViewPart {
	public static final String ID = "org.eclipse.equinox.demo.file.association.openFile";
	public OpenFileView() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createPartControl(Composite parent) {
		Text text = new Text(parent, SWT.BORDER);
		text.setText("OpenDocument event for \"" + filesToOpen.remove(0) + "\"");
	}

	@Override
	public void setFocus() {

	}

	
	private static List<String> filesToOpen = Collections.synchronizedList(new ArrayList<String>());
	private static int instanceNum = 0;
	static void openFile(String path) {
		filesToOpen.add(path);
		IWorkbench workbench = PlatformUI.getWorkbench();
		workbench.getDisplay().asyncExec(new Runnable() {
			
			public void run() {
				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				if (window == null)
					return;
				try {
					window.getActivePage().showView(ID, Integer.toString(instanceNum++), IWorkbenchPage.VIEW_ACTIVATE);
				} catch (PartInitException e) {
					// nothing
					e.printStackTrace();
				}
			}
		});
	}
}
