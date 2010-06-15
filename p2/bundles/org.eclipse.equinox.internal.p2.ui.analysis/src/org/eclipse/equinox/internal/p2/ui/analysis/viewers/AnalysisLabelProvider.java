package org.eclipse.equinox.internal.p2.ui.analysis.viewers;

import java.util.Collection;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.equinox.internal.p2.ui.ProvUIImages;
import org.eclipse.equinox.internal.p2.ui.analysis.model.IUElement;
import org.eclipse.equinox.internal.p2.ui.analysis.model.RequirementElement;
import org.eclipse.equinox.internal.p2.ui.analysis.model.RequirementElement.PropertyPairElement;
import org.eclipse.equinox.internal.p2.ui.viewers.ProvElementLabelProvider;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

public class AnalysisLabelProvider extends ProvElementLabelProvider {
	public String getText(Object obj) {
		if (obj instanceof IStatus)
			return ((IStatus) obj).getMessage();
		else if (obj instanceof TreeElement)
			return ((TreeElement<?>) obj).getText();
		else if (obj instanceof PropertyPairElement)
			return ((PropertyPairElement) obj).getProperty();
		else if (obj instanceof RequirementElement)
			return ((RequirementElement) obj).getLabel(obj);
		return super.getText(obj);
	}

	public String getColumnText(Object element, int columnIndex) {

		switch (columnIndex) {
			case 0 :
				return getText(element);
			case 1 :
				if (element instanceof RequirementElement)
					return ((RequirementElement) element).getVersion();
				else if (element instanceof PropertyPairElement)
					return ((PropertyPairElement) element).getValue();
			default :
				return super.getColumnText(element, columnIndex);
		}
	}

	public Image getImage(Object obj) {
		if (obj instanceof TreeElement) {
			if (((TreeElement<?>) obj).hasChildren())
				return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_INFO_TSK);
		} else if (obj instanceof Collection) {
			if (((Collection<?>) obj).isEmpty())
				return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_INFO_TSK);
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
		} else if (obj instanceof IStatus) {
			if (((IStatus) obj).isOK())
				return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_INFO_TSK);
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
		} else if (obj instanceof IUElement) {
			if (((IUElement) obj).isMarked())
				return new DecorationOverlayIcon(ProvUIImages.getImage(ProvUIImages.IMG_IU), PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_DEC_FIELD_ERROR), IDecoration.BOTTOM_RIGHT).createImage();
			return ProvUIImages.getImage(ProvUIImages.IMG_IU);
		} else if (obj instanceof RequirementElement) {
			return ProvUIImages.getImage(ProvUIImages.IMG_DISABLED_IU);
		} else if (obj instanceof PropertyPairElement) {
			return ProvUIImages.getImage(ProvUIImages.IMG_METADATA_REPOSITORY);
		}
		return super.getImage(obj);
	}
}