package org.cytoscape.rest.internal.resource;

import java.io.File;
import java.io.IOException;

import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.rest.internal.model.FileModel;
import org.cytoscape.rest.internal.model.MessageModel;
import org.cytoscape.rest.internal.model.SessionNameModel;
import org.cytoscape.rest.internal.task.HeadlessTaskMonitor;
import org.cytoscape.rest.internal.task.ResourceManager;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.task.create.NewSessionTaskFactory;
import org.cytoscape.task.read.OpenSessionTaskFactory;
import org.cytoscape.task.write.SaveSessionAsTaskFactory;
import org.cytoscape.task.write.SaveSessionTaskFactory;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Component(service = SessionResource.class, property = { "osgi.jaxrs.resource=true" })
@Tag(name = CyRESTSwagger.CyRESTSwaggerConfig.SESSION_TAG)
@Path("/v1/session")
public class SessionResource extends AbstractResource {

	static final String RESOURCE_URN = "session";

	private CySessionManager sessionManager;
	private SaveSessionAsTaskFactory saveSessionAsTaskFactory;
	private OpenSessionTaskFactory openSessionTaskFactory;
	private NewSessionTaskFactory newSessionTaskFactory;

	@Override
	public String getResourceURI() {
		return RESOURCE_URN;
	}
	
	private final static Logger logger = LoggerFactory.getLogger(SessionResource.class);
		
	@Override
	public Logger getResourceLogger() {
		return logger;
	}
	
	public static final int INTERNAL_METHOD_ERROR = 1;
	


	public SessionResource(ResourceManager manager) {
		super(manager);
		System.out.println("SessionResource");
	}

	public SessionResource() {
		super();
		System.out.println("SessionResource()");
	}

	public void init(ResourceManager manager) {
		super.init(manager);
		sessionManager = manager.getService(CySessionManager.class);
		saveSessionAsTaskFactory = manager.getService(SaveSessionAsTaskFactory.class);;
		openSessionTaskFactory = manager.getService(OpenSessionTaskFactory.class);;
		newSessionTaskFactory = manager.getService(NewSessionTaskFactory.class);;
	}


	@GET
	@Path("/name")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary ="Get current session file name",
    description ="Returns the file name for the current Cytoscape session.",
    responses = {
			@ApiResponse(responseCode = "200", description = "Session name", 
			             content = { @Content(schema = @Schema(implementation=SessionNameModel.class))})
	})
	public SessionNameModel getSessionName() 
	{
		String sessionName = sessionManager.getCurrentSessionFileName();
		if(sessionName == null || sessionName.isEmpty()) {
			sessionName = "";
		}
		return new SessionNameModel(sessionName);
	}


	
	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary ="Delete current Session",
    description ="This deletes the current session and initializes a new one. A message is returned to indicate the success of the deletion.",
    responses = {
			@ApiResponse(responseCode = "200", description="A message",
			             content = { @Content(schema = @Schema(implementation=MessageModel.class))})
	})
	public MessageModel deleteSession() {

		try {
			TaskIterator itr = newSessionTaskFactory.createTaskIterator(true);
			while(itr.hasNext()) {
				final Task task = itr.next();
				task.run(new HeadlessTaskMonitor());
			}
		} catch (Exception e) {
			e.printStackTrace();
			//throw getError("Could not delete current session.", e, Response.Status.INTERNAL_SERVER_ERROR);
			throw this.getCIWebApplicationException(Status.INTERNAL_SERVER_ERROR.getStatusCode(), 
					RESOURCE_URN, 
					INTERNAL_METHOD_ERROR, 
					"Could not delete current session.", 
					logger, e);
		}
		return new MessageModel("New session created.");
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary ="Load a Session from a local file",
    description ="Loads a session from a local file and returns the session file name",
    responses = {
			@ApiResponse(responseCode = "200", description="The loaded file",
			             content = { @Content(schema = @Schema(implementation=FileModel.class))})
	})
	public FileModel getSessionFromFile(@Parameter(description ="Session file location as an absolute path", required = true) @QueryParam("file") String file) throws IOException
	{
		System.out.println("File: "+file);
		File sessionFile = null;
		try {
			sessionFile = new File(file);
			TaskIterator itr = openSessionTaskFactory.createTaskIterator(sessionFile);
			while(itr.hasNext()) {
				final Task task = itr.next();
				task.run(new HeadlessTaskMonitor());
			}
		} catch (Exception e) {
			e.printStackTrace();
			//throw getError("Could not open session.", e, Response.Status.INTERNAL_SERVER_ERROR);
			throw this.getCIWebApplicationException(Status.INTERNAL_SERVER_ERROR.getStatusCode(), 
					RESOURCE_URN, 
					INTERNAL_METHOD_ERROR, 
					"Could not open session.", 
					logger, e);
		}
		cyEventHelper.flushPayloadEvents();
		return new FileModel(sessionFile.getAbsolutePath());
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(summary ="Save current Session to a file",
    description ="Saves the current session to a file. If successful, the session file location will be returned.",
	responses = {
			@ApiResponse(responseCode = "200", description = "Saved file", 
			             content = { @Content(schema = @Schema(implementation=FileModel.class))})
	})
	public FileModel createSessionFile(@Parameter(description ="Session file location as an absolute path", required = true) @QueryParam("file") String file) {
		File sessionFile = null;
		try {
			sessionFile = new File(file);
			TaskIterator itr = saveSessionAsTaskFactory.createTaskIterator(sessionFile);
			while(itr.hasNext()) {
				final Task task = itr.next();
				task.run(new HeadlessTaskMonitor());
			}
		} catch (Exception e) {
			e.printStackTrace();
			//throw getError("Could not save session.", e, Response.Status.INTERNAL_SERVER_ERROR);
			throw this.getCIWebApplicationException(Status.INTERNAL_SERVER_ERROR.getStatusCode(), 
					RESOURCE_URN, 
					INTERNAL_METHOD_ERROR, 
					"Could not save session.", 
					logger, e);
		}
	
		return new FileModel(sessionFile.getAbsolutePath());
	}
}
