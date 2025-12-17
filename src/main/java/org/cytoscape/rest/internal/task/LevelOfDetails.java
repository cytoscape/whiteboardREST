package org.cytoscape.rest.internal.task;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import jakarta.enterprise.context.ApplicationScoped;

import org.cytoscape.task.NetworkViewTaskFactory;

@ApplicationScoped
@Component(immediate = true)
public class LevelOfDetails {
  
	private NetworkViewTaskFactory lod;
  
	@Activate
	public LevelOfDetails(@Reference NetworkViewTaskFactory tf) {
		this.lod = tf;
	}
  
	public NetworkViewTaskFactory getLodTF() {
		return lod;
	}
  
}

