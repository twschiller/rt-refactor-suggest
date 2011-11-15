package edu.washington.cs.rtrefactor.quickfix;

import java.io.IOException;

import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import edu.washington.cs.rtrefactor.detect.SourceRegion;
import edu.washington.cs.rtrefactor.reconciler.CloneEditor;
import edu.washington.cs.rtrefactor.reconciler.CloneReconciler;
import edu.washington.cs.rtrefactor.util.FileUtil;


/**
 * The superclass for all Clone quick fixes.
 * 
 * Allows custom descriptions, images, and relevances (scores).
 * 
 * @author Travis Mandel, Todd Schiller
 *
 */
public abstract class CloneFix implements IMarkerResolution, IJavaCompletionProposal{

	private final int cloneNumber;
	private final SourceRegion source;
	private final SourceRegion other;
	private final String sourceContents;
	private final String otherContents;
	private final boolean sameFile;
	private final int relevance;
	private boolean hasBeenActivated;
	
	/**
	 * Instantiates a clone clone quick fix
	 * 
	 * @param cloneNumber The clone pair number
	 * @param sourceClone The region containing the source, i.e. active, clone
	 * @param otherClone The region containing the system clone
	 * @param sourceContents The contents of the <i>entire</i> document containing {@code sourceClone}
	 * @param isSameFile Is the second clone in the same file as the first
	 * @param relevance A score from 10-100 indicating the relevance of this 
	 * 			suggestion
	 * @throws IOException if other.getFile() cannot be read
	 */
	public CloneFix(int cloneNumber, SourceRegion sourceClone, SourceRegion otherClone, String sourceContents, 
			boolean isSameFile, int relevance) throws IOException {
		if (relevance < 10 || relevance > 100){
			throw new IllegalArgumentException("Illegal relevance value");
		}
		
		this.cloneNumber = cloneNumber;
		this.source = sourceClone;
		this.other = otherClone;
		this.sourceContents = sourceContents;
		this.sameFile = isSameFile;
		this.otherContents = sameFile ? sourceContents :  FileUtil.read(other.getFile());
		this.relevance = relevance;
	}

	/**
	 * Gets the description of the clone quick fix
	 * @return A string displaying the clone
	 */
	public String getDescription() {
		return CloneFixer.getCloneString(other.getStart().getGlobalOffset(), 
				other.getEnd().getGlobalOffset(), otherContents);
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
	
	/**
	 * Notify the reconciler that the user clicked on a clone marker
	 */
	protected void notifyReconcilerClicked()
	{
		IEditorPart edit = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		assert (edit instanceof CloneEditor);
		CloneReconciler cr = ((CloneEditor)edit).getCloneReconciler();
		//TODO call some method of the reconciler (what args?)
	}
	
	/**
	 * Notify the reconciler that the user activated the fix
	 */
	protected void notifyReconcilerActivated()
	{
		IEditorPart edit = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		assert (edit instanceof CloneEditor);
		CloneReconciler cr = ((CloneEditor)edit).getCloneReconciler();
		//TODO call some method of the reconciler (what args?)
	}
	
	@Override
	/** 
	 * Returns the relevance for this fix, which will dtermine ranking
	 * @return relevance The relevance, between 0-100
	 */
	public int getRelevance() {
		return relevance;
	}
	
	/**
	 * get the number for the clone pair
	 * @return the number for the clone pair
	 */
	protected int getCloneNumber() {
		return cloneNumber;
	}
	
	/**
	 * return the contents of the <i>entire</i> source file, or dirty buffer 
	 * contents if the buffer has been modified. The source region is given by
	 * {@link CloneFix#getSourceRegion()}
	 * @return the contents of the source
	 */
	protected String getSourceContents(){
		return sourceContents;
	}
	
	/**
	 * return the contents of the <i>entire</i> file containing the
	 * the system clone. The source region is given by {@link CloneFix#getOtherRegion()}.
	 * @return the contents of the file containing the system clone
	 */
	protected String getOtherContents(){
		return otherContents;
	}
	
	/**
	 * get the source region for the active clone, i.e., 
	 * the clone this QuickFix is for
	 * @return source region for the active clone
	 */
	protected SourceRegion getSourceRegion(){
		return source;
	}
	
	/**
	 * get the system clone's source region
	 * @return the system clone
	 */
	protected SourceRegion getOtherRegion() {
		return other;
	}

	/**
	 * true iff the clones reside in the same file, i.e., the regions 
	 * {@link CloneFix#getSourceRegion() and {@link CloneFix#getOtherRegion()} are in 
	 * the same file
	 * @return true iff the clones reside in the same file
	 */
	protected boolean isSameFile() {
		return sameFile;
	}
}
