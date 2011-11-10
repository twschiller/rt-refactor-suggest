package edu.washington.cs.rtrefactor.detect;

/**
 * A record describing a clone pair; the "order" of the cloned regions is insignificant
 * and is <b>not</b> guaranteed to be maintained
 * @author Todd Schiller
 */
public class ClonePair {

	// source regions are stored in a canonical order to improve efficiency
	
	private SourceRegion first; 
	private SourceRegion second;
	private double score;
	
	/**
	 * Create a clone pair record; the "order" of the cloned regions is insignificant
	 * and is <b>not</b> guaranteed to be maintained
	 * @param first one side of the pair
	 * @param second the other side of the pair
	 * @param score the quality of the match
	 */
	public ClonePair(SourceRegion first, SourceRegion second, double score) {
		super();
		
		if (first.compareTo(second) < 0){
			this.first = first;
			this.second = second;
		}else{
			this.first = second;
			this.second = first;
		}
		
		this.score = score;
	}

	/**
	 * Get the first element of the clone pair
	 * @return the first element of the clone pair
	 */
	public SourceRegion getFirst() {
		return first;
	}
	
	/**
	 * Get the second element of the clone pair
	 * @return the second element of the clone pair
	 */
	public SourceRegion getSecond() {
		return second;
	}
	
	/**
	 * Get the quality of the match
	 * @return the quality of the match
	 */
	public double getScore() {
		return score;
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((first == null) ? 0 : first.hashCode());
		result = prime * result + ((second == null) ? 0 : second.hashCode());
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
		ClonePair other = (ClonePair) obj;
		if (first == null) {
			if (other.first != null)
				return false;
		} else if (!first.equals(other.first))
			return false;
		if (second == null) {
			if (other.second != null)
				return false;
		} else if (!second.equals(other.second))
			return false;
		return true;
	}
}
