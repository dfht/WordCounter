/**
 * 
 */
package com.dfht;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;

/**
 * @author Darren
 *
 */
public class CharsetHelper {
	
	
	static Charset guessCharset(String path) throws IOException {
		
		 try( FileInputStream input = new FileInputStream(path);
			  //Needs to be BufferedInputStream so markSupported is true.
		      BufferedInputStream bis = new BufferedInputStream(input); 
				 ){
			 return guessCharset(bis);  
		 }
	}
	
	//Public so the tests can use it.
	public static Charset guessCharset(InputStream stream) throws IOException {
		  CharsetDetector cd = new CharsetDetector();
		  cd.setText(stream);
		  CharsetMatch cm = cd.detect();
		  Charset charSet = null;
		  if (cm != null) {
			  try {
				  charSet = Charset.forName(cm.getName());
			  }
			 catch(IllegalArgumentException ex) {
				 //Just fall back on the default platform charset. 
			 }
		  }
		  if(charSet == null)
			  charSet = Charset.defaultCharset();
		  return charSet;
	}
	
	static Charset checkCharset(String name) throws InputException{
		Charset charset =  Charset.forName(name);
		if(charset == null)
	    	throw new InputException(Messages.InvalidEncoding(name));
		return charset;
	}

}
