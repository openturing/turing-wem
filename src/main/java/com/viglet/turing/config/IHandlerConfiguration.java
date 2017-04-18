package com.viglet.turing.config;

/**
 * User: bedecoat
 * Date: 13/09/2010
 * Time: 21:43:29
 * To change this template use File | Settings | File Templates.
 */

/* Updated on 02/11 to include cda properties*/
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
