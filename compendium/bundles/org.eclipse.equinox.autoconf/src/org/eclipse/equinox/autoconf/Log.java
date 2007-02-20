/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.equinox.autoconf;

import org.osgi.framework.BundleContext;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Utility class with static methods for logging to LogService, if available
 */
public class Log {
	static private ServiceTracker logTracker;

	private Log() {
	};

	static void init(BundleContext bc) {
		logTracker = new ServiceTracker(bc, LogService.class.getName(), null);
		logTracker.open();
	}

	static void dispose() {
		if (logTracker != null) {
			logTracker.close();
		}
		logTracker = null;
	}

	public static void log(int level, String message) {
		log(level, null, null, message, null);
	}

	public static void log(int level, String message, Throwable e) {
		log(level, null, null, message, e);
	}

	public static void log(int level, String method, String message) {
		log(level, null, method, message, null);
	}

	public static void log(int level, Object obj, String method, String message) {
		log(level, obj, method, message, null);
	}

	public static void log(int level, Object obj, String method, String message, Throwable e) {
		// LogService logService = (LogService) logTracker.getService();
		LogService logService = null;
		String msg = null;
		if (method == null)
			if (obj == null)
				msg = message;
			else
				msg = "(" + obj.getClass().getName() + ")";
		else if (obj == null)
			msg = "[" + method + "]" + message;
		else
			msg = "[" + method + "]" + message + "(" + obj.getClass().getName() + ")";

		if (logService != null) {
			logService.log(level, msg, e);
		} else {
			String levelSt = null;
			if (level == LogService.LOG_DEBUG)
				levelSt = "DEBUG";
			else if (level == LogService.LOG_INFO)
				levelSt = "INFO";
			else if (level == LogService.LOG_WARNING)
				levelSt = "WARNING";
			else if (level == LogService.LOG_ERROR)
				levelSt = "ERROR";

			System.out.println("[" + levelSt + "]" + msg);
			if (e != null)
				System.out.println(e);
		}
	}
}