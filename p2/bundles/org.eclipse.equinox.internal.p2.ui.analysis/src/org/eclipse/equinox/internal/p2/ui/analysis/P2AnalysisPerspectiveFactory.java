package org.eclipse.equinox.internal.p2.ui.analysis;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class P2AnalysisPerspectiveFactory implements IPerspectiveFactory {
	private static final String FOLDER = P2AnalysisPerspectiveFactory.class.getName() + "folder";

	public void createInitialLayout(IPageLayout layout) {
		layout.addView(IUQueryablesView.class.getName(), IPageLayout.LEFT, 0.6f, layout.getEditorArea());

		IFolderLayout x = layout.createFolder(FOLDER, IPageLayout.RIGHT, 0.4f, layout.getEditorArea());
		x.addView(IUPropertiesView.class.getName());
		x.addView("org.eclipse.p2.ui.admin.ArtifactRepositoriesView");
		layout.setEditorAreaVisible(false);
	}
}
