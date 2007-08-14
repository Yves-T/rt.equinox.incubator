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
package org.eclipse.equinox.internal.security.ssl;

import java.security.Security;
import java.util.Hashtable;

import javax.net.ssl.TrustManagerFactorySpi;

import org.eclipse.equinox.security.boot.ssl.TrustManagerFactoryProxy;
import org.eclipse.equinox.security.ssl.TrustManagerFactorySpiService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;


public class PlatformTrustManagerFactorySpiFactory implements TrustManagerFactoryProxy.ITrustManagerFactorySpiFactory {

	private static PlatformTrustManagerFactorySpiFactory s_instance = new PlatformTrustManagerFactorySpiFactory( );
	public static PlatformTrustManagerFactorySpiFactory getInstance( ) { return s_instance; }
		
	private static Hashtable serviceMap = new Hashtable( );
	private BundleContext bundleContext;

	private PlatformTrustManagerFactorySpiFactory( ) { };
	
	public TrustManagerFactorySpi newInstance( ) {
		TrustManagerFactorySpiService currentFactory = (TrustManagerFactorySpiService)serviceMap.get( getCurrentTMFImpl() );
		if ( null == currentFactory) {
			return new NullTrustManagerFactory( );
		}
		return currentFactory.newInstance( null);
	}
	
	private static String FILTER_STRING = "(objectclass=" + TrustManagerFactorySpiService.class.getName( ) + ")";
	
	public static void attachServiceListener( BundleContext bundleContext) {
		getInstance( ).attachServiceListenerInternal( bundleContext);
	}
	
	private void attachServiceListenerInternal( BundleContext bundleContext) {
		try {
			this.bundleContext = bundleContext;
			bundleContext.addServiceListener( new InternalServiceListener( ), FILTER_STRING);
		}
		catch ( InvalidSyntaxException e) {
			throw new RuntimeException( "FATAL: Syntax of ServiceListener String is invalid!!!!");
		}
	}
	
	private class InternalServiceListener implements ServiceListener {

		public void serviceChanged( ServiceEvent event) {
			
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
	};
	
	private void registerService( ServiceReference ref) {
		
		synchronized ( serviceMap ) {
			String name = ref.getBundle( ).getSymbolicName( );
			serviceMap.put( name, (TrustManagerFactorySpiService)bundleContext.getService( ref));
		}
	}
	
	private void unregisterService( ServiceReference ref) {
		
		synchronized ( serviceMap ) {
			String name = ref.getBundle( ).getSymbolicName( );
			serviceMap.remove( name);
		}
	}
	
	private String getCurrentTMFImpl() {
		return Security.getProperty( TrustManagerFactorySpiService.SECURITY_PROPERTY_PROVIDER);
	}
}
