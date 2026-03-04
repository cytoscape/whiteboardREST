package org.cytoscape.rest.internal;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
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
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import jakarta.enterprise.context.ApplicationScoped;


import org.cytoscape.app.event.AppsFinishedStartingEvent;
import org.cytoscape.app.event.AppsFinishedStartingListener;
import org.cytoscape.application.swing.CyAction;
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
import org.cytoscape.task.NetworkViewTaskFactory;

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


public class CyActivator extends AbstractCyActivator
{

	private CyServiceRegistrar registrar;
	private static final Logger logger = LoggerFactory.getLogger(CyActivator.class);

	private String port = "1234";
	private	AutomationAppTracker automationAppTracker = null;
	private ServiceTracker cytoscapeJsWriterFactory = null;
	private ServiceTracker cytoscapeJsReaderFactory = null;

	@Reference
	private	ResourceManager resourceManager = null;

  public CyActivator() {
    super();
  }

	@Override
	public void start(BundleContext bc) {
		registrar = getService(bc, CyServiceRegistrar.class);

		final StreamUtil streamUtil = getService(bc, StreamUtil.class);
		final CyNetworkFactory netFact = getService(bc, CyNetworkFactory.class);
		final CyNetworkViewFactory netViewFact = getService(bc, CyNetworkViewFactory.class);
    final CyNetworkManager netMan = getService(bc, CyNetworkManager.class);
		final CyRootNetworkManager cyRootNetworkManager = getService(bc, CyRootNetworkManager.class);
		final BundleResourceProvider resourceProvider = new BundleResourceProvider(bc);


		// We need to do these here because the CyServiceRegistrar doesn't provide the createFilter
		// method.
		try {
			resourceManager = new ResourceManager(registrar, resourceProvider, bc,
			 																			cytoscapeJsReaderFactory, cytoscapeJsWriterFactory, port);

			registerService(bc, resourceManager, ResourceManager.class, new Properties());

			automationAppTracker = new AutomationAppTracker(bc, bc.createFilter(CyRESTConstants.ANY_SERVICE_FILTER));
			automationAppTracker.open();
			bc.addBundleListener(automationAppTracker);

			cytoscapeJsWriterFactory = new ServiceTracker(bc, bc.createFilter("(&(objectClass=org.cytoscape.io.write.CyNetworkViewWriterFactory)(id=cytoscapejsNetworkWriterFactory))"), null);
			cytoscapeJsWriterFactory.open();
			cytoscapeJsReaderFactory = new ServiceTracker(bc, bc.createFilter("(&(objectClass=org.cytoscape.io.read.InputStreamTaskFactory)(id=cytoscapejsNetworkReaderFactory))"), null);
			cytoscapeJsReaderFactory.open();

			resourceManager.setAutomationAppTracker(automationAppTracker);

		} catch (Exception e) {
			System.out.println("Unable to initialize Service Trackers");
			e.printStackTrace();
		}

		try {
			setPortConfig(bc);
		} catch (Exception e) {
			System.out.println("Unable to set default port: "+e.toString());
		}

		CyRESTCoreSwaggerAction swaggerCoreAction = new CyRESTCoreSwaggerAction(resourceManager);
    registerService(bc, swaggerCoreAction, CyAction.class, new Properties());

    CyRESTCommandSwaggerAction swaggerCommandAction = new CyRESTCommandSwaggerAction(resourceManager);
    registerService(bc, swaggerCommandAction, CyAction.class, new Properties());

    CyAutomationAction automationAction = new CyAutomationAction(registrar);
    registerService(bc, automationAction, CyAction.class, new Properties());



		// Extra readers and writers
    final BasicCyFileFilter elFilter = new BasicCyFileFilter(new String[] { "el" },
        new String[] { "text/edgelist" }, "Edgelist files", DataCategory.NETWORK, streamUtil);
    final EdgeListReaderFactory edgeListReaderFactory = new EdgeListReaderFactory(elFilter, netViewFact, netFact,
        netMan, cyRootNetworkManager);
    final Properties edgeListReaderFactoryProps = new Properties();
    edgeListReaderFactoryProps.setProperty("ID", "edgeListReaderFactory");
    registerService(bc, edgeListReaderFactory, InputStreamTaskFactory.class, edgeListReaderFactoryProps);

		// Thread thread = new Thread(new KickJaxb());
		// thread.start();
	}

	public class KickJaxb implements Runnable {
		@Override
		public void run() {
			try {
				Thread.sleep(5000);
				initiateCall();
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
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
		URL url = new URI("http://"+ResourceManager.HOST+":"+ResourceManager.DEF_PORT_NUMBER+"/v1/version").toURL();
		System.out.println("calling: "+url.toString());
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
	// TODO: get the port from the command line, if provided
	@SuppressWarnings({"rawtypes", "unchecked"})
  private void setPortConfig(BundleContext context) throws Exception
  {
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
			resourceManager.setCyRESTPort(port);
    }
    else
    {
      throw new IllegalStateException("No available ConfigurationAdmin service.");
    }
  }

}
