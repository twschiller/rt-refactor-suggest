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
	
	
}
