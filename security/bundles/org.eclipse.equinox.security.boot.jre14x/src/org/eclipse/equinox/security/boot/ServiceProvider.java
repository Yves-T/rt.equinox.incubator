package org.eclipse.equinox.security.boot;

import java.security.Provider;

import org.eclipse.equinox.internal.security.boot.ProviderServiceInternal;

public class ServiceProvider extends Provider {

	private static final long serialVersionUID = -696520082946329858L;

	public ServiceProvider() {
		super("EQUINOX", 0.1, "");
	}

	public void registerService(ProviderServiceInternal providerService) {
		String key = providerService.getAlgorithm() + "." + providerService.getType();
		this.put(key, providerService.getClassName());
	}
	
	public void unregisterService(ProviderServiceInternal providerService) {
		//TODO:
	}
}
