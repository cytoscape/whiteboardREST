package org.cytoscape.rest.internal.resource;

import java.io.IOException;
import jakarta.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

import jakarta.enterprise.context.ApplicationScoped;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import org.cytoscape.event.CyEventHelper;


/**
 * This filter flushes payload events from Cytoscape's event model, forcing all events to be applied before returning.
 * 
 * It is necessary to keep CyREST responses from being out of sync with Cytoscape.
 * 
 * @author David Otasek
 *
 */
@ApplicationScoped
@Provider
@Component(immediate=true)
public class EventFlushResponseFilter implements ContainerResponseFilter {

	@Reference
	protected CyEventHelper cyEventHelper;

	@Override
	public void filter(ContainerRequestContext request, ContainerResponseContext response) throws IOException {
		cyEventHelper.flushPayloadEvents();
	}
}
