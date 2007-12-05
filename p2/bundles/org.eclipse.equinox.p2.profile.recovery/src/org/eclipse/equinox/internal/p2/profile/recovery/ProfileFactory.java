/***********************************************************import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.p2.engine.Profile;
import org.eclipse.osgi.service.environment.EnvironmentInfo;
ailable under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.equinox.internal.p2.profile.recovery;

import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.p2.engine.Profile;
import org.eclipse.osgi.service.environment.EnvironmentInfo;

public class ProfileFactory {

	static private String FLAVOR_DEFAULT = "tooling"; //$NON-NLS-1$
	static private String EMPTY = ""; //$NON-NLS-1$
	static private EnvironmentInfo info;

	public static Profile makeProfile() {
		Profile profile = new Profile(getDefaultLocation());
		profile.setValue(Profile.PROP_INSTALL_FOLDER, getDefaultLocation());
		profile.setValue(Profile.PROP_FLAVOR, getDefaultFlavor());
		profile.setValue(Profile.PROP_ENVIRONMENTS, getDefaultEnvironments());
		profile.setValue(Profile.PROP_NL, getDefaultNL());
		return profile;
	}

	public static String getDefaultLocation() {
		if (Activator.ctx.getProperty("osgi.dev") == null) //$NON-NLS-1$
			return Platform.getInstallLocation().getURL().getPath();
		return Platform.getConfigurationLocation().getURL().getPath() + "selfhostingRoot/"; //$NON-NLS-1$
	}

	public static String getDefaultFlavor() {
		return FLAVOR_DEFAULT;
	}

	private static EnvironmentInfo getEnvironmentInfo() {
		if (info == null) {
			info = (EnvironmentInfo) ServiceHelper.getService(Activator.ctx, EnvironmentInfo.class.getName());
		}
		return info;
	}

	public static String getDefaultNL() {
		if (getEnvironmentInfo() != null) {
			return info.getNL();
		}
		return EMPTY;
	}

	public static String getDefaultEnvironments() {
		if (getEnvironmentInfo() != null) {
			return "osgi.os=" + info.getOS() + ",osgi.ws=" + info.getWS() + ",osgi.arch=" + info.getOSArch(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		return EMPTY;
	}
}
