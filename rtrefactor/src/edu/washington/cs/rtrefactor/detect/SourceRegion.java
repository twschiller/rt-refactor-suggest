package edu.washington.cs.rtrefactor.detect;

import java.io.File;

/**
 * A record describing a region in a file
 * @author Todd Schiller
 */
public class SourceRegion implements Comparable<SourceRegion>{

	private SourceLocation start;
	private SourceLocation end;
	
	public SourceRegion(SourceLocation start, SourceLocation end) {
		super();
		
		if (!start.getFile().equals(end.getFile())){
			throw new IllegalArgumentException("Source location file mismatch");
		}
		
		this.start = start;
		this.end = end;
	}
	
	/**
	 * Get the file the region is contained in, or the file underlying the buffer
	 * @return the file the region is contained in, or the file underlying the buffer
	 */
	public File getFile(){
		return start.getFile();
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
		
		return region.getStart().compareTo(getEnd()) <= 0 &&// other region begins before other this ends
			   region.getEnd().compareTo(getStart()) >= 0; // the other region ends after this begins 
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

	
}
