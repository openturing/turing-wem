package com.viglet.turing.config;

import com.vignette.as.config.ConfigUtil;
import com.vignette.config.client.common.ConfigException;
import com.vignette.logging.context.ContextLogger;
import com.vignette.util.CustomerMsg;
import com.vignette.util.MsgObject;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

/**
 * The format of the generic resource value should be as follows:<br/>
 * <code>
 * ostn.server=demo.nstein.com<br/>
 * otsn.port=80<br/>
 * </code>
 */

/* Updated to add cda properties */
public class GenericResourceHandlerConfiguration implements IHandlerConfiguration {

	public static final String RESOURCE_TYPE = "Properties";
	public static final String RESOURCE_NAME = "OTSN";

	// <string, CollabClientServiceConfigAdapter>
	private String otsnServer;
	private String otcaServer;
	private String indexPort;
	private String queryPort;
	private String solrPort;
	private String index;
	private String config;
	private String locale;
	private String channel;
	private String mappingsXML;
	private String tmePort;
	private String cdaContextName;
	private String cdaServer;
	private String cdaPort;
	private String siteFormat;
	private boolean isLive;

	private static final ContextLogger log = ContextLogger.getLogger(GenericResourceHandlerConfiguration.class);

	// Load up from Generic Resource
	public GenericResourceHandlerConfiguration() {
		parsePropertiesFromResource();
	}

	public String getOTSNServer() {
		return otsnServer;
	}

	public void setOTSNServer(String otsnServer) {
		this.otsnServer = otsnServer;
	}

	public String getOTCAServer() {
		return otcaServer;
	}

	public void setOTCAServer(String otcaServer) {
		this.otcaServer = otcaServer;
	}

	public String getQueryPort() {
		return this.queryPort;
	}

	public void setQueryPort(String queryPort) {
		this.queryPort = queryPort;
	}

	public String getIndexPort() {
		return this.indexPort;
	}

	public void setIndexPort(String indexPort) {
		this.indexPort = indexPort;
	}

	public String getConfig() {
		return config;
	}

	public String getIndex() {
		return index;
	}

	public String getChannel() {
		return channel;
	}

	public String getLocale() {
		return locale;
	}

	public String getSOLRPort() {
		return solrPort;
	}

	public String getTMEPort() {
		return tmePort;
	}

	public String getMappingsXML() {
		return mappingsXML;
	}

	public String getCDAContextName() {
		return cdaContextName;
	}

	public String getCDAContextName(String site) {
		return getDynamicProperties("cda." + site + ".contextname");
	}

	public void setCDAContextName(String cdaContextName) {
		this.cdaContextName = cdaContextName;
	}

	public String getCDAServer() {
		return cdaServer;
	}

	public String getCDAServer(String site) {
		return getDynamicProperties("cda." + site + ".server");
	}

	public void setCDAServer(String cdaServer) {
		this.cdaServer = cdaServer;
	}

	public String getCDAPort() {
		return cdaPort;
	}

	public String getCDAPort(String site) {
		return getDynamicProperties("cda." + site + ".port");
	}

	public void setCDAPort(String cdaPort) {
		this.cdaPort = cdaPort;
	}

	public String getSiteFormat() {
		return siteFormat;
	}

	public void setSiteFormat(String siteFormat) {
		this.siteFormat = siteFormat;
	}

	private void parsePropertiesFromResource() {
		try {
			String propertiesBody = ConfigUtil.getGenericResourceValue(RESOURCE_TYPE, RESOURCE_NAME);
			if (log.isDebugEnabled()) {
				log.debug(propertiesBody);
			}

			if (propertiesBody == null) {
				MsgObject msg = CustomerMsg
						.getMsgObject("Generic Resource [" + RESOURCE_NAME + "] is empty or does not exist.");
				log.error(msg);
			}

			StringReader propsBodyStream = new StringReader(propertiesBody);

			Properties properties = new Properties();

			properties.load(propsBodyStream);

			parseProperties(properties);

		} catch (ConfigException e) {
			MsgObject msg = CustomerMsg.getMsgObject("Error loading generic resource [" + RESOURCE_NAME + "]");
			log.error(msg, e);
		} catch (IOException e) {
			MsgObject msg = CustomerMsg
					.getMsgObject("Generic Resource [" + RESOURCE_NAME + "] is empty or does not exist.");
			log.error(msg);
		}
	}

	private String getDynamicProperties(String property) {
		try {
			String propertiesBody = ConfigUtil.getGenericResourceValue(RESOURCE_TYPE, RESOURCE_NAME);
			if (log.isDebugEnabled()) {
				log.debug(propertiesBody);
			}

			if (propertiesBody == null) {
				MsgObject msg = CustomerMsg
						.getMsgObject("Generic Resource [" + RESOURCE_NAME + "] is empty or does not exist.");
				log.error(msg);
			}

			StringReader propsBodyStream = new StringReader(propertiesBody);

			Properties properties = new Properties();

			properties.load(propsBodyStream);

			return properties.getProperty(property);

		} catch (ConfigException e) {
			MsgObject msg = CustomerMsg.getMsgObject("Error loading generic resource [" + RESOURCE_NAME + "]");
			log.error(msg, e);
		} catch (IOException e) {
			MsgObject msg = CustomerMsg
					.getMsgObject("Generic Resource [" + RESOURCE_NAME + "] is empty or does not exist.");
			log.error(msg);
		}
		return null;
	}

	private void parseProperties(Properties properties) {

		otsnServer = properties.getProperty("otsn.server");
		otcaServer = properties.getProperty("otca.server");
		indexPort = properties.getProperty("otsn.index.port");
		queryPort = properties.getProperty("otsn.query.port");
		solrPort = properties.getProperty("otsn.solrport");
		tmePort = properties.getProperty("otsn.tmeport");
		config = properties.getProperty("otsn.config");
		index = properties.getProperty("otsn.index");
		locale = properties.getProperty("otsn.locale");
		channel = properties.getProperty("otsn.channel");
		mappingsXML = properties.getProperty("otsn.mappingsxml", "/CTD-Nstein-Mappings.xml");
		cdaContextName = properties.getProperty("cda.default.contextname");
		cdaServer = properties.getProperty("cda.default.server");
		cdaPort = properties.getProperty("cda.default.port");
		siteFormat = properties.getProperty("site.format", "web");
		isLive = Boolean.parseBoolean(properties.getProperty("otsn.isLive", "false"));
	}

	public boolean isLive() {
		return isLive;
	}

	public void setLive(boolean isLive) {
		this.isLive = isLive;
	}
}
