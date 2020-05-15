package org.spideruci.analysis.config.utils;

public class SystemProperties {

	public static String valueFor(final String propertyName) {
		final String value = System.getProperty(propertyName);
		return value;
	}
	
	public static boolean has(final String propertyName) {
		return System.getProperties().containsKey(propertyName);
	}

}
