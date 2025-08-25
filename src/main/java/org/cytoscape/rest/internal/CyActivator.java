package org.cytoscape.rest.internal;

import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import org.cytoscape.app.event.AppsFinishedStartingEvent;
import org.cytoscape.app.event.AppsFinishedStartingListener;
import org.cytoscape.io.BasicCyFileFilter;
import org.cytoscape.io.DataCategory;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;

import org.cytoscape.rest.internal.reader.EdgeListReaderFactory;
import org.cytoscape.rest.internal.resource.AlgorithmicResource;
import org.cytoscape.rest.internal.resource.AppsResource;
import org.cytoscape.rest.internal.resource.CollectionResource;
import org.cytoscape.rest.internal.resource.MiscResource;
import org.cytoscape.rest.internal.resource.NetworkNameResource;
import org.cytoscape.rest.internal.resource.SessionResource;
import org.cytoscape.rest.internal.task.AllAppsStartedListener;
import org.cytoscape.rest.internal.task.AutomationAppTracker;
import org.cytoscape.rest.internal.task.ResourceManager;

import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CyActivator extends AbstractCyActivator implements AppsFinishedStartingListener
{

	private CyServiceRegistrar registrar;
	private static final Logger logger = LoggerFactory.getLogger(CyActivator.class);

	private String port;
	private	AutomationAppTracker automationAppTracker = null;
	private ServiceTracker cytoscapeJsWriterFactory = null;
	private ServiceTracker cytoscapeJsReaderFactory = null;
	private	ResourceManager resourceManager = null;


  public CyActivator() {
    super();
  }

	@Override
	public void start(BundleContext bc) {
		registrar = getService(bc, CyServiceRegistrar.class);

		registerService(bc, this, AppsFinishedStartingListener.class);

		// We need to do these here because the CyServiceRegistrar doesn't provide the createFilter
		// method.
		try {
			automationAppTracker = new AutomationAppTracker(bc, bc.createFilter(CyRESTConstants.ANY_SERVICE_FILTER));
			automationAppTracker.open();
			bc.addBundleListener(automationAppTracker);

			cytoscapeJsWriterFactory = new ServiceTracker(bc, bc.createFilter("(&(objectClass=org.cytoscape.io.write.CyNetworkViewWriterFactory)(id=cytoscapejsNetworkWriterFactory))"), null);
			cytoscapeJsWriterFactory.open();
			cytoscapeJsReaderFactory = new ServiceTracker(bc, bc.createFilter("(&(objectClass=org.cytoscape.io.read.InputStreamTaskFactory)(id=cytoscapejsNetworkReaderFactory))"), null);
			cytoscapeJsReaderFactory.open();
		} catch (Exception e) {
			System.out.println("Unable to initialize Service Trackers");
		}

		try {
			setPortConfig(bc);
		} catch (Exception e) {
			System.out.println("Unable to set default port: "+e.toString());
		}

		final StreamUtil streamUtil = getService(bc, StreamUtil.class);
		final CyNetworkFactory netFact = getService(bc, CyNetworkFactory.class);
		final CyNetworkViewFactory netViewFact = getService(bc, CyNetworkViewFactory.class);
    final CyNetworkManager netMan = getService(bc, CyNetworkManager.class);
		final CyRootNetworkManager cyRootNetworkManager = getService(bc, CyRootNetworkManager.class);


		// Extra readers and writers
    final BasicCyFileFilter elFilter = new BasicCyFileFilter(new String[] { "el" },
        new String[] { "text/edgelist" }, "Edgelist files", DataCategory.NETWORK, streamUtil);
    final EdgeListReaderFactory edgeListReaderFactory = new EdgeListReaderFactory(elFilter, netViewFact, netFact,
        netMan, cyRootNetworkManager);
    final Properties edgeListReaderFactoryProps = new Properties();
    edgeListReaderFactoryProps.setProperty("ID", "edgeListReaderFactory");
    registerService(bc, edgeListReaderFactory, InputStreamTaskFactory.class, edgeListReaderFactoryProps);

		resourceManager = new ResourceManager(registrar, automationAppTracker, 
				                                  cytoscapeJsReaderFactory, cytoscapeJsWriterFactory, port);

		/*
		new AlgorithmicResource(resourceManager);
		new AppsResource(resourceManager);
		new CollectionResource(resourceManager);
		new MiscResource(resourceManager);
		new NetworkNameResource(resourceManager);
		new SessionResource(resourceManager);
		*/

	}

	@Override
	public void shutDown() {
		if (cytoscapeJsWriterFactory != null) {
      cytoscapeJsWriterFactory.close();
    }
    if (cytoscapeJsReaderFactory != null) {
      cytoscapeJsReaderFactory.close();
    }
    super.shutDown();
	}

	/**
	 * For some reason, we need to "kick" the jaxb wiring to get the
	 * first call to work.  This does that.  It will generate a backtrace,
	 * but that's somewhat intentional.
	 */
	public void initiateCall() throws Exception {
		URL url = new URL("http://"+ResourceManager.HOST+":"+ResourceManager.DEF_PORT_NUMBER+"/v1/");
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		int status = con.getResponseCode();
	}

  /**
   * Set the port the CyREST service will be listening on.
   *
   * @param context
   * @throws Exception
   */
  private void setPortConfig(BundleContext context) throws Exception
  {
		port = "1234";
    ServiceReference configurationAdminReference =
        context.getServiceReference(ConfigurationAdmin.class.getName());

    if (configurationAdminReference != null)
    {
      ConfigurationAdmin configurationAdmin = (ConfigurationAdmin) context.getService(configurationAdminReference);

      Configuration config = configurationAdmin.getConfiguration("org.ops4j.pax.web", null);

      Dictionary<String, Object> dictionary = new Hashtable<String, Object>();
      dictionary.put("org.osgi.service.http.port", port);
      //Set session timeout to infinite (while Cytoscape is running)
      dictionary.put("org.ops4j.pax.web.session.timeout", "0");

      config.update(dictionary);

      context.ungetService(configurationAdminReference);
    }
    else
    {
      throw new IllegalStateException("No available ConfigurationAdmin service.");
    }
  }

	@Override
  public void handleEvent(AppsFinishedStartingEvent event)  {
		System.out.println("All apps loaded");

		// Initiate all of our services.  We need to do this here bacause
		// the whiteboard calls each constructor with no arguments, so this
		// is how we pass arguments
		//
		for (Bundle bundle: automationAppTracker.getAppBundles()) {
			for (Object obj: automationAppTracker.getServices(bundle)) {
				// For each service that has an init method, call it
				try {
					Method method;
					if ((method = getMethod(obj, "init", ResourceManager.class)) != null) {
						method.invoke(obj,resourceManager);
					} else if ((method = getMethod(obj, "init", CyServiceRegistrar.class)) != null) {
						method.invoke(obj,registrar);
					}
				} catch (Exception iae) {
					logger.error("Unable to initialize: "+obj);
				}
			}
		}

    try {
      // initiateCall();
    }
    catch (Exception e) {
      e.printStackTrace();
      logger.error("Unable to start CyREST", e);
    }
  }

	private Method getMethod(Object obj, String methodName, Class<?>... parameterTypes) {
		try {
			Method method = obj.getClass().getMethod(methodName, parameterTypes);
			return method;
		} catch (NoSuchMethodException e) {
			return null;
		}
	}

}
