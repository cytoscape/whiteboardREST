package org.cytoscape.rest.internal.resource;

import java.io.IOException;
import jakarta.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
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
@Component(immediate=true)
@Provider
public class EventFlushRequestFilter implements ContainerRequestFilter {

	@Reference
	protected CyEventHelper cyEventHelper;

	@Override
	public void filter(ContainerRequestContext request) throws IOException {
		cyEventHelper.flushPayloadEvents();
	}
}
