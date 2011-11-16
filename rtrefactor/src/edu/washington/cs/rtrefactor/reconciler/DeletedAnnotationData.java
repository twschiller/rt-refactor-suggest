package edu.washington.cs.rtrefactor.reconciler;

import org.eclipse.jface.text.Position;

/**
 * Stores data about a deleted annotation (& its marker)
 * @author Travis Mandel
 *
 */
public class DeletedAnnotationData {
	
	private final Position pos;
	private final int cloneNumber;
	
	/**
	 * Create data about a deleted annotation
	 * 
	 * @param pos  The position of the deleted annotation in the document just before it was 
	 * 		deleted
	 * @param cloneNumber The clone number associated with the marker
	 */
	public DeletedAnnotationData(Position pos, int cloneNumber) {
		super();
		this.pos = pos;
		this.cloneNumber = cloneNumber;
	}
	
	/**
	 * Get the position of the annotation in the document just before deletion
	 * @return the position of the annotation in the document just before deletion
	 */
	public Position getPos() {
		return pos;
	}
	
	/**
	 * Get the unique id number of clone pointed to by the deleted annotation
	 * @return the unique id number of clone pointed to by the deleted annotation
	 */
	public int getCloneNumber() {
		return cloneNumber;
	}
	

}
