package edu.washington.cs.rtrefactor.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import com.google.common.base.Joiner;
import com.google.common.io.Files;

import edu.washington.cs.rtrefactor.detect.SourceRegion;

/**
 * Static helper methods for working with file
 * @author Travis Mandel, Todd Schiller
 */
public abstract class FileUtil {

	/**
	 * Read the contents of a file to a string, using the default charset
	 * provided by {@link Charset#defaultCharset()}. Lines are joined with
	 * with the "line.separator" property fetched from {@link System#getProperty(String)}.
	 * @param file the file
	 * @return the contents of the file
	 * @throws IOException If file does not exist or is unreadable
	 */
	public static String read(File file) throws IOException{
		return Joiner.on(System.getProperty("line.separator")).join(Files.readLines(file, Charset.defaultCharset()));
	}
	
	/**
	 * Get the text within the region from a file
	 * @param content the contents of a document or file
	 * @param region the region in the content to retreive
	 * @return the text within the region
	 */
	public static String get(String content, SourceRegion region){
		return content.substring(region.getStart().getGlobalOffset(), region.getEnd().getGlobalOffset());
	}
	
	public static String shiftLeft(String orig)
	{
		//String stripped = orig.r
		String[] lines = orig.split("\n");
		int minSpaces = -1;
		for(int i=0; i<lines.length; i++) {
			int spaces = 0;
			if(lines[i].trim().length() == 0) {
				continue;
			}
			lines[i] = shiftTagToEnd(lines[i]);
			while(spaces < lines[i].length() &&
					Character.isWhitespace(lines[i].charAt(spaces)) ) {
				spaces++;
				
			}
			minSpaces = (spaces < minSpaces || (minSpaces < 0)) ? spaces : minSpaces;
		}
		String result = "";
		for(int i=0; i<lines.length; i++) {
			
			if(lines[i].trim().length() == 0) {
				result += lines[i] + "\n";
			} else {
				result += lines[i].substring(minSpaces) + "\n";
			}
		}
		
		return result;
		
	}
	
	private static String shiftTagToEnd(String s){
		int off = 0;
		while(off < s.length() &&
				Character.isWhitespace(s.charAt(off)) ) {
			off++;
		}
		int startOff = off;
		if(s.charAt(off) == '<')
		{
			off++;
			while(s.charAt(off) != '>')
				off++;
			int tagEndOff = off;
			off++;
			
			while(off < s.length() &&
					Character.isWhitespace(s.charAt(off)) ) {
				off++;
			}
			String ret = s.substring(0, startOff) +  s.substring(tagEndOff+1, off) + 
					s.substring(startOff, tagEndOff+1) + s.substring(off);
			return ret;
		}
		
		return s;
	}
	
}

