/*******************************************************************************
 * Copyright (c) 2006 Cognos Incorporated.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.equinox.log;

import org.osgi.service.log.LogService;

public interface ExtendedLogService extends LogService, Logger {

	public Logger getLogger(String loggerName);
}
