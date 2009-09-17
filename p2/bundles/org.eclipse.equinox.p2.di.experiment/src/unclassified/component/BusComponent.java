/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package unclassified.component;

import agent.IAgentServiceFactory;
import agent.IProvisioningAgent;
import unclassified.EventBus;

/**
 * 
 */
public class BusComponent implements IAgentServiceFactory {

	public Object createService(IProvisioningAgent agent) {
		return new EventBus();
	}

}
