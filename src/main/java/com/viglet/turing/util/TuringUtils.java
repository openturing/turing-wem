package com.viglet.turing.util;

import java.util.List;

public class TuringUtils {
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
	
	public static boolean isTuringTag(String tagName) {
		return (tagName.equals("turingSentimentTone") || tagName.equals("turingGL") || tagName.equals("turingON")
				|| tagName.equals("turingPN") || tagName.equals("turingSentimentSubj")
				|| tagName.equals("turingSimpleConcept"));
	}

	public static boolean isSinlgeValueTMETag(String tagName) {
		return (tagName.equals("turingSentimentTone") || tagName.equals("turingSentimentSubj"));
	}

}
