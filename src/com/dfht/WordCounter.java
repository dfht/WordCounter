package com.dfht;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Stream;
/**
 * The big question/ambiguity is 'What is a word?'.
 * Also, what assumptions can be made about character encodings.
 * I have elected to make it able to deal with more character sets 
 * by use of icu4j, but of course this brings in an extra dependency and 
 * makes it slightly harder to distribute.
 * However I wrap it into a single jar via the Maven assembly plug-in, so 
 * its no big deal.
 * 
 * @author Darren
 *
 */
public class WordCounter {


	
	//What is a word?
	// The regex \p{L} matches any letter from any language (including with 'diacritics' i.e. accents etc.).
	//However, apostrophe's or hyphenated words bring further questions........
	//Also, I could consider numbers as well (\p{N}).
	//I could have gone with \w for letters (in Java regex this is [A-Za-z0-9_] i.e. ASCII characters) 
	//with \W as the negation ([^\w]).
	//I could equally well have said any sequence of characters other than whitespace (\S+). The latter has 
	//the problem of ignoring punctuation and being perhaps overly general.
	//Other possibilities are to include a list of punctuation characters to include along with \s as valid separators.
	//E.g. [\s\,\.\:\;\?'"\(\)\+\=\!]+
	//However, this then leads to questions about what that list should be and how punctuation works in 
	// other languages....
	
	//See https://www.regular-expressions.info/unicode.html for a discussion on finer points of unicode
	//categories etc.
	//I could use this for a 'letter' \p{L}\p{M}*+ but I don't think I need this as I deal in strings 
	//only and Java regex normalises (so I don't ever have the double character representation for 'diacritics' etc.).
	private static String WORD_SEPARATORS_REGEX = "\\P{L}+"; //(same as [^\\p{L}]+);
			//"\\W+";
		
	
	
	
	private static int orderEntries(Entry<String, Long> a, Entry<String, Long> b) {
		//I can now use Java 8 streams to sort it by value as well.......
		//However, to guarantee identical results (identical reports) regardless of the order of words in the file I 
		//need a deterministic rule to sort out which comes first when two strings occur an equal number of times.
		int anInt = b.getValue().compareTo(a.getValue());
		if(anInt == 0) {
			//For a deterministic ordering, deal with tie-breaks by making the
			//'first' string in lexicographical order appear first (the default ordering of strings).
			anInt = a.getKey().compareTo(b.getKey());
		};
		return anInt;
	}
	
	
	
	/**
	 * Create an amalgamated string report.
	 * @param lines - A stream with of lines (usually derived from a file).
	 * @return
	 */
	public static String wordCountReport(Stream<String> lines) {
		return processedLines(lines).collect(joining("\\n"));
	}
	
	
	/**
	 * For testing purposes, return a list to compare report output.
	 * @param lines - A stream with of lines (usually derived from a file).
	 * @return
	 */
	public static List<String> wordCountReportCollection(Stream<String> lines) {
		return processedLines(lines).collect(toList());
	}
	
	/**
	 * Output the report of word counts to System.out.
	 * Note that for 'exotic' characters (none Latin) these may not appear correctly 
	 * in System.out due to OS configuration regarding the console etc.
	 * This is not a fault of this application.
	 * @param lines - A stream with of lines (usually derived from a file).
	 */
	public static void wordCountReportToSystemOut(Stream<String> lines)  {
		processedLines(lines).forEach(System.out::println);
	}
	
	
	
	
	
	//Takes a stream of lines (e.g .from a file) and
	//returns a Stream with the ordered lines (with word and number of occurrences) to print. 
	//Collectors, other terminal operations, further pipeline operations etc. 
	//can decide what to do with the resulting stream
	//(print to system.out, gather a collection, create a report etc.).
	private static Stream<String> processedLines(Stream<String> lines) {
		
		//First I count the words. 
		//I do this in processCounts to produce a map of words to the number of occurrences.
		//Then I process this into an ordered report stream in orderedlinesToPrint. 
		Map<String, Long> counts = processedCounts(lines);
		return orderedLinesToPrint(counts);
		
		/* 
		 * NOTE - I could have done the whole thing as a single line, but for testing 
		 * I split the count (processCounts) and ordereing parts (orderedLinesToPrint).
		 *
		 * With the beauty of Java 8 streams, collectors and lambda expressions this could all be done in a single line,
		 * which I think is very nice. Here's how the single line would look.
		return lines.map(line -> line.split(WORD_SEPARATORS_REGEX))
			 .flatMap(Arrays::stream)
			 .collect(groupingBy(Function.identity(), counting()))
			 .entrySet().stream().sorted((a, b) -> orderEntries(a, b))
			 .map(entry -> Messages.FormatWordCount(entry));
		*/
	}
	
	
	
	/**
	 * Takes the map of word counts and produces a stream with the lines in order (most frequent word 
	 * first - then alphabetical for tie-breaks).
	 * @param counts
	 * @return
	 */
	public static Stream<String> orderedLinesToPrint(Map<String, Long> counts){
		//Stream the maps entrySet and sort it by number of occurrences, using strings lexicographical ordering 
		//to deterministically order 'words' with the same number of occurrences (for consistent output between runs etc.).
		//Finally I map the sorted entries to output strings. I use a Java resource bundle for 
		//this so that the final format can be internationalised AND easily changed.
		//I output a stream of the lines of the report in the correct order.
		return counts.entrySet().stream().sorted((a, b) -> orderEntries(a, b))
		 .map(entry -> Messages.FormatWordCount(entry));	
	}
	
	
	
	
	/**
	 * The basic algorithm for counting.
	 * Takes a stream of all of the lines from the input stream (usually a file) 
	 * and counts the occurrences of each word.
	 * I expose this publicly so the tests can check that correct results are obtained.
	 * @param lines
	 * @return
	 */
	public static Map<String, Long> processedCounts(Stream<String> lines) {
		
		//Lambda expression to map the lines into Stream<String[]> (via split) then flatMap to turn into 
		//a stream of all the 'words' in the file..
		//Then I use Collectors.groupingsBy and Collectors.counting to count the occurrences and 
		//collect into a map keyed by the 'words'(values being the the number of occurrences).
		
		//As an aside, I could do parallel to process the lines in parallel via ForkJoin etc.
		//If so i'd have lines.parallel() instead of lines at the beginning, and change the groupingBy 
		//to be groupingByConcurrent.
		
		//With the beauty of Java 8 streams, collectors and lambda expressions this can all be done in a single line,
		//which I think is very nice.
		return lines.map(line -> line.split(WORD_SEPARATORS_REGEX))
			 .flatMap(Arrays::stream)
			 .collect(groupingBy(Function.identity(), counting()));
	}
}
