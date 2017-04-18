package com.viglet.turing.index;

import com.viglet.turing.config.IHandlerConfiguration;
import com.vignette.as.client.common.WhereClause;
import com.vignette.as.client.javabean.ContentInstance;
import com.vignette.logging.context.ContextLogger;

public interface IValidToIndex {
	
	static final ContextLogger log = ContextLogger.getLogger(IValidToIndex.class);
	
	boolean isValid(ContentInstance ci, IHandlerConfiguration config) throws Exception;
	boolean isValid(ExternalResourceObject ci, IHandlerConfiguration config) throws Exception;
	
	void whereToValid(WhereClause clause, IHandlerConfiguration config) throws Exception;

}
