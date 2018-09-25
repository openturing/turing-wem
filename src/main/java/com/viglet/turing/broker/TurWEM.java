package com.viglet.turing.broker;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.w3c.dom.Document;

import com.viglet.turing.beans.TuringTag;
import com.viglet.turing.broker.attribute.TurWEMAttrXML;
import com.viglet.turing.config.GenericResourceHandlerConfiguration;
import com.viglet.turing.config.IHandlerConfiguration;
import com.viglet.turing.exceptions.MappingNotFoundException;
import com.viglet.turing.index.ExternalResourceObject;
import com.viglet.turing.mappers.CTDMappings;
import com.viglet.turing.mappers.MappingDefinitions;
import com.viglet.turing.util.TuringUtils;
import com.viglet.turing.util.XmlParserUtilities;
import com.vignette.as.client.common.ref.ChannelRef;
import com.vignette.as.client.exception.ApplicationException;
import com.vignette.as.client.javabean.Channel;
import com.vignette.as.client.javabean.ContentInstance;
import com.vignette.as.client.javabean.IPagingList;
import com.vignette.as.client.javabean.Site;
import com.vignette.ext.templating.link.LinkBuilder;
import com.vignette.ext.templating.link.LinkSpec;
import com.vignette.ext.templating.util.ContentUtil;
import com.vignette.ext.templating.util.RequestContext;
import com.vignette.logging.context.ContextLogger;

public class TurWEM {

	private static MappingDefinitions mappingDefinitions = null;
	private static final ContextLogger log = ContextLogger.getLogger(TurWEM.class);

	public static String getXML(ContentInstance ci, IHandlerConfiguration config) throws Exception {

		if (log.isDebugEnabled()) {
			log.debug("Generating Viglet Turing XML for a content instance");
		}
		StringBuffer xml = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\" ?><document>");
		xml.append("<id>" + ci.getContentManagementId().getId() + "</id>");

		// we force the type on the Viglet Turing side
		HashMap<String, CTDMappings> mappings = mappingDefinitions.getMappingDefinitions();

		CTDMappings ctdMappings = mappings.get(ci.getObjectType().getData().getName());

		if (ctdMappings == null) {

			if (log.isDebugEnabled()) {
				log.debug("Mapping definition is not found in the mappingXML for the CTD: "
						+ ci.getObjectType().getData().getName());
			}
			throw new MappingNotFoundException("Mapping definition is not found in the mappingXML for the CTD: "
					+ ci.getObjectType().getData().getName());
		}

		TuringTag typeTag = ctdMappings.findIndexTagInMappings("type");
		if (typeTag == null || ci.getAttributeValue(typeTag.getSrcAttribute()) == null
				|| ci.getAttributeValue(typeTag.getSrcAttribute()).toString().trim().equals("")) {
			xml.append("<type>" + ci.getObjectType().getData().getName() + "</type>");
		}

		HashMap<String, List<String>> attributesDefs = new HashMap<String, List<String>>();

		attributesDefs.put("headline", new ArrayList<String>());
		attributesDefs.put("text", new ArrayList<String>());
		attributesDefs.put("title", new ArrayList<String>());
		attributesDefs.put("url", new ArrayList<String>());

		TimeZone tz = TimeZone.getTimeZone("UTC");
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
		df.setTimeZone(tz);

		String modDate = ci.getLastModTime() != null ? df.format(ci.getLastModTime()) : df.format(ci.getCreationTime());
		String publishDate = ci.getLastPublishDate() != null ? df.format(ci.getLastPublishDate()) : modDate;
		xml.append(
				"<modification_date>" + modDate + "</modification_date><publication_date>" + publishDate + "</publication_date>");

		for (String key : ctdMappings.getIndexAttrs()) {
			for (TuringTag tag : ctdMappings.getIndexAttrTag(key)) {
				if (key != null && tag != null && tag.getTagName() != null) {

					if (log.isDebugEnabled()) {
						log.debug("Key: " + key + " Tag: " + tag.getTagName() + " relation: "
								+ TuringUtils.listToString(tag.getSrcAttributeRelation()) + " content Type: "
								+ tag.getSrcAttributeType());
					}
					attributesDefs = TurWEMAttrXML.attributeXML(ci, attributesDefs, tag, key, config, mappingDefinitions);
				}
			}
		}

		// Create xml of attributesDefs
		for (Entry<String, List<String>> entry : attributesDefs.entrySet()) {
			String key = entry.getKey();
			List<String> listValue = entry.getValue();
			for (String value : listValue) {
				if ((value != null) && (value.toString().trim().length() > 0)) {
					xml.append("<" + key + "><![CDATA[" + value.toString() + "]]></" + key + ">");
				}
			}
		}

		// Let's append the sections (channel path)
		ChannelRef[] cref = ci.getChannelAssociations();
		for (int i = 0; i < cref.length; i++) {
			xml.append("<section><![CDATA[");
			Channel currentChannel = cref[i].getChannel();
			String[] breadcrumb = currentChannel.getBreadcrumbNamePath(true);
			for (int j = 0; j < breadcrumb.length; j++) {
				xml.append("/" + breadcrumb[j]);
			}
			xml.append("]]></section>");

		}

		String classifications[] = ci.getTaxonomyClassifications();
		if (classifications != null && classifications.length > 0) {
			for (int i = 0; i < classifications.length; i++) {
				String wemClassification = classifications[i].substring(classifications[i].lastIndexOf("/") + 1);
				xml.append("<tmeCategories>").append(wemClassification).append("</tmeCategories>");
			}
		}

		xml.append("</document>");

		if (log.isDebugEnabled()) {
			log.debug("Viglet Turing XML content: " + xml.toString());
		}

		return xml.toString();

	}

	public static String getXML(ExternalResourceObject ci, IHandlerConfiguration config) throws Exception {

		if (log.isDebugEnabled()) {
			log.debug("Generating Viglet Turing XML for a content instance");
		}
		StringBuffer xml = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\" ?><document>");
		xml.append("<id>" + ci.getId() + "</id>");

		// we force the type on the Viglet Turing side
		HashMap<String, CTDMappings> mappings = mappingDefinitions.getMappingDefinitions();

		CTDMappings ctdMappings = mappings.get(ci.getTypeName());

		if (ctdMappings == null) {

			if (log.isDebugEnabled()) {
				log.debug("Mapping definition is not found in the mappingXML for the CTD: " + ci.getTypeName());
			}
			throw new MappingNotFoundException(
					"Mapping definition is not found in the mappingXML for the CTD: " + ci.getTypeName());
		}

		TuringTag typeTag = ctdMappings.findIndexTagInMappings("type");
		if (typeTag == null || ci.get(typeTag.getSrcAttribute()) == null
				|| ci.get(typeTag.getSrcAttribute()).toString().trim().equals("")) {
			xml.append("<type>" + ci.getTypeName() + "</type>");
		}

		HashMap<String, List<String>> attributesDefs = new HashMap<String, List<String>>();

		attributesDefs.put("headline", new ArrayList<String>());
		attributesDefs.put("text", new ArrayList<String>());
		attributesDefs.put("title", new ArrayList<String>());
		attributesDefs.put("url", new ArrayList<String>());

		// for now considering the current date as published and original data
		// SimpleDateFormat sdf = new SimpleDateFormat("MMM d yyyy");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		// String currentDate = sdf.format(new Date());
		String modDate = ci.getLastModTime() != null ? sdf.format(ci.getLastModTime())
				: sdf.format(ci.getCreationTime());
		String publishDate = ci.getLastPublishDate() != null ? sdf.format(ci.getLastPublishDate()) : modDate;
		xml.append(
				"<modification_date>" + modDate + "</modification_date><publication_date>" + publishDate + "</publication_date>");

		for (String key : ctdMappings.getIndexAttrs()) {
			for (TuringTag tag : ctdMappings.getIndexAttrTag(key)) {
				if (key != null && tag != null && tag.getTagName() != null) {

					if (log.isDebugEnabled()) {
						log.debug("Key: " + key + " Tag: " + tag.getTagName() + " relation: "
								+ TuringUtils.listToString(tag.getSrcAttributeRelation()) + " content Type: "
								+ tag.getSrcAttributeType());
					}
					attributesDefs = TurWEMAttrXML.attributeXML(ci, attributesDefs, tag, key, config);
				}
			}
		}

		// Create xml of attributesDefs
		for (Entry<String, List<String>> entry : attributesDefs.entrySet()) {
			String key = entry.getKey();
			List<String> listValue = entry.getValue();
			for (String value : listValue) {
				if ((value != null) && (value.toString().trim().length() > 0)) {
					xml.append("<" + key + "><![CDATA[" + value.toString() + "]]></" + key + ">");
				}
			}
		}

		// Let's append the sections (link)
		xml.append("<section><![CDATA[");
		xml.append("/");
		xml.append("]]></section>");

		xml.append("</document>");

		if (log.isDebugEnabled()) {
			log.debug("Viglet Turing XML content: " + xml.toString());
		}

		return xml.toString();

	}



	// This method returns the link to the primary Channel for Semantic
	// Navigation
	@SuppressWarnings("unchecked")
	public static String getSemanticLink(GenericResourceHandlerConfiguration turingConfig, RequestContext rc) {

		// Looking up the Semantic Navigation channel, or pick the current
		// channel if not available
		if (log.isDebugEnabled()) {
			log.debug("Building a link to the Semantic Navigation channel");
		}

		LinkSpec spec = new LinkSpec();
		String guid = rc.getRequestOIDString();
		Site currentSite = null;
		try {
			currentSite = rc.getCurrentSite();
		} catch (ApplicationException e) {
			log.error("Unable to get the current site using requestContext.getCurrentSite api");
		}
		String link = "";
		try {
			String channelName = turingConfig.getChannel();
			if (currentSite != null) {
				if (log.isDebugEnabled()) {
					log.debug("Loading the channel [" + channelName + "] using the site:"
							+ currentSite.getData().getName());
				}
				Channel semanticNavigation = ContentUtil.getChannelByPath(currentSite, channelName);
				if (semanticNavigation != null) {
					guid = semanticNavigation.getContentManagementId().getId();
					if (log.isDebugEnabled()) {
						log.debug("Found the channel with guid [" + guid + "]");
					}
				} else {
					if (log.isDebugEnabled()) {
						log.debug("Channel was not found, defaulting to current guid [" + guid + "]");
					}
				}
			} else {
				if (log.isDebugEnabled()) {
					log.debug(
							"Unable to get the current site using requestContext.getCurrentSite api loading the channel ["
									+ channelName + "] without site");
				}
				IPagingList list = Channel.findByName(channelName);
				List<Channel> channelList = list.asList();
				if (!channelList.isEmpty()) {
					Channel semanticNavigation = (Channel) channelList.get(0);
					if (semanticNavigation != null) {
						guid = semanticNavigation.getContentManagementId().getId();
					}
				}
			}
			spec.setOid(guid);
			link = LinkBuilder.buildLink(spec, rc, true, false);
		} catch (Exception e) {
			log.error("Error when building link to Semantic Navigation channel");
		}
		return link;

	}

	// Gets a widget content...
	public static Document turingRequest(IHandlerConfiguration turingConfig, String query) {

		// A formalized XML document will be returned
		Document doc = null;

		if (query == null || turingConfig == null) {
			return doc;
		}

		try {

			// Primary query
			String request = turingConfig.getTuringURL() + "/" + turingConfig.getIndex() + query;

			// Depending on the form , we need either &format=xml or /format/xml
			if (query.indexOf('?') > 0) {
				request = request + "&format=xml";
			} else {
				request = request + "/format/xml";
			}
			if (log.isDebugEnabled()) {
				log.debug("OSTN request is " + request);
			}
			// Performs a simple GET request to the Viglet Turing server
			GetMethod get = new GetMethod(request);
			// setting the header as below is also not helping in converting the
			// response body to utf-8 string.
			// get.setRequestHeader("Content-Type",
			// "application/xml;charset=utf-8");
			HttpClient httpclient = new HttpClient();

			int result = httpclient.executeMethod(get);
			if (log.isDebugEnabled()) {
				log.debug("OSTN request HTTP return code is " + result);
			}
			InputStream bodyStream = get.getResponseBodyAsStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(bodyStream, "UTF-8"));
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = br.readLine()) != null) {
				sb.append(line + "\n");
			}
			br.close();
			// String xml = get.getResponseBodyAsString();
			String xml = sb.toString();
			if (log.isDebugEnabled()) {
				log.debug("OSTN request HTTP response string is " + xml);
			}
			get.releaseConnection();

			// Let's treat the results as XML
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputStream is = new ByteArrayInputStream(xml.getBytes("UTF-8"));
			doc = db.parse(is);
			doc.getDocumentElement().normalize();

		} catch (Exception e) {

			log.error("Error is " + e.getMessage());

		}

		return doc;

	}

	public static boolean createAndRegisterTuringIndex(IHandlerConfiguration config, String indexName,
			String templateName) {
		boolean success = false;
		if (templateName == null || templateName.trim().equals("")) {
			templateName = "default-en";
		}
		try {
			if (log.isDebugEnabled()) {
				log.debug("Creating the index with the indexname:" + indexName);
			}
			PostMethod post = new PostMethod(config.getTuringURL() + "/sse/index/" + indexName);
			post.setParameter("template", templateName);
			post.setRequestHeader("Accept", "*/*");
			HttpClient httpclient = new HttpClient();
			int result = httpclient.executeMethod(post);
			if (log.isDebugEnabled()) {
				log.debug("Viglet Turing create indexer response HTTP result is: " + result);
				log.debug("Viglet Turing create indexer response HTTP response body is: "
						+ post.getResponseBodyAsString());
			}
			post.releaseConnection();
			if (result == 201) {
				if (log.isDebugEnabled()) {
					log.debug("Created the index, now registering the index with the solr indexname:" + indexName);
				}
				GetMethod get = new GetMethod(config.getTuringURL() + "/solr/admin/cores?action=CREATE&name="
						+ indexName + "&instanceDir=" + indexName);
				get.setRequestHeader("Accept", "*/*");
				result = httpclient.executeMethod(get);
				if (log.isDebugEnabled()) {
					log.debug("Viglet Turing register indexer response HTTP result is: " + result);
					log.debug("Viglet Turing register indexer response HTTP response body is: "
							+ get.getResponseBodyAsString());
				}
				get.releaseConnection();
				success = true;
			}
		} catch (Exception e) {
			System.out.println("Error is " + e.getMessage());
		}
		return success;
	}

	public static boolean deleteTuringIndex(IHandlerConfiguration config, String indexName) {
		boolean success = false;
		try {
			HttpClient httpclient = new HttpClient();
			DeleteMethod del = new DeleteMethod(config.getTuringURL() + "/sse/index/" + indexName);
			del.setRequestHeader("Accept", "*/*");
			int result = httpclient.executeMethod(del);
			if (log.isDebugEnabled()) {
				log.debug("Viglet Turing Delete indexer response HTTP result is: " + result);
				log.debug("Viglet Turing Delete indexer response HTTP response body is: "
						+ del.getResponseBodyAsString());
			}
			del.releaseConnection();
			success = true;
		} catch (Exception e) {
			System.out.println("Error is " + e.getMessage());
		}
		return success;
	}

	public static void printTuringIndexes(IHandlerConfiguration config) {
		try {
			HttpClient httpclient = new HttpClient();
			GetMethod get = new GetMethod(config.getTuringURL() + "/sse/index");
			get.setRequestHeader("Accept", "*/*");
			int result = httpclient.executeMethod(get);
			if (log.isDebugEnabled()) {
				log.debug("executing query:" + get.getURI());
				log.debug("Viglet Turing list indexer response HTTP result is: " + result);
				log.debug(
						"Viglet Turing list indexer response HTTP response body is: " + get.getResponseBodyAsString());
			}
			get.releaseConnection();
		} catch (Exception e) {
			System.out.println("Error is " + e.getMessage());
		}
	}

	public static MappingDefinitions getMappingDefinitions(IHandlerConfiguration config) {
		if (mappingDefinitions == null || !mappingDefinitions.getMappingsXML().equals(config.getMappingsXML())) {
			mappingDefinitions = XmlParserUtilities.loadMappings(config.getMappingsXML());
		}
		if (mappingDefinitions == null) {
			if (log.isDebugEnabled()) {
				log.error("Mapping definitions are not loaded properly from mappingsXML: " + config.getMappingsXML());
			}
		}
		return mappingDefinitions;
	}

	public static boolean isExternalResource(String objectTypeName, IHandlerConfiguration config) {
		return getCustomClassName(objectTypeName, config) != null;
	}

	public static String getCustomClassName(String objectTypeName, IHandlerConfiguration config) {
		HashMap<String, CTDMappings> mappings = getMappingDefinitions(config).getMappingDefinitions();
		CTDMappings ctdMappings = mappings.get(objectTypeName);
		if (ctdMappings.getCustomClassName() == null) {
			if (log.isDebugEnabled()) {
				log.debug("Custom className is not found in the mappingXML for the CTD: " + objectTypeName);
			}
			return null;
		}
		return ctdMappings.getCustomClassName();
	}


}
