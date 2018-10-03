package com.viglet.turing.util;

import java.rmi.RemoteException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.viglet.turing.config.IHandlerConfiguration;
import com.vignette.as.client.common.ref.ChannelRef;
import com.vignette.as.client.common.ref.ManagedObjectVCMRef;
import com.vignette.as.client.common.ref.SiteRef;
import com.vignette.as.client.exception.ApplicationException;
import com.vignette.as.client.exception.AuthorizationException;
import com.vignette.as.client.exception.ValidationException;
import com.vignette.as.client.javabean.Channel;
import com.vignette.as.client.javabean.ContentInstance;
import com.vignette.as.client.javabean.ManagedObject;
import com.vignette.logging.context.ContextLogger;

public class ETLTuringTranslator {

	IHandlerConfiguration config;

	private static final ContextLogger log = ContextLogger.getLogger(ETLTuringTranslator.class);

	public ETLTuringTranslator(IHandlerConfiguration config) {
		this.config = config;
	}

	public String translate(String attributeValue)
			throws ApplicationException, AuthorizationException, ValidationException, RemoteException {

		String href = null;
		String guid = null;

		Pattern hrefFinder = Pattern.compile("<a.*href=\"(.*?)\"", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		Matcher regexMatcher = hrefFinder.matcher(attributeValue);

		while (regexMatcher.find()) {
			href = regexMatcher.group(1);
		}
		if (log.isDebugEnabled()) {
			log.error("ETLTuringTranslator Href: " + href);
		}
		if (href != null) {
			Pattern guidFinder = Pattern.compile(
					".*vgn_ext_templ_rewrite.*vgnextoid=(.*?)RCRD.*/vgn_ext_templ_rewrite.*",
					Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
			Matcher regexMatcherGUID = guidFinder.matcher(href);
			// vgn_ext_templ_rewrite?vgnextoid=616913074c0a3410VgnVCM1000003b74010aRCRD/vgn_ext_templ_rewrite
			while (regexMatcherGUID.find()) {
				guid = regexMatcherGUID.group(1);
			}

			if (log.isDebugEnabled()) {
				log.error("ETLOTSNTranslator GUID: " + guid);
			}
			if (guid != null) {
				guid += "RCRD";
				return translateByGUID(guid);
			} else {
				return href;
			}
		} else {
			return attributeValue;
		}

	}

	public String translateByGUID(String guid)
			throws ApplicationException, AuthorizationException, ValidationException, RemoteException {

		ChannelRef[] fcref = null;
		Channel firstChannel;
		String chFurlName = "";
		String ciFurlName = "";
		StringBuffer channelPath = new StringBuffer();
		String moFurlName = "";
		ManagedObject mo = ManagedObject.findByContentManagementId(new ManagedObjectVCMRef(guid));
		if (mo instanceof ContentInstance) {
			if (log.isDebugEnabled()) {
				log.error("ETLTuringTranslator MO: ContentInstance");
			}
			ContentInstance ci = (ContentInstance) mo;

			fcref = ci.getChannelAssociations();
			ciFurlName = ci.getFurlName();
			if (fcref.length > 0) {
				firstChannel = fcref[0].getChannel();

				Channel[] breadcrumb = firstChannel.getBreadcrumbPath(true);
				for (int j = 0; j < breadcrumb.length; j++) {
					if (j > 0) {
						channelPath.append("/" + breadcrumb[j].getFurlName());
					}
				}
				channelPath.append("/");
				chFurlName = channelPath.toString();

			}
			moFurlName = normalizeText(chFurlName + ciFurlName);

		} else if (mo instanceof Channel) {
			if (log.isDebugEnabled()) {
				log.error("ETLTuringTranslator MO: Channel");
			}
			Channel ch = (Channel) mo;
			Channel[] breadcrumb = ch.getBreadcrumbPath(true);
			for (int j = 0; j < breadcrumb.length; j++) {
				if (j > 0) {
					channelPath.append("/" + breadcrumb[j].getFurlName());
				}
			}
			chFurlName = channelPath.toString();
			moFurlName = normalizeText(chFurlName);
		}
		String siteUrl = getSiteUrl(mo);
		if (siteUrl != null) {
			return siteUrl + moFurlName;
		} else {
			return null;
		}

	}

	public String normalizeText(String text) {
		return text.replaceAll("-", "–").replaceAll(" ", "-").replaceAll("\\?", "%3F");
	}

	public String getSiteDomain(ManagedObject mo)
			throws ApplicationException, RemoteException, AuthorizationException, ValidationException {
		ChannelRef[] fcref = null;
		Channel firstChannel;
		SiteRef[] sr = null;
		String siteNameAssociated = "default";
		if (mo instanceof ContentInstance) {
			ContentInstance ci = (ContentInstance) mo;
			fcref = ci.getChannelAssociations();

			if (fcref.length > 0) {
				firstChannel = fcref[0].getChannel();
				sr = firstChannel.getSiteRefs();
			}
		} else if (mo instanceof Channel) {
			Channel ch = (Channel) mo;
			sr = ch.getSiteRefs();
		}

		if ((sr != null) && (sr.length > 0)) {
			siteNameAssociated = sr[0].getSite().getName();

			return getSiteDomainBySiteName(siteNameAssociated);
		} else {
			log.info("ETLTuringTranslator Content without channel:" + mo.getName().toString());
			return null;
		}

	}

	public String getSiteDomainBySiteName(String siteName)
			throws ApplicationException, RemoteException, AuthorizationException, ValidationException {
		if (log.isDebugEnabled()) {
			log.debug("ETLTuringTranslator getSiteUrl:" + siteName);
		}

		String cdaServer = config.getCDAServer(siteName) + ":";
		String cdaPort = config.getCDAPort(siteName);

		return "http://" + cdaServer + cdaPort;

	}

	public String getSiteUrl(ManagedObject mo)
			throws ApplicationException, RemoteException, AuthorizationException, ValidationException {
		ChannelRef[] fcref = null;
		Channel firstChannel;
		SiteRef[] sr = null;
		String siteNameAssociated = "default";
		if (mo != null) {
			if (mo instanceof ContentInstance) {
				ContentInstance ci = (ContentInstance) mo;
				fcref = ci.getChannelAssociations();

				if (fcref.length > 0) {
					firstChannel = fcref[0].getChannel();
					sr = firstChannel.getSiteRefs();
				}
			} else if (mo instanceof Channel) {
				Channel ch = (Channel) mo;
				sr = ch.getSiteRefs();
			}

			if ((sr != null) && (sr.length > 0)) {
				siteNameAssociated = sr[0].getSite().getName();

				if (log.isDebugEnabled()) {
					log.debug("ETLTuringTranslator getSiteUrl:" + siteNameAssociated);
				}

				String cdaContextName = "/" + config.getCDAContextName(siteNameAssociated) + "/";

				return getSiteDomain(mo) + cdaContextName + normalizeText(siteNameAssociated);
			} else {
				if (log.isDebugEnabled()) {
					log.debug("ETLTuringTranslator Content without channel:" + mo.getName().toString());
				}
				return null;
			}
		} else {
			if (log.isDebugEnabled()) {
				log.debug("ETLTuringTranslator Content is null");
			}
			return null;
		}

	}
}
