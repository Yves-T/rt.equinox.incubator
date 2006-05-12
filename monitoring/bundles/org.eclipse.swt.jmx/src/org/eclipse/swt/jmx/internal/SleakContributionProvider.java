/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.jmx.internal;

import java.net.URL;
import java.util.Set;
import java.util.TreeSet;
import javax.management.*;
import org.eclipse.equinox.jmx.common.ContributionNotificationEvent;
import org.eclipse.equinox.jmx.server.Contribution;
import org.eclipse.equinox.jmx.server.ContributionProvider;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Display;

public class SleakContributionProvider extends ContributionProvider {

	private static int DEFAULT_REFRESH_INTERVAL = 10000;

	private int colors, cursors, fonts, gcs, images, regions;
	private boolean started;
	private int refreshInterval = DEFAULT_REFRESH_INTERVAL;
	private Object mutex = new Object();

	public SleakContributionProvider() {
		new Sleak().start();
	}

	/**
	 * Start monitoring graphics resources.
	 */
	public void start() {
		synchronized (mutex) {
			started = true;
			mutex.notify();
		}
	}

	/**
	 * Stop monitoring graphics resources.
	 */
	public synchronized void stop() {
		synchronized (mutex) {
			started = false;
		}
	}

	public void setPollInterval(Integer interval) {
		refreshInterval = interval.intValue();
	}

	protected boolean contributesType(Object obj) {
		return false;
	}

	protected boolean providesType(Object obj) {
		return false;
	}

	protected ContributionProvider createProvider(Object obj) {
		return null;
	}

	protected Contribution createContribution(Object obj) throws MalformedObjectNameException {
		return null;
	}

	protected String getName() {
		return SleakMessages.name;
	}

	protected Object[] getChildren() {
		return null;
	}

	protected Set getProperties() {
		Set result = new TreeSet();
		result.add(SleakMessages.colors + " " + colors);
		result.add(SleakMessages.cursors + " " + cursors);
		result.add(SleakMessages.fonts + " " + fonts);
		result.add(SleakMessages.gcs + " " + gcs);
		result.add(SleakMessages.images + " " + images);
		result.add(SleakMessages.regions + " " + regions);
		return result;
	}

	protected URL getImageLocation() {
		return null;
	}

	protected MBeanInfo getMBeanInfo(Object contributionDelegate) {
		MBeanOperationInfo[] ops = new MBeanOperationInfo[3];
		ops[0] = createStartOperation();
		ops[1] = createStopOperation();
		ops[2] = createSetPollIntervalOperation();
		return new MBeanInfo(SleakContributionProvider.class.getName(), SleakMessages.description, null, null, ops, null);
	}

	protected Object invokeOperation(String operationName, Object[] args, String[] argTypes) {
		if (operationName.equals("start")) {
			start();
		} else if (operationName.equals("stop")) {
			stop();
		} else if (operationName.equals("setPollInterval") && args.length == 1 && args[0] instanceof Integer) {
			try {
				setPollInterval((Integer) args[0]);
			} catch (NumberFormatException e) {
			}
		}
		return null;
	}

	public Object getAttribute(String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException {
		return null;
	}

	public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
	}

	public AttributeList getAttributes(String[] attributes) {
		return null;
	}

	public AttributeList setAttributes(AttributeList attributes) {
		return null;
	}

	private static MBeanOperationInfo createStartOperation() {
		return new MBeanOperationInfo("start", SleakMessages.start_monitoring, new MBeanParameterInfo[0], Void.TYPE.getName(), 0);//$NON-NLS-1$
	}

	private static MBeanOperationInfo createStopOperation() {
		return new MBeanOperationInfo("stop", SleakMessages.stop_monitoring, new MBeanParameterInfo[0], Void.TYPE.getName(), 0); //$NON-NLS-1$
	}

	private static MBeanOperationInfo createSetPollIntervalOperation() {
		try {
			return new MBeanOperationInfo(SleakMessages.poll_interval_desc, SleakContributionProvider.class.getMethod("setPollInterval", new Class[] {Integer.class}));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private class Sleak extends Thread {

		Object[] oldObjects = new Object[0];
		Error[] oldErrors = new Error[0];
		Object[] objects = new Object[0];
		Error[] errors = new Error[0];

		ContributionNotificationEvent event = new ContributionNotificationEvent(ContributionNotificationEvent.NOTIFICATION_UPDATED);

		public Sleak() {
		}

		public void run() {
			while (true) {
				synchronized (mutex) {
					try {
						if (!started) {
							mutex.wait();
						}
						refreshDifference();
					} catch (InterruptedException e) {
					}
				}
				try {
					contributionStateChanged(event);
					Thread.sleep(refreshInterval);
				} catch (InterruptedException e) {
				}
			}
		}

		void refreshCounters() {
			colors = cursors = fonts = gcs = images = regions = 0;
			for (int i = 0; i < objects.length; i++) {
				Object object = objects[i];
				if (object instanceof Color)
					colors++;
				if (object instanceof Cursor)
					cursors++;
				if (object instanceof Font)
					fonts++;
				if (object instanceof GC)
					gcs++;
				if (object instanceof Image)
					images++;
				if (object instanceof Region)
					regions++;
			}
		}

		void refreshDifference() {
			final Display display = Display.getDefault();
			display.asyncExec(new Runnable() {
				public void run() {
					DeviceData info = display.getDeviceData();
					if (!info.tracking) {
						return;
					}
					Object[] newObjects = info.objects;
					Error[] newErrors = info.errors;
					Object[] diffObjects = new Object[newObjects.length];
					Error[] diffErrors = new Error[newErrors.length];
					int count = 0;
					for (int i = 0; i < newObjects.length; i++) {
						int index = 0;
						while (index < oldObjects.length) {
							if (newObjects[i] == oldObjects[index])
								break;
							index++;
						}
						if (index == oldObjects.length) {
							diffObjects[count] = newObjects[i];
							diffErrors[count] = newErrors[i];
							count++;
						}
					}
					objects = new Object[count];
					errors = new Error[count];
					System.arraycopy(diffObjects, 0, objects, 0, count);
					System.arraycopy(diffErrors, 0, errors, 0, count);
					refreshCounters();
				}
			});
		}
	}
}
