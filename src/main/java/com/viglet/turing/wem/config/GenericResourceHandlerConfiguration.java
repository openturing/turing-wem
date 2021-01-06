/*
 * Copyright (C) 2016-2021 Alexandre Oliveira <alexandre.oliveira@viglet.com> 
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.viglet.turing.wem.config;

import com.vignette.as.config.ConfigUtil;
import com.vignette.config.client.common.ConfigException;
import com.vignette.logging.context.ContextLogger;
import com.vignette.util.CustomerMsg;
import com.vignette.util.MsgObject;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/* Updated to add cda properties */
public class GenericResourceHandlerConfiguration implements IHandlerConfiguration {

	public static final String RESOURCE_TYPE = "Properties";
	public static final String RESOURCE_NAME = "VigletTuring";

	private String turingURL;
	private String index;
	private String config;
	private String locale;
	private String channel;
	private String mappingsXML;
	private String cdaContextName;
	private String cdaURLPrefix;
	private String cdaFormatName;
	private String sitesAssociationPriority;
	private boolean isLive;
	private String login;
	private String password;

	private static final ContextLogger log = ContextLogger.getLogger(GenericResourceHandlerConfiguration.class);

	// Load up from Generic Resource
	public GenericResourceHandlerConfiguration() {
		parsePropertiesFromResource();
	}

	@Override
	public String getTuringURL() {
		return turingURL;
	}

	public void setTuringURL(String turingURL) {
		this.turingURL = turingURL;
	}

	@Override
	public String getConfig() {
		return config;
	}

	@Override
	public String getIndex() {
		return index;
	}

	@Override
	public String getChannel() {
		return channel;
	}

	@Override
	public String getLocale() {
		return locale;
	}

	@Override
	public String getMappingsXML() {
		return mappingsXML;
	}

	@Override
	public String getCDAContextName() {
		return cdaContextName;
	}

	@Override
	public String getCDAContextName(String site) {
		String contextName = getDynamicProperties("cda." + site + ".contextname");
		return contextName != null ? contextName : getCDAContextName();
	}

	public void setCDAContextName(String cdaContextName) {
		this.cdaContextName = cdaContextName;
	}

	@Override
	public String getCDAFormatName() {
		return cdaFormatName;
	}

	@Override
	public String getCDAFormatName(String site) {
		String formatName = getDynamicProperties("cda." + site + ".formatname");
		return formatName != null ? formatName : getCDAFormatName();
	}

	@Override
	public String getCDAURLPrefix() {
		return cdaURLPrefix;
	}

	@Override
	public String getCDAURLPrefix(String site) {
		String urlPrefix = getDynamicProperties("cda." + site + ".urlprefix");
		return urlPrefix != null ? urlPrefix : getCDAURLPrefix();
	}

	public void setCDAURLPrefix(String cdaURLPrefix) {
		this.cdaURLPrefix = cdaURLPrefix;
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

		turingURL = properties.getProperty("turing.url");
		config = properties.getProperty("turing.config");
		index = properties.getProperty("turing.index");
		locale = properties.getProperty("turing.locale");
		channel = properties.getProperty("turing.channel");
		mappingsXML = properties.getProperty("turing.mappingsxml", "/CTD-Turing-Mappings.xml");
		cdaContextName = properties.getProperty("cda.default.contextname");
		cdaURLPrefix = properties.getProperty("cda.default.urlprefix");
		cdaFormatName = properties.getProperty("cda.default.formatname");
		sitesAssociationPriority = properties.getProperty("sites.association.priority");
		isLive = Boolean.parseBoolean(properties.getProperty("otsn.isLive", "false"));
	}

	@Override
	public boolean isLive() {
		return isLive;
	}

	public void setLive(boolean isLive) {
		this.isLive = isLive;
	}

	@Override
	public boolean hasSiteName(String site) {
		String hasSiteNameString = getDynamicProperties("cda." + site + ".hasSiteName");
		return (hasSiteNameString == null) ? true : Boolean.parseBoolean(hasSiteNameString);
	}

	@Override
	public boolean hasContext(String site) {
		String hasContextString = getDynamicProperties("cda." + site + ".hasContext");
		return (hasContextString == null) ? true : Boolean.parseBoolean(hasContextString);
	}

	@Override
	public boolean hasFormat(String site) {
		String hasFormatString = getDynamicProperties("cda." + site + ".hasFormat");
		return (hasFormatString == null) ? true : Boolean.parseBoolean(hasFormatString);
	}

	@Override
	public List<String> getSitesAssocPriority() {
		if (sitesAssociationPriority != null) {
			String[] sites = sitesAssociationPriority.split(",");

			List<String> siteList = new ArrayList<String>();
			for (String site : sites) {
				siteList.add(site.trim());
			}
			if (siteList.size() > 0)
				return siteList;
			else
				return null;

		} else
			return null;

	}
	
	@Override
	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	@Override
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
}
