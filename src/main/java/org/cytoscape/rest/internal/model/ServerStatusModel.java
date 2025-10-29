package org.cytoscape.rest.internal.model;

import javax.xml.bind.annotation.XmlRootElement;

import io.swagger.v3.oas.annotations.media.Schema;

@XmlRootElement
public class ServerStatusModel {
	
	private boolean allAppsStarted;
	private String apiVersion;
	private Integer numberOfCores;
	
	private MemoryStatusModel memoryStatus;

	
	public ServerStatusModel() {
		this.setApiVersion("v1");
		this.setNumberOfCores(Runtime.getRuntime().availableProcessors());
		this.setMemoryStatus(new MemoryStatusModel());
	}
	
	@Schema(description="`true` if this instance of Cytoscape has finished loading all installed apps. If this value is `false`, sending requests to App-dependent operations may not be safe.")
	public boolean getAllAppsStarted() {
		return allAppsStarted;
	}
	
	
	public void setAllAppsStarted(boolean allAppsLoaded) {
		this.allAppsStarted = allAppsLoaded;
	}
	
	/**
	 * @return the apiVersion
	 */
	@Schema(description="CyREST API Version")
	public String getApiVersion() {
		return apiVersion;
	}

	/**
	 * @param apiVersion
	 *            the apiVersion to set
	 */
	public void setApiVersion(String apiVersion) {
		this.apiVersion = apiVersion;
	}

	/**
	 * @return the numberOfCores
	 */
	@Schema(description="Number of Processor Cores Available")
	public Integer getNumberOfCores() {
		return numberOfCores;
	}

	/**
	 * @param numberOfCores
	 *            the numberOfCores to set
	 */
	public void setNumberOfCores(Integer numberOfCores) {
		this.numberOfCores = numberOfCores;
	}

	/**
	 * @return the memoryStatus
	 */
	@Schema(description="Details on memory use and availability.")
	public MemoryStatusModel getMemoryStatus() {
		return memoryStatus;
	}

	/**
	 * @param memoryStatus the memoryStatus to set
	 */
	public void setMemoryStatus(MemoryStatusModel memoryStatus) {
		this.memoryStatus = memoryStatus;
	}

}
