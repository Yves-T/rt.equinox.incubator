
package org.eclipse.osgi.aspectj.tests;

import java.io.File;
import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.framework.internal.core.ReferenceInputStream;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.PackageAdmin;

public class TestsActivator implements BundleActivator {

    private static BundleContext bundleContext;

    private static PackageAdmin packageAdmin;

    private static FrameworkListener listener = new TestsFrameworkListener();

    public void start(BundleContext context) throws Exception {
        // TODO Auto-generated method stub
        bundleContext = context;

        ServiceReference reference = context
                .getServiceReference(PackageAdmin.class.getName());
        if (reference != null) {
            packageAdmin = (PackageAdmin) context.getService(reference);
        }

        bundleContext.addFrameworkListener(listener);
    }

    public void stop(BundleContext context) throws Exception {
        // TODO Auto-generated method stub
        bundleContext.removeFrameworkListener(listener);

        bundleContext = null;
    }

    protected static Bundle installBundle(String name, boolean start)
            throws Exception {
        if (CachingTest.debug)
            System.out.println("> TestsActivator.installBundle() name=" + name);

        Bundle bundle = null;
        String testsLocation = bundleContext.getBundle().getLocation();
        String location = testsLocation.substring(7);
        String installLocation = Platform.getInstallLocation().getURL()
                .getFile();
        File dir = new File(installLocation, location).getParentFile();
        File file = new File(dir, name).getCanonicalFile();

        URL url = file.toURL();
        if (CachingTest.debug)
            System.out.println("- TestsActivator.installBundle() location="
                    + url);
        bundle = bundleContext.installBundle(url.toString(),
                new ReferenceInputStream(url));
        bundle.update();

        //		refreshPackages();
        resolveBundles(new Bundle[] { bundle });

        //		if (!isFragment(bundle)) bundle.start();
        if (start) bundle.start();
        //		installedBundles.add(bundle);

        if (CachingTest.debug)
            System.out.println("< TestsActivator.installBundle() bundle="
                    + bundle + ", state=" + bundle.getState());
        return bundle;
    }

    public static void resolveBundles(Bundle[] bundles) {
        boolean success = packageAdmin.resolveBundles(bundles);
    }

    public static BundleContext getContext() {
        return bundleContext;
    }

    /*
     * Not only do we need to uninstall the bundles but also unexport their
     * packages if we are to reinstall them again. This is an asynchronous
     * process so we wait for a Framework event
     */
    public static void uninstallBundles(Bundle[] bundles)
            throws BundleException {
        for (int i = 0; i < bundles.length; i++) {
            bundles[i].uninstall();
        }

        refreshPackages(null);
    }

    public static void refreshPackages(Bundle[] bundles) throws BundleException {
        packageAdmin.refreshPackages(bundles);
        synchronized (listener) {
            try {
                listener.wait();
            } catch (InterruptedException ex) {
                throw new BundleException(ex.toString(), ex);
            }
        }
    }

    private static class TestsFrameworkListener implements FrameworkListener {

        public void frameworkEvent(FrameworkEvent event) {
            //            System.out.println("? TestsActivator.frameworkEvent() type="
            //                    + event.getType() + ", bundle=" + event.getBundle()
            //                    + ", th=" + event.getThrowable());
            Throwable th = event.getThrowable();
            if (th != null) th.printStackTrace();
            synchronized (this) {
                notifyAll();
            }
        }
    };

}
