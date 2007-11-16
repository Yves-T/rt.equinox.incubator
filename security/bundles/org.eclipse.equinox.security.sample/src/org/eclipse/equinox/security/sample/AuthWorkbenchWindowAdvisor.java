package org.eclipse.equinox.security.sample;

import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.application.*;

public class AuthWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {

	public AuthWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
		super(configurer);
		// TODO Auto-generated constructor stub
	}

	public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {
		return new AuthActionBarAdvisor(configurer);
	}

	public void preWindowOpen() {
		//super.preWindowOpen( configurer);
		this.getWindowConfigurer().setShowMenuBar(true);
		this.getWindowConfigurer().setInitialSize(new Point(640, 480));
		this.getWindowConfigurer().setShowCoolBar(false);
		this.getWindowConfigurer().setShowStatusLine(false);
		this.getWindowConfigurer().setTitle("Authenticated Application");
	}
}
