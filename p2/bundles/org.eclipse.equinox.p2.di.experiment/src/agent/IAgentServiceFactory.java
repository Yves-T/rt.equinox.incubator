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
package agent;

/**
 * A factory for creating a service that forms part of a provisioning agent instance.
 */
public interface IAgentServiceFactory {
	public static final String AGENT_SERVICE = "agent.IAgentServiceFactory"; //$NON-NLS-1$
	public static final String PROP_SERVICE_NAME = "p2.agent.servicename"; //$NON-NLS-1$

	public Object createService(IProvisioningAgent agent);
}
