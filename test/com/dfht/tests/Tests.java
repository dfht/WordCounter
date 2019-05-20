package com.dfht.tests;

import static java.util.stream.Collectors.toList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Stream;

import org.junit.Test;

import com.dfht.Arguments;
import com.dfht.CharsetHelper;
import com.dfht.InputException;
import com.dfht.WordCounter;

/**
 * A few tests.
 * Basically just test it runs without exceptions and that shuffling the order round gives the 
 * same results.
 * Perhaps also a basic test on a very small file with known values would be sensible.
 * @author Darren
 *
 */
public class Tests {
	
	//A basic default character set file of words.
	private static String EXAMPLE_FILE = "example.txt";
	//Arbitrarily a different encoding to test.
	private static String EXAMPLE_UF16_FILE = "utf16Example.txt";
	
	
	//Different language characters (non-ASCII, diacritics/accents and 'exotic' characters).
	private static String DANISH_UTF8 = "danishUTF8.txt";
	private static String CHINESE_UTF8 = "chineseUTF8.txt";
	private static String MIXED_UTF8 = "variousUTF8.txt";
	
	//I use this for a test of the overall counting algorithm (testBasicExpectedResultsForCountAlgorithm);
	private static String[] WHITE_SPACE = {"\n", "\t", " "};
	
	
	
	/**
	 * Test argument parsing,
	 * @throws IOException
	 * @throws InputException
	 */
	@Test
	public void testArgumentParsing() throws IOException, InputException{
		
		
		Arguments args = Arguments.from(false, "-help");
		assert args.isDoingHelp() : "Incorrect argument parsing";
		assert args.getFilePath() == null : "Incorrect argument parsing";
		assert args.getEncoding() == null : "Incorrect argument parsing";
		
		String expectedPath = "C:\\Program Files\\blob.txt";
		String pathWithSpaces = "-file=" + expectedPath;
		String expectedEncoding = "UTF-8";
		String encoding = "-encoding=" + expectedEncoding;
		
		
		args = Arguments.from(false, "-help",  pathWithSpaces, encoding);
		assert args.isDoingHelp() : "Incorrect argument parsing";
		//These should be null as I intend it to short-circuit and ignore them in this case.
		assert args.getFilePath() == null : "Incorrect argument parsing";
		assert args.getEncoding() == null : "Incorrect argument parsing";
	
		
		args = Arguments.from(false, pathWithSpaces,  encoding);
		
		
		assert !args.isDoingHelp() : "Incorrect argument parsing";
		//These should be null as I intend it to short-circuit and ignore them in this case.
		assert expectedPath.equals(args.getFilePath()): "Incorrect argument parsing";
		assert Charset.forName(expectedEncoding).equals(args.getEncoding()) : "Incorrect argument parsing";
	}
	
	
	
	
	
	/**
	 * Test basic running without exceptions of a simple file in default platform character encoding.
	 * @throws IOException
	 */
	@Test
	public void testBasicFile() throws IOException{
		testBasicFile(EXAMPLE_FILE);
	}
	
	/**
	 * Test basic running without exceptions for a UTF-16 encoded file.
	 */
	@Test
	public void testUTF16File() throws IOException{
		testBasicFile(EXAMPLE_UF16_FILE);	
	}
	
	
	
	

	/** 
	 * Test basic running without exceptions.
	 * of a UTF-8 encoded file with Chinese characters etc.
	 * 
	 */
	@Test
	public void testChinese() throws IOException{
		testBasicFile(CHINESE_UTF8);
	}
	
	
	/**
	 * Test basic running without exceptions.
	 * of a UTF-8 encoded file with characters with Danish characters 
	 * and accents.
	 * @throws IOException
	 */
	@Test
	public void testDanish() throws IOException{
		testBasicFile(DANISH_UTF8);
	}
		
	/**
	 * * Test basic running without exceptions.
	 * of a UTF-8 encoded file with characters characters from 
	 * various 'alphabets'.
	 * and accents.
	 * @throws IOException
	 */
	@Test
	public void testMixed() throws IOException{
		testBasicFile(MIXED_UTF8);
	}
		
	
	
	
	
	
	private void testBasicFile(String path) throws IOException{
		try(
			InputStream stream = Tests.class.getResourceAsStream(path);
			Stream<String> lines = acquireStream(stream);
			){
			WordCounter.wordCountReportToSystemOut(lines);
		}
	}
	
	
	private static <Z> List<Z> shuffledCopy(List<Z> to) {
		//I need an array that I can definitely call set on and i can't guarantee the 
		//implementation of the one passed in (so I make an ArrayList here).
		List<Z> toShuffle = new ArrayList<Z>(to);
		//The JDK has a nice shuffle method that I think uses the Fisher-Yates shuffle algorithm and
		//is O(n) (i.e. runs in linear time w.r.t. the length of the collection).
		//For true random shuffling (certainly overkill here) you can use the JSON RPC API exposed by Random.org
		//(see https://api.random.org/json-rpc/2/basic).
		Collections.shuffle(toShuffle);
		return toShuffle;
	}
	
	
	
	/**
	 * Test same report regardless of order of words in the file for a simple file in default platform character encoding.
	 * @throws IOException
	 */
	@Test
	public void testShuffleBasic() throws IOException{
		testShuffle(EXAMPLE_FILE);
	}
	
	
	/**
	 * Test same report regardless of order of words in the file for a UTF-16 encoded file.
	 * @throws IOException
	 */
	@Test
	public void testShuffleUTF16() throws IOException{
		testShuffle(EXAMPLE_UF16_FILE);	
	}
	
	
	
	/**
	 * Test same report regardless of order of words in the file for a UTF-8 encoded file 
	 * with Chinese characters.
	 * @throws IOException
	 */
	@Test
	public void testChineseShuffle() throws IOException{
		testShuffle(CHINESE_UTF8);
	}
		
	/**
	 * Test same report regardless of order of words in the file for a UTF-8 encoded file 
	 * with Danish characters and 'diacritics'.
	 * @throws IOException
	 */
	@Test
	public void testDanishShuffle() throws IOException{
		testShuffle(DANISH_UTF8);
	}
			
	/**
	 *  Test same report regardless of order of words in the file for a UTF-8 encoded file 
	 *  with characters from various 'alphabets'.
	 * @throws IOException
	 */
	@Test
	public void testMixedShuffle() throws IOException{
		testShuffle(MIXED_UTF8);
	}
			
	
	
	
	
	
	
	
	private static Stream<String> acquireStream(InputStream stream ) throws IOException{
		//Shuffle the lines.
		Stream<String> lines = new BufferedReader(new InputStreamReader(stream, CharsetHelper.guessCharset(stream))).lines();
		return lines;
	}
	
	
	
	private void testShuffle(String path) throws IOException{		
		    List<String> report = null;
		    List<String> reportShuffled = null;
		    //Test I get the same results if line orders shuffled.
			try(
					
					InputStream stream = Tests.class.getResourceAsStream(path);
					Stream<String> lines = acquireStream(stream);
					//Need a separate stream for shuffling as a stream can only be 'operated upon' once.
					){
				report = WordCounter.wordCountReportCollection(lines);
				assert report != null && !report.isEmpty() : "The report is null or empty!";
				}
			try(
					//Shuffle the lines.
					InputStream stream = Tests.class.getResourceAsStream(path);
					Stream<String> lines = acquireStream(stream);
					Stream<String> shuffled = shuffledCopy(lines.collect(toList())).stream();
					)
			{
				reportShuffled = WordCounter.wordCountReportCollection(shuffled);
				assert reportShuffled != null && !reportShuffled.isEmpty() : "The shuffled report is null or empty!";
			}
			
			assert report.equals(reportShuffled) : "The shuffled report gives a different result!";
		}
	
	
	
	
	private  static int random(Random random, int min, int max) {
		return min + random.nextInt(max - min + 1);
	}
	
	
	/**
	 * Test the answer for a simple content when the answer is known beforehand.
	 * @throws IOException
	 */
	@Test
	public void testBasicExpectedResultsForCountAlgorithm() throws IOException{
		String[] WORDS = {"hello", "goodbye", "time", "day", "what", "where", "am", "I", "is", "east", "west", "north", "south", "country", "city", "continent", "town", "village"};
		testBasicExpectedResultsForCountAlgorithm(WORDS, 100);	
	}
	
	
	
	
	//Make a 'random' content with each of the words appearing a random number of times up to maxOccurence 
	//and separated by a 'random' whitespace character.
	//Check that the word count comes back as expected.
	//This tests the counting algorithm.
	private static void testBasicExpectedResultsForCountAlgorithm(String[] words, int maxOccurence) throws IOException{
		Random random = new Random();
		Map<String, Long> frequencies = new HashMap<>(words.length);
		//Make a collection of all the words (each one occurring a random number of times between 0 and 100). 
		//Add 'random' white space between each one.
		//Then jumble the lot up and write it to a String.
		//Then try and process this and see that it comes back with the expected answer. 
		List<String> allWords = new ArrayList<String>();
		for (String w : words) {
			//A 'Random' integer between 0 and 100.
			long howMany = random(random, 0, maxOccurence);
			frequencies.put(w, howMany);
			for (int i = 0; i < howMany; i++) {
				allWords.add(w);
			};
		};
		//Now jumble up the words and insert 'random' whitespace between them.
		List<String> shuffled = shuffledCopy(allWords);
		StringBuilder b = new StringBuilder();
		//Now build up a combined string. 
		shuffled.stream().forEach(word -> {
			b.append(word);
			int wsIndex = random(random, 0, WHITE_SPACE.length - 1);
			//A random whitespace between each word.
			String whitespace = WHITE_SPACE[wsIndex];
			b.append(whitespace);
			
		});
		
		BufferedReader reader = new BufferedReader(new StringReader(b.toString()));
		Map<String, Long> computedFrequencies = WordCounter.processedCounts(reader.lines());
		assert frequencies.equals(computedFrequencies) : "Incorrect word count computed";
	}
	

}
