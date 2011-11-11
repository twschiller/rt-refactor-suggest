package edu.washington.cs.rtrefactor.detect;

import java.io.File;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

/**
 * A location in a source file. 
 * Stores two representations: line and offset in that line or global offset 
 * 		into the file.
 *   
 * @author Todd Schiller
 * @author Travis Mandel
 */
public class SourceLocation implements Comparable<SourceLocation>{

	private File file;
	private int line;
	private int offset;
	private int globalOffset;
	
	/**
	 * Construct a record describing a location in a source file, given
	 * the line and line specific offset.  
	 * 
	 * Performs and stores the conversion to global offset form. 
	 * 
	 * @param file the source file, or file underlying the buffer (non-null)
	 * @param line the line number (>= 0)
	 * @param offset the offset on the line (>= 0)
	 * @param document the source document buffer (non-null)
	 */
	public SourceLocation(File file, int line, int offset, IDocument document) {
		super();
		
		if (file == null){
			throw new NullPointerException("Attempt to create source location with no associated source file");
		}else if (line < 0){
			throw new IllegalArgumentException("Attempt to create source location with a negative line number");
		}else if (offset < 0){
			throw new IllegalArgumentException("Attempt to create source location with a negative line offset");	
		} else if (document == null){
			throw new NullPointerException("Attempt to create source location with no associated source document");
		}
		
		this.file = file;
		this.line = line;
		this.offset = offset;
		convertLineOffset(document);
	}
	
	/**
	 * Construct a record describing a location in a source file, given the global 
	 * 	offset into the file.
	 * 
	 * Performs and stores the conversion to line-offset form.
	 * 
	 * @param file the source file, or file underlying the buffer (non-null)
	 * @param globalOffset the global offset in the file (>= 0)
	 * @param document the source document buffer (non-null)
	 */
	public SourceLocation(File file, int globalOffset, IDocument document) {
		super();
		
		if (file == null){
			throw new NullPointerException("Attempt to create source location with no associated source file");
		}else if (globalOffset < 0){
			throw new IllegalArgumentException("Attempt to create source location with a negative line offset");	
		} else if (document == null){
			throw new NullPointerException("Attempt to create source location with no associated source document");
		}
		
		this.file = file;
		this.globalOffset = globalOffset;
		convertGlobalOffset(document);
	}
	
	/**
	 * get the source file, or the file underlying the buffer
	 * @return the source file, or the file underlying the buffer
	 */
	public File getFile() {
		return file;
	}
	
	/**
	 * get the the line number
	 * @return the line number
	 */
	public int getLine() {
		return line;
	}
	
	/**
	 * get the offset on the line
	 * @return the offset on the line
	 * @see getLine
	 */
	public int getOffset() {
		return offset;
	}
	
	/**
	 * get the offset in the file
	 * @return the offset in the file
	 * @see getLine
	 */
	public int getGlobalOffset() {
		return globalOffset;
	}


	@Override
	/**
	 * {@inheritDoc}
	 */
	public int compareTo(SourceLocation o) {
		if (o == null){
			throw new NullPointerException("Attempt to compare source location to null");
		}else if(!file.equals(o.file)){
			throw new IllegalArgumentException("Cannot compare source locations with different underlying files");
		}
		
		if (line == o.line){
			return new Integer(offset).compareTo(o.offset);
		}else{
			return new Integer(line).compareTo(o.line);
		}
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((file == null) ? 0 : file.hashCode());
		result = prime * result + line;
		result = prime * result + offset;
		return result;
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SourceLocation other = (SourceLocation) obj;
		if (file == null) {
			if (other.file != null)
				return false;
		} else if (!file.equals(other.file))
			return false;
		if (line != other.line)
			return false;
		if (offset != other.offset)
			return false;
		return true;
	}
	
	
	/**
	 * Given a document, computed and stores this location's
	 * global offset based on its line and line offset
	 * 
	 * @param doc The document which this SourceLocation is in
	 */
	private void convertLineOffset(IDocument doc)
	{
		int off = 0;
		try {
			if(line == 0)
				off = 0;
			else
				off = doc.getLineOffset(line-1);
		} catch (BadLocationException e) {
			e.printStackTrace();
			globalOffset = 0;
			return;
		}

		off += offset;
		globalOffset = off;
	}
	
	/**
	 * Given a document, compute this location's line and offset based on
	 * its global offset
	 * 
	 * @param doc the document this SourceLocation is in
	 */
	private void convertGlobalOffset(IDocument doc)
	{
		int line =0, newOffset =0;
		try {
			line = doc.getLineOfOffset(globalOffset);
			int lineOff;
			if(line == 0)
				lineOff = 0;
			else
				lineOff = doc.getLineOffset(line-1);
			newOffset = globalOffset - lineOff;
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		this.line = line;
		this.offset = newOffset;
	}
}
