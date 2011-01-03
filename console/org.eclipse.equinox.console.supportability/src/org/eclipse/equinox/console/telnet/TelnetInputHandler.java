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

package org.eclipse.equinox.console.telnet;

import org.eclipse.equinox.console.common.ConsoleInputStream;
import org.eclipse.equinox.console.common.ConsoleOutputStream;
import org.eclipse.equinox.console.common.InputHandler;
import org.eclipse.equinox.console.common.Scanner;

import java.io.InputStream;

/**
 * This class customizes the generic handler with a concrete content processor,
 * which provides telnet protocol handling.
 */
public class TelnetInputHandler extends InputHandler {
    public TelnetInputHandler(InputStream input, ConsoleInputStream in, ConsoleOutputStream out, Callback callback) {
        super(input, in, out);
        inputScanner = new TelnetInputScanner(in, out, callback);
    }
    
}
