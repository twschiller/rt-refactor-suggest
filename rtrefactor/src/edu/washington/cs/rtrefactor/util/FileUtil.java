package edu.washington.cs.rtrefactor.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

import edu.washington.cs.rtrefactor.detect.SourceLocation;

/**
 * Some helpful utility methods for dealing with files and documents
 *  
 * @author Travis Mandel
 *
 */
public class FileUtil {
	/**
	 * Given a File in the default charset, read it in to a String and return it.
	 *  
	 * @param path A path to a valid file in the default character set
	 * @return a String containing the entire contents of the File
	 */
	public static String readFileToString(File path)
	{
		StringBuilder content = new StringBuilder();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(path));
			
			String tmp;
			char[] buf = new char[1024];
			int read = 0;
			while((read = reader.read(buf)) >= 0)
			{
				tmp = String.valueOf(buf, 0, read);
				content.append(tmp);
				buf = new char[1024];
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return content.toString();
	}
	
	/**
	 * Given a source location, return the corresponding character 
	 * offset in the file
	 * 
	 * @param sl The source location to convert
	 * @return the offset into the file
	 */
	public static int convertSourceLocation(SourceLocation sl, IDocument doc)
	{
		int off;
		try {
			if(sl.getLine() == 0)
				off = 0;
			else
				off = doc.getLineOffset(sl.getLine()-1);
		} catch (BadLocationException e) {
			e.printStackTrace();
			return 0;
		}

		off += sl.getOffset();

		return off;
	}
	
	/**
	 * Given a character offset into the current document, returns a new 
	 * SourceLocation representing the same location.
	 * 
	 * @param offset An offset into the current document
	 * @return The corresponding SourceLocation
	 */
	public static SourceLocation convertOffset(int offset, IDocument doc, File file)
	{
		int line =0, newOffset =0;
		try {
			line = doc.getLineOfOffset(offset);
			int lineOff;
			if(line == 0)
				lineOff = 0;
			else
				lineOff = doc.getLineOffset(line-1);
			newOffset = offset - lineOff;
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		return new SourceLocation(file, line, newOffset);
	}
}
