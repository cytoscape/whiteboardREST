package org.cytoscape.rest.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

public class BundleResourceProvider {
	
	private final Bundle bundle;
	private final BundleContext bundleContext;
	
	public BundleResourceProvider(BundleContext bundleContext) {
		this.bundle = bundleContext.getBundle();
		this.bundleContext = bundleContext;
	}
	
	public InputStream getResourceInputStream(String resourcePath) throws IOException {
		URL url = bundle.getResource(resourcePath);
		return url.openConnection().getInputStream();
	}

	public BundleContext getBundleContext() { return bundleContext; }
}
