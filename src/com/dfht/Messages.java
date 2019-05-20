package com.dfht;

import java.util.Map.Entry;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * I did the message strings as a resource bundle in case it needs to be i18n-ed.
 * @author Darren
 *
 */
public class Messages {
	private static final String BUNDLE_NAME = "com.dfht.messages"; //$NON-NLS-1$
	
	
	private static final String EnterPath = "EnterPath";
	private static final String NotAFile = "NotAFile";
	private static final String NoSuchFile = "NoSuchFile";	
	private static final String InvalidEncoding = "InvalidEncoding";
	private static final String ProblemEncountered = "ProblemEncountered";
	
	private static final String FormatWordCountSingle = "FormatWordCountSingle";
	private static final String FormatWordCountPlural = "FormatWordCountPlural";
	
	private static final String Usage = "Usage";
	
	
	static final String EnterPath() {
		return getString(EnterPath);
	}

	static final String NotAFile(String path) {
		return  formattedMessage((NotAFile), path);
	}
	
	static final String NoSuchFile(String path) {
		return formattedMessage((NoSuchFile), path);
	}

	static final String InvalidEncoding(String encoding) {
		return formattedMessage(InvalidEncoding, encoding);		
	}
	static final String ProblemEncountered(String message) {
		return formattedMessage(ProblemEncountered, message);
	}
	
	private static final String formattedMessage(String key, Object... parameters) {
		return String.format(getString(key), parameters);		
	}
	
	
	static final String FormatWordCount(Entry<String, Long> entry) {
		long l = entry.getValue();
		String key = l > 1 ? FormatWordCountPlural  : FormatWordCountSingle;
		return formattedMessage(key, entry.getKey(), entry.getValue());
	}
	
	static final String Usage(String help, String file ,String encoding) {
		return formattedMessage(Usage, help, file ,encoding);
	}
	
	

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

	private Messages() {
	}

	private static final String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
