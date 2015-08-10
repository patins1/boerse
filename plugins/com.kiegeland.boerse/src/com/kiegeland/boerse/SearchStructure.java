/**
 * Copyright (C) 2015 by Joerg Kiegeland
 */
package com.kiegeland.boerse;

public class SearchStructure {

	private final String string;

	int index = 0;

	public SearchStructure(String string) {
		this.string = string;
	}

	public String findNext(String startsWith, String endsWith) {
		if (index == -1)
			throw new RuntimeException("Already failed findNext cannot be resumed!");
		index = string.indexOf(startsWith, index);
		if (index == -1) {
			return null;
		}
		index += startsWith.length();
		int beginIndex = index;
		index = string.indexOf(endsWith, index);
		if (index == -1) {
			return null;
		}
		String result = string.substring(beginIndex, index);
		index += endsWith.length();
		return result;
	}

}
