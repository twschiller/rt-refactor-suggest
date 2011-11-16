package edu.washington.cs.rtrefactor.quickfix;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator;

import edu.washington.cs.rtrefactor.Activator;
import edu.washington.cs.rtrefactor.detect.SourceLocation;
import edu.washington.cs.rtrefactor.detect.SourceRegion;
import edu.washington.cs.rtrefactor.preferences.PreferenceConstants;
import edu.washington.cs.rtrefactor.reconciler.ClonePairData;
import edu.washington.cs.rtrefactor.reconciler.CloneReconciler;
import edu.washington.cs.rtrefactor.scorer.Scorer;
import edu.washington.cs.rtrefactor.util.FileUtil;

/**
 *  Provides the quick fixes suggestions for refactoring clones to the Eclipse UI.
 *  
 *  This class is called once per clone PAIR.  The UI combines suggestions for 
 *  multiple pairs. 
 *  
 *  This class gets called when the user clicks on our marker or presses Ctrl+1
 *  (or the UI is trying to figure out what marker to display)
 *  
 *  getResolutions is only called once per instance
 *  
 * @author Travis Mandel
 *
 */
public class CloneFixer implements IMarkerResolutionGenerator {
	public static final String CLONE_NUMBER = "cloneNumber";
	
	public static final String SOURCE_START_OFFSET = "sourceStartOffset";
	public static final String SOURCE_END_OFFSET = "sourceEndOffset";
	public static final String SOURCE_TEXT = "dirtyFileText";
	
	public static final String OTHER_START_OFFSET = "cloneStartOffset";
	public static final String OTHER_END_OFFSET = "cloneEndOffset";
	public static final String OTHER_FILE = "cloneFile";
	
	private IMarkerResolution[] lastFixes;
	private boolean hasActivated;
	
	public CloneFixer()
	{
		lastFixes = null;
		hasActivated = false;
	}
	
	/**
	 * Called to provide quick fixes for the given marker
	 * \
	 * Should only be called once per instance
	 * 
	 * @see IMarkerResolutionGenerator.getResolutions
	 */
	@Override
	public IMarkerResolution[] getResolutions(IMarker marker) {
		//Called twice, should not happen
		if(lastFixes != null)
			throw new RuntimeException("Single CloneFixer instance called twice!");
		
		File sourceFile = marker.getResource().getRawLocation().makeAbsolute().toFile();
		
		int cloneNumber = -1;
		
		int sourceStart = -1;
		int sourceEnd = -1;
		String sourceText = null;
		
		int otherStart = -1;
		int otherEnd = -1;
		File otherFile = null;
		
		//Get the attributes from the marker (they were put here by the reconciler)
		try {
			cloneNumber = (Integer)marker.getAttribute(CLONE_NUMBER);
		
			sourceStart = (Integer)marker.getAttribute(SOURCE_START_OFFSET);
			sourceEnd = (Integer)marker.getAttribute(SOURCE_END_OFFSET);
			sourceText = (String)marker.getAttribute(SOURCE_TEXT);
			
			assert sourceStart != sourceEnd;
			
			otherStart = (Integer)marker.getAttribute(OTHER_START_OFFSET);
			otherEnd = (Integer)marker.getAttribute(OTHER_END_OFFSET);
			otherFile = new File((String)marker.getAttribute(OTHER_FILE));
			
			assert otherStart != otherEnd;
		
		} catch (CoreException e) {
			CloneReconciler.reconcilerLog.error("Error fetching clone marker attributes", e);
			return new IMarkerResolution[]{};
		}

		// Check if the clones come from the same file
		boolean sameFile = sourceFile.equals(otherFile);

		Document sourceDoc = new Document(sourceText);
		Document otherDoc;
		try {
			otherDoc = sameFile ? sourceDoc : new Document(FileUtil.read(otherFile));
		} catch (IOException e1) {
			CloneReconciler.reconcilerLog.error("Problem reading file indicated by marker", e1);
			return new IMarkerResolution[]{};
		}

		SourceRegion sourceClone;
		try {
			sourceClone = new SourceRegion(
					new SourceLocation(sourceFile, sourceStart, sourceDoc), 
					new SourceLocation(sourceFile, sourceEnd, sourceDoc));
		} catch (BadLocationException e) {
			CloneReconciler.reconcilerLog.error("Bad clone location for source clone in file " + sourceFile.getName(), e);
			return new IMarkerResolution[]{};
		}
		
		SourceRegion otherClone;
		try{
			otherClone = new SourceRegion(
					new SourceLocation(otherFile, otherStart, otherDoc), 
					new SourceLocation(otherFile, otherEnd, otherDoc));
		}catch (BadLocationException e) {
			CloneReconciler.reconcilerLog.error("Bad clone location for system (other) clone in file " + otherFile.getName(), e);
			return new IMarkerResolution[]{};
		}
		
		ClonePairData pairData;
		try {
			pairData = new ClonePairData(cloneNumber, sourceClone, otherClone, sourceText, sameFile);
		} catch (IOException e) {
			CloneReconciler.reconcilerLog.error("Could not read file when creating clones", e);
			return new IMarkerResolution[]{};
		}

		lastFixes =  Scorer.getInstance().calculateResolutions(pairData, this).toArray(new IMarkerResolution[]{});
		return lastFixes;
	}

	/**
	 * Gets a string suitable for the quick fix display showing the clone.
	 * 
	 * @param startOffOrig The starting offset of the clone
	 * @param endOffOrig The end offset of the clone
	 * @param content The contents of the file the clones are in
	 * @return A string, filled with HTML markup, displaying the bolded clone
	 * 		with CONTEXT_LINES lines of context.  Intended to be shown to the user.
	 */
	public static String getCloneString(int startOffOrig, int endOffOrig, 
			String content)
	{
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		int contextLines = store.getInt(PreferenceConstants.P_CONTEXT_LINES);
		
		//  Trim the whitespace off the region described by the offsets.
		int startOff = startOffOrig, endOff = endOffOrig;
		while(startOff > 0 && Character.isWhitespace(content.charAt(startOff)))
			startOff--;
		while(endOff < content.length()-1 && 
				Character.isWhitespace(content.charAt(endOff)))
			endOff++;

		//Expand the region to give a little context
		String newline = System.getProperty("line.separator");
		//First move the start backward
		int rStart = startOff;
		int cLines = 0;
		do {
			rStart = content.lastIndexOf(newline, rStart-newline.length());
			cLines++;
		} while(rStart >= 0 && cLines < contextLines);
		if(rStart< 0) {
			rStart = 0;
		}
			

		//Next move the end forward
		int rEnd = endOff;
		cLines = 0;
		do{
			rEnd = content.indexOf(newline, rEnd+newline.length());
			cLines++;
		} while(rEnd >= 0 && cLines < contextLines) ;
		if(rEnd < 0) {
			rEnd = content.length()-1;
		}
			

		//return the string with the bolding tags
		String ret = "..." +content.substring(rStart, startOffOrig) + "<b>" +
				content.substring(startOffOrig, endOffOrig) + "</b>" + 
				content.substring(endOffOrig, rEnd) + "<br/>...";

		// We need to preserve the indentations here.  Eclipse's HTML 
		// parser/renderer  is very incomplete, so normal methods don't work.  
		// Here I  replace spaces and tabs by &#160;.  This is the same as 
		//  &nbsp;, but Eclipse doesn't recognize that :(
		ret = ret.replaceAll(newline, "<br/>").replaceAll(" ", "&#160;").
				replaceAll("\t", "&#160;&#160;&#160;&#160;&#160;") ;

		return ret;
	}
	
	/**
	 * Notify the scorer that this fix group was activated.
	 * 
	 * We need this method because only the individual fixes get the callback that they
	 * have been activated, we need to only notify the scorer once.
	 */
	public void notifyFixesActivated() {
		if(lastFixes == null) {
			CloneReconciler.reconcilerLog.error("User clicked marker but CloneFixer has not sucessfully generated fixes!");
			return;
		}
		if(!hasActivated) {
			Scorer.getInstance().recordQuickFixActivation(lastFixes);
			hasActivated = true;
		}
	}
	
	/**
	 * Notify the scorer that the given fix has been selected by the user
	 * 
	 * @param fix The selected CloneFix
	 */
	public void notifyFixSelected(CloneFix fix) {
		if(lastFixes == null) {
			CloneReconciler.reconcilerLog.error("User selected fix but CloneFixer has not sucessfully generated fixes!");
			return;
		}
		Scorer.getInstance().recordQuickFixSelection (lastFixes, fix);
	}
}
