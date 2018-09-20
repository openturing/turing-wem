package com.viglet.turing.broker.relator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.vignette.as.client.exception.ApplicationException;
import com.vignette.as.client.javabean.AttributedObject;
import com.vignette.logging.context.ContextLogger;

public class TurWEMRelator {
	private static final ContextLogger log = ContextLogger.getLogger(TurWEMRelator.class);

	public static AttributedObject[] nestedRelators(List<String> relationTag, List<AttributedObject[]> currentRelation,
			int currentPosition) {
		List<AttributedObject> relators = new ArrayList<AttributedObject>();

		int nextPosition = currentPosition + 1;

		if (nextPosition < relationTag.size()) {
			List<AttributedObject[]> nestedRelationChild = new ArrayList<AttributedObject[]>();
			for (AttributedObject[] attributesFromRelation : currentRelation) {

				for (AttributedObject attributeFromRelation : Arrays.asList(attributesFromRelation)) {
					try {
						AttributedObject[] childRelation = attributeFromRelation
								.getRelations(relationTag.get(nextPosition));

						nestedRelationChild.add(childRelation);

					} catch (ApplicationException e) {
						log.error(String.format("Error getting relations: %s of relation: %s",
								relationTag.get(currentPosition), relationTag.get(currentPosition - 1)), e);
					}
				}
			}
			return nestedRelators(relationTag, nestedRelationChild, nextPosition);

		} else {
			for (AttributedObject[] attributesFromRelation : currentRelation) {
				if (attributesFromRelation != null) {
					relators.addAll(Arrays.asList(attributesFromRelation));
				}
			}

			AttributedObject[] relatorsArr = new AttributedObject[relators.size()];
			relatorsArr = relators.toArray(relatorsArr);
			return relatorsArr;
		}

	}

}
