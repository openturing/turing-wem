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
}
