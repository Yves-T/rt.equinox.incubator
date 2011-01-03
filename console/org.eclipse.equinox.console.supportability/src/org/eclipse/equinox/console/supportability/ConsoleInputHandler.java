/*******************************************************************************
 * Copyright (c) 2010 SAP AG
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Lazar Kirchev, SAP AG - initial API and implementation 
 *******************************************************************************/

package org.eclipse.equinox.console.supportability;

import org.eclipse.equinox.console.common.ConsoleInputStream;
import org.eclipse.equinox.console.common.InputHandler;
import org.eclipse.equinox.console.common.Scanner;
import org.osgi.framework.BundleContext;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * This class customizes the generic handler with a concrete content processor,
 * which provides command line editing.
 */
public class ConsoleInputHandler extends InputHandler {
    public ConsoleInputHandler(InputStream input, ConsoleInputStream in, OutputStream out) {
        super(input, in, out);
        inputScanner = new ConsoleInputScanner(in, out);
    }
    
}
