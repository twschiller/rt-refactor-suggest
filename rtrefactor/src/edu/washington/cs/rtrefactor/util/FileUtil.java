package edu.washington.cs.rtrefactor.util;

import java.io.File;
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
	 */
	public static String read(File file){
		// TODO this method should really have checked exceptions.
		
		try{
			return Joiner.on(System.getProperty("line.separator")).join(Files.readLines(file, Charset.defaultCharset()));
		}catch(Exception e){
			throw new RuntimeException(e);
		}
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
	
}
