package com.viglet.turing.config;

public interface IHandlerConfiguration {

    String getOTSNServer();
    String getOTCAServer();
    String getQueryPort();
    String getIndexPort();
    String getSOLRPort();
    String getIndex();
    String getConfig();
    String getLocale();
    String getChannel();
    String getMappingsXML();
    String getTMEPort();
    String getCDAContextName(); 
    String getCDAServer();
    String getCDAPort();
    String getCDAServer(String site);
    String getCDAPort(String site);
    String getCDAContextName(String site);
    String getSiteFormat();
	boolean isLive();
   
}
