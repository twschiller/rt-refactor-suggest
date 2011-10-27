package edu.washington.cs.rtrefactor.detect;

import java.io.File;

/**
 * A location in a source file
 * @author Todd Schiller
 */
public class SourceLocation implements Comparable<SourceLocation>{

	private File file;
	private int line;
	private int offset;
	
	/**
	 * Construct a record describing a location in a source file
	 * @param file the source file, or file underlying the buffer (non-null)
	 * @param line the line number (>= 0)
	 * @param offset the offset on the line (>= 0)
	 */
	public SourceLocation(File file, int line, int offset) {
		super();
		
		if (file == null){
			throw new NullPointerException("Attempt to create source location with no associated source file");
		}else if (line < 0){
			throw new IllegalArgumentException("Attempt to create source location with a negative line number");
		}else if (offset < 0){
			throw new IllegalArgumentException("Attempt to create source location with a negative line offset");	
		}
		
		this.file = file;
		this.line = line;
		this.offset = offset;
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
}
