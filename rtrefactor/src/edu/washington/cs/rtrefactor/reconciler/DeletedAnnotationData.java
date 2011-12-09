package edu.washington.cs.rtrefactor.reconciler;

import java.io.File;

import org.eclipse.jface.text.Position;

/**
 * Stores data about a deleted annotation (& its marker)
 * @author Travis Mandel
 *
 */
public class DeletedAnnotationData {
	
	private final Position pos;
	private final int cloneNumber;
	private final File otherFile;
	private final int otherStart;
	private final int otherEnd;
	
	/**
	 * Create data about a deleted annotation
	 * 
	 * @param pos  The position of the deleted annotation in the document just before it was 
	 * 		deleted
	 * @param cloneNumber The clone number associated with the marker
	 */
	public DeletedAnnotationData(Position pos, int cloneNumber, File otherFile, int otherStart, int otherEnd) {
		super();
		this.pos = pos;
		this.cloneNumber = cloneNumber;
		this.otherFile = otherFile;
		this.otherStart = otherStart;
		this.otherEnd = otherEnd;
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
	
	/**
	 * Get the other file which this annotation pointed to 
	 * @return the other file which this annotation pointed to
	 */
	public File getOtherFile() {
		return otherFile;
	}
	
	/**
	 * Get the global start offset of the other clone
	 * @return the global start offset of the other clone
	 */
	public int getOtherStart() {
		return otherStart;
	}

	/**
	 * Get the global end offset of the other clone
	 * @return the global end offset of the other clone
	 */
	public int getOtherEnd() {
		return otherEnd;
	}
	

}
