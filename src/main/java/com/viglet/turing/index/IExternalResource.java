package com.viglet.turing.index;

import java.util.List;

import com.viglet.turing.config.IHandlerConfiguration;

public interface IExternalResource {
	
	public abstract List<ExternalResourceObject> listExternalResource(IHandlerConfiguration config) throws Exception;
	
	public abstract ExternalResourceObject getExternalResource(String vcmid, IHandlerConfiguration config) throws Exception;

}
