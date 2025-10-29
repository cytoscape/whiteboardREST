package org.cytoscape.rest.internal;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.cytoscape.ci.CIResponseFactory;
import org.cytoscape.ci.model.CIError;
import org.cytoscape.ci.model.CIResponse;

public class CIResponseFactoryImpl implements CIResponseFactory {
	
	@Override
	public <J extends CIResponse<K>, K> J getCIResponse(K arg0, Class<J> responseClass) throws InstantiationException, IllegalAccessException {
		try{
			J ciResponse = responseClass.getDeclaredConstructor().newInstance();
			List<CIError> errorList = new ArrayList<CIError>();
			ciResponse.data = arg0;
			ciResponse.errors = errorList;
			return ciResponse;
		} catch (NoSuchMethodException e) {
			throw new IllegalAccessException(e.getMessage());
		} catch (InvocationTargetException e) {
			throw new IllegalAccessException(e.getMessage());
		}
	}

	@Override
	public <K> CIResponse<K> getCIResponse(K arg0) {
		CIResponse<K> ciResponse = new CIResponse<K>();
		List<CIError> errorList = new ArrayList<CIError>();
		ciResponse.data = arg0;
		ciResponse.errors = errorList;
		return ciResponse;
	}

}
