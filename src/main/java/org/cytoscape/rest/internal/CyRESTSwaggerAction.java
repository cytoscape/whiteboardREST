package org.cytoscape.rest.internal;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLEncoder;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.rest.internal.task.ResourceManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.OpenBrowser;

public abstract class CyRESTSwaggerAction extends AbstractCyAction{

	protected final ResourceManager resourceManager;

	public CyRESTSwaggerAction(String name, ResourceManager resourceManager) {
		super(name);
		this.setPreferredMenu(CyRESTConstants.CY_REST_HELP_MENU_ANCHOR);
		this.resourceManager = resourceManager;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			final OpenBrowser openBrowser = resourceManager.getService(OpenBrowser.class);
			String port = resourceManager.getCyRESTPort();
			String url = rootURL() + "?url=" + URLEncoder.encode("http://" + ResourceManager.HOST + ":" + port + "/" + swaggerPath(), "UTF-8");
			System.out.println("URL: "+url);
			openBrowser.openURL(url);
		} catch ( IOException e1) {
			e1.printStackTrace();
		}
	}

	protected abstract String rootURL();

	protected abstract String swaggerPath();

}
