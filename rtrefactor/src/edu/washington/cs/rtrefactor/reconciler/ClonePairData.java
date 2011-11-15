package edu.washington.cs.rtrefactor.reconciler;

import java.io.IOException;

import edu.washington.cs.rtrefactor.detect.SourceRegion;
import edu.washington.cs.rtrefactor.util.FileUtil;

/**
 * An augmented clone pair record
 * @author Todd Schiller
 */
public class ClonePairData {

	private final int cloneNumber;
	private final SourceRegion sourceClone;
	private final SourceRegion otherClone;
	private final String sourceContents;
	private final String otherContents;
	private final boolean sameFile;
	
	/**
	 * Construct an augmented clone pair record
	 * @param cloneNumber The clone pair number
	 * @param sourceClone The region containing the source, i.e. active, clone
	 * @param otherClone The region containing the system clone
	 * @param sourceContents The contents of the <i>entire</i> document containing {@code sourceClone}
	 * @param isSameFile Is the second clone in the same file as the first
	 * @throws IOException iff <code>other.getFile()</code> cannot be read
	 */
	public ClonePairData(int cloneNumber, SourceRegion sourceClone,
			SourceRegion otherClone, String sourceContents, boolean sameFile) throws IOException {
		super();
		this.cloneNumber = cloneNumber;
		this.sourceClone = sourceClone;
		this.otherClone = otherClone;
		this.sourceContents = sourceContents;
		this.otherContents = sameFile ? sourceContents :  FileUtil.read(otherClone.getFile());
		this.sameFile = sameFile;
	}

	/**
	 * @return the cloneNumber
	 */
	public int getCloneNumber() {
		return cloneNumber;
	}

	/**
	 * get the source region for the active clone
	 * @return the source region for the active clone
	 */
	public SourceRegion getSourceRegion() {
		return sourceClone;
	}

	/**
	 * get the system clone's source region
	 * @return the system clone's source region
	 */
	public SourceRegion getOtherRegion() {
		return otherClone;
	}

	/**
	 * return the contents of the <i>entire</i> source file, or dirty buffer 
	 * contents if the buffer has been modified. The source region is given by
	 * {@link ClonePairData#getSourceRegion()}
	 * @return the contents of the source
	 */
	public String getSourceContents() {
		return sourceContents;
	}

	/**
	 * return the contents of the <i>entire</i> file containing the
	 * the system clone. The source region is given by {@link ClonePairData#getOtherRegion()}.
	 * @return the contents of the file containing the system clone
	 */
	public String getOtherContents() {
		return otherContents;
	}

	/**
	 * true iff the clones reside in the same file, i.e., the regions 
	 * {@link ClonePairData#getSourceRegion() and {@link ClonePairData#getOtherRegion()} are in 
	 * the same file
	 * @return true iff the clones reside in the same file
	 */
	public boolean isSameFile() {
		return sameFile;
	}
}
