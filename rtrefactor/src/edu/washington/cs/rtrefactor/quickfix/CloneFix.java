package edu.washington.cs.rtrefactor.quickfix;

import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IMarkerResolution;

import edu.washington.cs.rtrefactor.detect.SourceRegion;
import edu.washington.cs.rtrefactor.util.FileUtil;




/**
 * The superclass for all Clone quick fixes.
 * 
 * Allows custom descriptions, images, and relevances (scores).
 * 
 * @author Travis
 *
 */
public abstract class CloneFix implements IMarkerResolution, IJavaCompletionProposal{

	private final int cloneNumber;
	private final SourceRegion sourceCloneRegion;
	private final SourceRegion otherCloneRegion;
	private final String dirtyText;
	private final String otherContent;
	private final boolean sameFile;
	private final int relevance;
	
	/**
	 * Instantiates a clone quick fix
	 * 
	 * @param cNumber The clone pair number
	 * @param otherClone The region containing the second clone (possibly in 
	 * 		another file)
	 * @param dirtyContent The contents of the currently open file, containing 
	 * 		the first clone
	 * @param isSameFile Is the second clone in the same file as the first
	 * @param relevance A score from 10-100 indicating the relevance of this 
	 * 			suggestion
	 */
	public CloneFix(int cNumber, SourceRegion sourceClone, SourceRegion otherClone, String dirtyContent, 
			boolean isSameFile, int relevance) {
		this.cloneNumber = cNumber;
		this.sourceCloneRegion = sourceClone;
		this.otherCloneRegion = otherClone;
		this.dirtyText = dirtyContent;
		this.sameFile = isSameFile;
		this.otherContent = sameFile ? dirtyText :  FileUtil.readFileToString(otherRegion.getFile());
		this.relevance = relevance;
	}

	/**
	 * Gets the description of the clone quick fix
	 * @return A string displaying the clone
	 */
	public String getDescription() {
		String otherClone;
		otherClone = CloneFixer.getCloneString(otherRegion.getStart().getOffset(), 
				otherRegion.getEnd().getOffset(), otherContent);
		return otherClone;
	}

	@Override
	public Image getImage() {
		//TODO: Insert cool graphics here

		return null;
	}

	
	@Override
	public void apply(IDocument document) {
		// never called
	}
	
	@Override
	public Point getSelection(IDocument document) {
		// we don't need this feature
		return null;
	}
	
	@Override
	public String getAdditionalProposalInfo() {
		return getDescription();
	}
	
	@Override
	public String getDisplayString() {
		return getLabel();
	}
	
	@Override
	public IContextInformation getContextInformation() {
		// We don't need this feature
		return null;
	}
	
	@Override
	public int getRelevance() {
		return relevance;
	}
	
	protected SourceRegion getRegion(){
		return region;
	}
	
	protected int getCloneNumber() {
		return cloneNumber;
	}
	
	protected SourceRegion getOtherRegion() {
		return otherRegion;
	}

	protected boolean isSameFile() {
		return sameFile;
	}


}
