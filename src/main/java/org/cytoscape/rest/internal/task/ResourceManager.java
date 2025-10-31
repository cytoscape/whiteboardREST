package org.cytoscape.rest.internal.task;

import org.cytoscape.rest.internal.BundleResourceProvider;
import org.cytoscape.rest.internal.CyNetworkViewWriterFactoryManager;
import org.cytoscape.rest.internal.task.AutomationAppTracker;

import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.service.util.CyServiceRegistrar;

import org.osgi.util.tracker.ServiceTracker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceManager {
 private final static Logger logger = LoggerFactory.getLogger(ResourceManager.class);

  public static final String PORT_NUMBER_PROP = "rest.port";
  public static final Integer DEF_PORT_NUMBER = 1234;

  // Note; this used to be used
  public static final String HOST = "localhost";

	public static String cyRESTPort;
	public static CyServiceRegistrar serviceRegistrar;
	public static AutomationAppTracker appTracker;
	public static CyNetworkViewWriterFactoryManager viewWriterFactoryManager;
	public static ServiceTracker cytoscapeJsWriterFactory;
	public static ServiceTracker cytoscapeJsReaderFactory;
	public static BundleResourceProvider resourceProvider; 

	public ResourceManager(final CyServiceRegistrar serviceRegistrar,
			                   final BundleResourceProvider resourceProvider,
			                   final AutomationAppTracker appTracker,
												 final ServiceTracker cytoscapeJsReaderFactory,
												 final ServiceTracker cytoscapeJsWriterFactory,
												 final String cyRESTPort) {
		ResourceManager.serviceRegistrar = serviceRegistrar;
		ResourceManager.resourceProvider = resourceProvider;
		ResourceManager.cyRESTPort = cyRESTPort;
		ResourceManager.appTracker = appTracker;
		ResourceManager.cytoscapeJsReaderFactory = cytoscapeJsReaderFactory;
		ResourceManager.cytoscapeJsWriterFactory = cytoscapeJsWriterFactory;
		ResourceManager.viewWriterFactoryManager = new CyNetworkViewWriterFactoryManager();

    serviceRegistrar.registerServiceListener(viewWriterFactoryManager, "addFactory", "removeFactory",
        CyNetworkViewWriterFactory.class);
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

	public AutomationAppTracker getAutomationAppTracker() {
		return appTracker;
	}

	public BundleResourceProvider getBundleResourceProvider() {
		return resourceProvider;
	}

}
