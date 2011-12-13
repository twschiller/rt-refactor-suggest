package edu.washington.cs.rtrefactor.detect;

import java.io.File;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;

/**
 * A record describing a region in a file
 * @author Todd Schiller
 */
public class SourceRegion implements Comparable<SourceRegion>{

	private SourceLocation start;
	private SourceLocation end;
	
	/**
	 * Construct a source region
	 * @param start the start of the region
	 * @param end the end of the region
	 * @throws IllegalArgumentException iff the files associated with the locations are not {@link File##equals(Object)}
	 * 		or the end location occurs before or at the start location
	 */
	public SourceRegion(SourceLocation start, SourceLocation end) {
		super();
		
		if (!start.getFile().equals(end.getFile())){
			throw new IllegalArgumentException("Source location file mismatch");
		}else if (start.getGlobalOffset() >= end.getGlobalOffset()){
			throw new IllegalArgumentException("Source region must be non-empty");
		}
		
		this.start = start;
		this.end = end;
	}
	
	/**
	 * Construct a source region
	 * @param file the source file containing the region
	 * @param document the contents of <code>file</code>
	 * @param start the start global offset
	 * @param end the end global offset
	 * @throws BadLocationException iff <code>start</code> or <code>end</code> is not valid
	 */
	public SourceRegion(File file, Document document, int start, int end) throws BadLocationException{
		this(new SourceLocation(file, start, document), new SourceLocation(file, end, document));
	}
	
	/**
	 * Get the file the region is contained in, or the file underlying the buffer
	 * @return the file the region is contained in, or the file underlying the buffer
	 */
	public File getFile(){
		return start.getFile();
	}
	
	/**
	 * Get the length of the source region, as determined by the difference in the
	 * global offset of the end point and start point
	 * @return the length of the source region
	 */
	public int getLength(){
		return end.getGlobalOffset() - start.getGlobalOffset();
	}
	
	/**
	 * Get the starting location
	 * @return the starting location
	 */
	public SourceLocation getStart() {
		return start;
	}
	
	/**
	 * The ending location
	 * @return the ending location
	 */
	public SourceLocation getEnd() {
		return end;
	}
	
	/**
	 * true iff this region overlaps the given region
	 * @param region the query
	 * @return true iff this region overlaps the given region
	 */
	public boolean overlaps(SourceRegion region){
		if (!getFile().equals(region.getFile())){
			return false;
		}
		
		//http://world.std.com/~swmcd/steven/tech/interval.html
		//[A, B) [X, Y)
		//X < B AND A < Y
		
		SourceLocation A = this.getStart();
		SourceLocation B = this.getEnd();
		SourceLocation X = region.getStart();
		SourceLocation Y = region.getEnd();
		
		return X.compareTo(B) < 0 && A.compareTo(Y) < 0; 
	}
	
	@Override
	public int compareTo(SourceRegion o) {
		if (getFile().equals(o.getFile())){
			if (start.equals(o.start)){
				return end.compareTo(o.end);
			}else{
				return start.compareTo(o.start);
			}
		}else{
			return getFile().compareTo(o.getFile());
		}	
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((end == null) ? 0 : end.hashCode());
		result = prime * result + ((start == null) ? 0 : start.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SourceRegion other = (SourceRegion) obj;
		if (end == null) {
			if (other.end != null)
				return false;
		} else if (!end.equals(other.end))
			return false;
		if (start == null) {
			if (other.start != null)
				return false;
		} else if (!start.equals(other.start))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getFile().getName() + " " 
				+ start.getLine() + ":" + start.getLineOffset() + " - "
				+ end.getLine() + ":" + end.getLineOffset();
	}
}
