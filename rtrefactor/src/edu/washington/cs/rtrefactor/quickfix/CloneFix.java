package edu.washington.cs.rtrefactor.quickfix;

import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IMarkerResolution;

import edu.washington.cs.rtrefactor.Activator;
import edu.washington.cs.rtrefactor.detect.SourceRegion;
import edu.washington.cs.rtrefactor.reconciler.ClonePairData;


/**
 * The superclass for all Clone quick fixes.
 * 
 * Allows custom descriptions, images, and relevances (scores).
 * 
 * NOTE: getLabel() and run() should NOT be called if this fix has no parent.
 * 
 * @author Travis Mandel
 * @author Todd Schiller
 *
 */
public abstract class CloneFix implements IMarkerResolution, IJavaCompletionProposal{

    protected static boolean DEBUG_MODE = true;
    
	private final ClonePairData pairData;
	private final int relevance;
	private final CloneResolutionGenerator parent;
	
	
	/**
	 * Instantiates a clone clone quick fix
	 * @param pairData The clone pair data
	 * @param relevance A score from 10-100 indicating the relevance of this suggestion
	 * @param parent The parent CloneFixer (can be null)
	 */
	public CloneFix(ClonePairData pairData, int relevance, CloneResolutionGenerator parent){
		if (relevance < 10 || relevance > 100){
			throw new IllegalArgumentException("Illegal relevance value " + relevance);
		}
		this.pairData = pairData;
		this.relevance = relevance;
		this.parent = parent;
	}

	/**
	 * Gets the description of the clone quick fix
	 * @return A string displaying the clone
	 */
	public String getDescription() {
		return CloneResolutionGenerator.getCloneString(pairData.getOtherRegion().getStart().getGlobalOffset(), 
				pairData.getOtherRegion().getEnd().getGlobalOffset(), pairData.getOtherContents());
	}

	@Override
	public Image getImage() {
		//Cycle through the image choices, all clones of the same number should 
		// 	have the same image
		int imageNum = getCloneNumber() % Activator.IMAGE_IDS.length;
		String imageId = Activator.IMAGE_IDS[imageNum];
		return Activator.getDefault().getImageRegistry().get(imageId);
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
	/**
	 * Returns a description of the proposal, to display 
	 * 		to the user
	 * @see getDescription()
	 */
	public String getAdditionalProposalInfo() {
		return getDescription();
	}
	
	@Override
	/**
	 * Returns a short label describing this quickfix
	 * @see getLabel() 
	 */
	public String getDisplayString() {
		return getLabel();
	}
	
	@Override
	public IContextInformation getContextInformation() {
		// We don't need this feature
		return null;
	}
	
	@Override
	/** 
	 * Returns the relevance for this fix, which will determine ranking
	 * @return relevance The relevance, between 0-100
	 */
	public int getRelevance() {
		return relevance;
	}
	
	/**
	 * get the number for the clone pair
	 * @return the number for the clone pair
	 */
	public int getCloneNumber() {
		return pairData.getCloneNumber();
	}
	
	/**
	 * return the contents of the <i>entire</i> source file, or dirty buffer 
	 * contents if the buffer has been modified. The source region is given by
	 * {@link CloneFix#getSourceRegion()}
	 * @return the contents of the source
	 */
	protected String getSourceContents(){
		return pairData.getSourceContents();
	}
	
	/**
	 * return the contents of the <i>entire</i> file containing the
	 * the system clone. The source region is given by {@link CloneFix#getOtherRegion()}.
	 * @return the contents of the file containing the system clone
	 */
	protected String getOtherContents(){
		return pairData.getOtherContents();
	}
	
	/**
	 * get the source region for the active clone, i.e., 
	 * the clone this QuickFix is for
	 * @return source region for the active clone
	 */
	protected SourceRegion getSourceRegion(){
		return pairData.getSourceRegion();
	}
	
	/**
	 * get the system clone's source region
	 * @return the system clone
	 */
	protected SourceRegion getOtherRegion() {
		return pairData.getOtherRegion();
	}

	/**
	 * true iff the clones reside in the same file, i.e., the regions 
	 * {@link CloneFix#getSourceRegion() and {@link CloneFix#getOtherRegion()} are in 
	 * the same file
	 * @return true iff the clones reside in the same file
	 */
	protected boolean isSameFile() {
		return pairData.isSameFile();
	}
	
	/**
	 * Returns the parent, or null if there is none
	 * @return the parent, or null if there is none
	 */
	protected CloneResolutionGenerator getParent() {
		return parent;
	}
	
	/**
	 * Returns details to display iff {@link DEBUG_MODE} is set, otherwise
	 * returns an empty string.
	 * @return Returns details to display iff {@link DEBUG_MODE} is set, otherwise returns an empty string.
	 */
	protected String getLabelDetails(){
	    return DEBUG_MODE ? " (raw: " + ((int)pairData.getSimilarity()) + " adj: " + relevance + ")" : "";
	}
}
