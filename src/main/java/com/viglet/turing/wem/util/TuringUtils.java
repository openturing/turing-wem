/*
 * Copyright (C) 2016-2019 Alexandre Oliveira <alexandre.oliveira@viglet.com> 
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
package com.viglet.turing.wem.util;

import java.util.ArrayList;
import java.util.HashSet;

import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import com.viglet.turing.wem.beans.TuringTag;
import com.viglet.turing.wem.beans.TuringTagMap;
import com.vignette.as.client.common.AttributeData;
import com.vignette.as.client.common.AttributeDefinitionData;
import com.vignette.as.client.common.DataType;
import com.vignette.as.client.common.ref.ManagedObjectRef;
import com.vignette.as.client.common.ref.ObjectTypeRef;
import com.vignette.as.client.exception.ApplicationException;
import com.vignette.as.client.javabean.ContentInstance;
import com.vignette.as.client.javabean.ContentType;
import com.vignette.as.client.javabean.ManagedObject;
import com.vignette.logging.context.ContextLogger;

public class TuringUtils {
	private static final ContextLogger log = ContextLogger.getLogger(TuringUtils.class);

	public static String listToString(List<String> stringList) {
		StringBuilder sb = new StringBuilder();
		int i = 0;
		for (String s : stringList) {
			if (i++ != stringList.size() - 1) {
				sb.append(s);
				sb.append(", ");
			}
		}
		return sb.toString();
	}

	// Old turIndexAttMapToSet
	public static Set<TuringTag> turingTagMapToSet(TuringTagMap turingTagMap) {
		Set<TuringTag> turingTags = new HashSet<TuringTag>();
		for (Entry<String, ArrayList<TuringTag>> entryCtd : turingTagMap.entrySet()) {
			for (TuringTag turingTag : entryCtd.getValue()) {
				turingTags.add(turingTag);
			}
		}
		return turingTags;
	}

	public static ContentInstance findContentInstanceByKey(ContentType contentType, String primaryKeyValue)
			throws Exception {

		ContentInstance ci = null;
		try {
			AttributeDefinitionData add = getKeyAttributeDefinitionData(contentType);
			DataType dt = add.getDataType();
			Object val = primaryKeyValue;
			if (dt.isInt() || dt.isNumerical() || dt.isTinyInt())
				val = new Integer(primaryKeyValue);
			ObjectTypeRef otr = new ObjectTypeRef(contentType);
			AttributeData atd = new AttributeData(add, val, otr);
			ManagedObjectRef ref = new ManagedObjectRef(otr, new AttributeData[] { atd });

			ci = (ContentInstance) ManagedObject.findById(ref);
		} catch (ApplicationException e) {
			log.error(e.getStackTrace());
		}

		return ci;
	}

	public static AttributeDefinitionData getKeyAttributeDefinitionData(ContentType ct) throws Exception {
		AttributeDefinitionData adds[] = new AttributeDefinitionData[0];
		adds = ct.getData().getTopRelation().getKeyAttributeDefinitions();
		if (adds == null)
			throw new Exception("Failed to retrieve primary key definition", null);
		if (adds.length == 0)
			throw new Exception("No primary key found", null);
		if (adds.length > 1) {
			StringBuilder sb = new StringBuilder();
			sb.append("Works with one primary key only: ").append(adds.length);
			throw new Exception(sb.toString(), null);
		} else
			return adds[0];
	}

}
