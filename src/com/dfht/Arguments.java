package com.dfht;

import static java.util.stream.Collectors.joining;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Encapsulate command line argument parsing.
 * I also deal with gathering input from the command line here.
 * @author Darren
 *
 */
public class Arguments{
	
	
	//Two simple optional arguments.
	//Not worth bringing in a library for argument processing in such a small example.
	private static final String FILE_ARG = "file";
	private static final String ENCODING_ARG = "encoding";
	private static final String HELP_ARG = "help";

	private static final Pattern ARG_REGEX;
	private static final Pattern SWITCH_REGEX;
	
	
	private static final String[] SWITCHES = new String[] {HELP_ARG};
	   
	private static final String[] ARGS = new String[] {FILE_ARG, ENCODING_ARG,};
    static {
    	//Cache the arguments regex. It captures values in a group.
    	String pattern = '-' + Arrays.stream(ARGS).collect(joining("|", "(?<key>", ")")) + "\\=(?<value>.+)";
    	ARG_REGEX = Pattern.compile(pattern);
    	
    	//Command line 'switches' are just -key. Still it's elegant to capture them via regex.
        pattern = '-' + Arrays.stream(SWITCHES).collect(joining("|", "(?<key>", ")"));
        SWITCH_REGEX = Pattern.compile(pattern); 	
    }
	
	private boolean doingHelp = false;
	private String filePath;
	private Charset encoding;
	
	
	
	
	
	private static void checkFile(String path) throws InputException {
		//For user experience it is better to check the file before proceeding to ask about character sets etc.!
		File f = new File(path);
		if(!f.exists())
			throw new InputException(Messages.NoSuchFile(path));
		if(!f.isFile())
			throw new InputException(Messages.NotAFile(path));
	}
	
	
	
	
	static String usageMessage() {
		return Messages.Usage(Arguments.HELP_ARG, Arguments.FILE_ARG, Arguments.ENCODING_ARG);
			
	}
	
	private Arguments(boolean doHelp, String filePath, Charset encoding) {
		super();
		this.doingHelp = doHelp;
		this.encoding = encoding;
		this.filePath = filePath;
	}
	
	/**
	 * The proper API for parsing arguments. 
	 * This one checks that the file actually exists.
	 * @param args
	 * @return
	 * @throws InputException
	 */
	public static Arguments from(String... args) throws InputException {
		
		return from(true, args);
	}
	/**
	 * 
	 * @param checkFile - USeful so I can just test the parsing of arguments without the file 
	 * having to exist.
	 * @param args
	 * @return
	 * @throws InputException
	 */
	public static Arguments from(boolean checkFile, String... args) throws InputException {
		boolean doHelp = false;
		Charset encoding = null;
		String filePath = null;	
		//Check the file path and encoding if specified for early as possible failure (better user experience etc.).
		outerloop:
		for (String arg: args) {
			Matcher matcher = ARG_REGEX.matcher(arg);
			if(matcher.matches()) {	
				String key = matcher.group("key");
				String value = matcher.group("value");
				switch (key) {
				case FILE_ARG:
					filePath = value;
					if(checkFile)
						checkFile(filePath);
					break;
				case ENCODING_ARG:
					encoding = CharsetHelper.checkCharset(value);
					break;
				}
			}
			else {
				matcher = SWITCH_REGEX.matcher(arg);
				if(matcher.matches())
				{
					String key = matcher.group("key");
					switch (key) {
					case HELP_ARG:
						doHelp = true;
						//Help short circuits it all here as presence of help argument 
						//means I don't bother processing the other arguments.
						break outerloop;
					}
				}
			}
		}
		return new Arguments(doHelp, filePath, encoding);	
	}

	public boolean isDoingHelp() {
		return doingHelp;
	}

	public String getFilePath() {
		return filePath;
	}

	public Charset getEncoding() {
		return encoding;
	}
	
	//Fill in (by command line input) anything not passed in as an argument. Also, check anything that is entered.
	void complete() throws IOException, InputException{
		//If a fie path is not provided then ask on the command line.
		if(filePath == null) {
			//Try with resources so scanner is closed.
		    try(Scanner scanner = new Scanner(System.in)) {
		    	//First ask for the path (if not specified).
		    	if(filePath ==null) {
			    	
			    	System.out.print(Messages.EnterPath()); 
			    	String path = scanner.next();
			    	checkFile(path);
			    	this.filePath = path;
		    	}
		    }
		}
		//if an encoding isn't provided, guess it from the BOM via icu4j (falling back on platform default).
		if(encoding == null) {
	    		//Hmmm, what character set should I use here? I could assume it, or just allow the user to enter it.....	
		    	//In fact I will just use icu4j to try and guess it from the BOM if its not provided.
				this.encoding = CharsetHelper.guessCharset(this.filePath);
	    }
	}
}