/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.internal.security.provider;

import java.security.Security;

import org.eclipse.equinox.internal.security.boot.ProviderServiceInternal;
import org.eclipse.equinox.security.boot.ServiceProvider;
import org.eclipse.equinox.security.provider.ProviderService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

public class ProviderServiceListener implements ServiceListener {

	private static ProviderServiceListener s_instance = new ProviderServiceListener( );
	public static ProviderServiceListener getInstance( ) { return s_instance; }

	private static final String FILTER_STRING = "(objectclass=" + ProviderService.class.getName( ) + ")";;
	
	private BundleContext bundleContext;
	private ServiceProvider provider;
	
	public static void attachServiceListener( BundleContext bundleContext) {
		getInstance( ).attachServiceListenerInternal( bundleContext);
	}
	
	private void attachServiceListenerInternal( BundleContext bundleContext) {
		try {
			provider = (ServiceProvider)Security.getProvider("EQUINOX");
			this.bundleContext = bundleContext;
			bundleContext.addServiceListener( this, FILTER_STRING);
		}
		catch ( InvalidSyntaxException e) {
			throw new RuntimeException( "FATAL: Syntax of ServiceListener String is invalid!!!!");
		}
	}
	
	public void serviceChanged(ServiceEvent event) {

		switch ( event.getType( )) {
			case ServiceEvent.REGISTERED:
				registerService( event.getServiceReference( ));
				break;
				
			case ServiceEvent.UNREGISTERING:
				unregisterService( event.getServiceReference( ));
				break;
				
			case ServiceEvent.MODIFIED:
				break;
				
			default:
				break;
		}
	}

	private void registerService( ServiceReference ref) {
		
		ProviderService service = (ProviderService)bundleContext.getService(ref);
		ProviderServiceLoader loader = new ProviderServiceLoader(ref.getBundle());
		
		ProviderServiceInternal internalService = service.getInternalService( );
		internalService.setClassLoader(loader);
		internalService.setProvider(provider);

		provider.registerService(internalService);
	}
	
	private void unregisterService( ServiceReference ref) {
		//tell the provider to remove the service
	}
}
