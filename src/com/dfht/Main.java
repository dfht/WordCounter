package com.dfht;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

/**
 * 
 * @author Darren
 *
 */
public class Main {
	
	
	
	 public static void main(String[] args) {
			
			try {
				Arguments arguments = Arguments.from(args);
	    		//If its just help then output the usage message.
				if(arguments.isDoingHelp()) {
					String message = Arguments.usageMessage();
					System.out.print(message);
				}
				else {
					//Fill in anything not specified by engaging with the user.
					arguments.complete();
					//Proceed to process the file.
					String path = arguments.getFilePath();
					Charset charset = arguments.getEncoding();	;
					process(path, charset);
				}	
			}
		    catch ( IllegalArgumentException | NoSuchElementException | IllegalStateException | IOException | InputException ex) {
		    	String errorMessage = ex.getLocalizedMessage();	
		    	errorMessage = errorMessage == null ? ex.getClass().getSimpleName() : errorMessage;
		    	String messageToDisplay = Messages.ProblemEncountered(errorMessage);
		    	System.out.print(messageToDisplay); 	
			}	    
	}
	 
	 
	 private static void process(String path, Charset charset) throws IOException{		
	     //Try with resources to close the stream. 
	     //It doesn't matter what order I process the lines.
	      try(Stream<String> stream = Files.lines(Paths.get(path), charset)){  	  
	    	  WordCounter.wordCountReportToSystemOut(stream);
	     }
	}

}
