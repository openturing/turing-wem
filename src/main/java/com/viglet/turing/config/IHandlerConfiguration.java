package com.viglet.turing.config;

public interface IHandlerConfiguration {

    String getTuringURL();
    String getIndex();
    String getConfig();
    String getLocale();
    String getChannel();
    String getMappingsXML();
    String getCDAContextName(); 
    String getCDAServer();
    String getCDAPort();
    String getCDAServer(String site);
    String getCDAPort(String site);
    String getCDAContextName(String site);
    String getSiteFormat();
    boolean hasSiteName(String site);
    boolean hasContext(String site);
    boolean hasFormat(String site);
	boolean isLive();
   
}
