package org.cytoscape.rest.internal.task;

import org.cytoscape.rest.internal.BundleResourceProvider;
import org.cytoscape.rest.internal.CyNetworkViewWriterFactoryManager;
import org.cytoscape.rest.internal.task.AutomationAppTracker;

import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.service.util.CyServiceRegistrar;

import jakarta.enterprise.context.ApplicationScoped;
import org.osgi.framework.BundleContext;

import org.osgi.service.component.annotations.Component;
import org.osgi.util.tracker.ServiceTracker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//@ApplicationScoped
//@Component(service=ResourceManager.class, immediate=true)
public class ResourceManager {
 private final static Logger logger = LoggerFactory.getLogger(ResourceManager.class);

  public static final String PORT_NUMBER_PROP = "rest.port";
  public static final Integer DEF_PORT_NUMBER = 1234;

  // Note; this used to be used
  public static final String HOST = "localhost";

	public String cyRESTPort;

	public CyServiceRegistrar serviceRegistrar;
	public AutomationAppTracker appTracker;
	public CyNetworkViewWriterFactoryManager viewWriterFactoryManager;
	public ServiceTracker cytoscapeJsWriterFactory;
	public ServiceTracker cytoscapeJsReaderFactory;
	public BundleResourceProvider resourceProvider; 
	public BundleContext bundleContext;

	public ResourceManager(final CyServiceRegistrar serviceRegistrar,
			                   final BundleResourceProvider resourceProvider,
			                   final BundleContext bundleContext,
												 final ServiceTracker cytoscapeJsReaderFactory,
												 final ServiceTracker cytoscapeJsWriterFactory,
												 final String cyRESTPort) {
		this.serviceRegistrar = serviceRegistrar;
		this.resourceProvider = resourceProvider;
		this.cyRESTPort = cyRESTPort;
		this.bundleContext = bundleContext;
		this.cytoscapeJsReaderFactory = cytoscapeJsReaderFactory;
		this.cytoscapeJsWriterFactory = cytoscapeJsWriterFactory;
		this.viewWriterFactoryManager = new CyNetworkViewWriterFactoryManager();

    serviceRegistrar.registerServiceListener(viewWriterFactoryManager, "addFactory", "removeFactory",
        CyNetworkViewWriterFactory.class);
	}

	public CyServiceRegistrar getServiceRegistrar() {
		return serviceRegistrar;
	}

	public <T> T getService(Class<? extends T> clazz) {
    return serviceRegistrar.getService(clazz);
  }

  public <T> T getService(Class<? extends T> clazz, String filter) {
    return serviceRegistrar.getService(clazz, filter);
  }

  public void registerServiceListener(Object listener, String registerMethodName, String unregisterMethodName, Class<?>clazz) {
    serviceRegistrar.registerServiceListener(listener, registerMethodName, unregisterMethodName, clazz);
  }

	public CyNetworkViewWriterFactory getViewWriterFactory() {
    return (CyNetworkViewWriterFactory) cytoscapeJsWriterFactory.getService();
	}

	public InputStreamTaskFactory getViewReaderFactory() {
    return (InputStreamTaskFactory) cytoscapeJsReaderFactory.getService();
	}

	public CyNetworkViewWriterFactoryManager getViewWriterFactoryManager() {
		return this.viewWriterFactoryManager;
	}

	public AutomationAppTracker getAutomationAppTracker() {
		return appTracker;
	}

	public void setAutomationAppTracker(AutomationAppTracker appTracker) {
		this.appTracker = appTracker;
	}

	public String getCyRESTPort() { return cyRESTPort; }

	public void setCyRESTPort(String port) { cyRESTPort = port; }

	public BundleResourceProvider getBundleResourceProvider() {
		return resourceProvider;
	}

	public BundleContext getBundleContext() { return bundleContext; }

}
