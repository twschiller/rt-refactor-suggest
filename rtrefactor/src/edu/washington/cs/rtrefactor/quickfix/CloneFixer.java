package edu.washington.cs.rtrefactor.quickfix;

import java.io.File;
import java.util.Random;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.Document;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator;

import edu.washington.cs.rtrefactor.detect.SourceLocation;
import edu.washington.cs.rtrefactor.detect.SourceRegion;
import edu.washington.cs.rtrefactor.reconciler.CloneReconciler;
import edu.washington.cs.rtrefactor.util.FileUtil;

/**
 *  Provides the quick fixes suggestions for refactoring clones to the Eclipse UI.
 *  
 *  This class is called once per clone PAIR.  The UI combines suggestions for 
 *  multiple pairs. 
 *  
 *  This class gets called when the user clicks on our marker or presses Ctrl+1
 *  
 * @author Travis Mandel
 *
 */
public class CloneFixer implements IMarkerResolutionGenerator {

	public static final int CONTEXT_LINES = 2;
	
	/**
	 * Called to provide quick fixes for the given marker
	 * 
	 * @see IMarkerResolutionGenerator.getResolutions
	 */
	@Override
	public IMarkerResolution[] getResolutions(IMarker mk) {
		int cloneNum = -1;
		String cloneFile = null;
		int cloneStart = -1;
		int cloneEnd = -1;
		int sourceStart = -1;
		int sourceEnd = -1;
		String dirtyText = null;
		
		//Get the attributes from the marker (they were put here by the reconciler)
		try {
			cloneNum = (Integer)mk.getAttribute("cloneNumber");
			cloneFile = (String)mk.getAttribute("cloneFile");
			cloneStart = (Integer)mk.getAttribute("cloneStartOffset");
			cloneEnd = (Integer)mk.getAttribute("cloneEndOffset");
			dirtyText = (String)mk.getAttribute("dirtyFileText");
			sourceStart = (Integer)mk.getAttribute("sourceStartOffset");
			sourceEnd = (Integer)mk.getAttribute("sourceEndOffset");
		} catch (CoreException e) {
			CloneReconciler.reconcilerLog.error("Error fetching clone marker attributes", e);
			return new IMarkerResolution[]{};
		}
		
		// Check if the clones come from the same file
		File otherFile = new File(cloneFile);
		File thisFile = mk.getResource().getRawLocation().makeAbsolute().toFile();
		boolean sameFile = thisFile.equals(otherFile);
		
		Document thisDoc = new Document(dirtyText);
		Document otherDoc = sameFile ? thisDoc :  new Document(FileUtil.read(otherFile));
		
		SourceRegion otherClone = new SourceRegion(
				new SourceLocation(otherFile, cloneStart, otherDoc), 
				new SourceLocation(otherFile, cloneEnd, otherDoc));
		
		SourceRegion sourceClone = new SourceRegion(
				new SourceLocation(thisFile, sourceStart, thisDoc), 
				new SourceLocation(thisFile, sourceEnd, thisDoc));
		
		//TODO: We assign these for testing purposes only, use real scores
		Random r= new Random();
		//Relevance must be between 10-100
		int relevance = r.nextInt(91) + 10;
		
		//TODO:  If score too low, some may not be shown
		return new IMarkerResolution[] {
			new CopyPasteFix(cloneNum, sourceClone, otherClone, dirtyText, sameFile,
					relevance),
			new ExtractMethodFix(cloneNum, sourceClone, otherClone, dirtyText, 
					sameFile, relevance+1),
			new InsertCallFix(cloneNum, sourceClone, otherClone, dirtyText, sameFile, 
					relevance+2),
			new JumpToFix(cloneNum, sourceClone, otherClone, dirtyText, sameFile, 
					relevance+3),			
			};

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
		} while(rStart >= 0 && cLines < CONTEXT_LINES);
		if(rStart< 0)
			rStart = 0;

		//Next move the end forward
		int rEnd = endOff;
		cLines = 0;
		do{
			rEnd = content.indexOf(newline, rEnd+newline.length());
			cLines++;
		} while(rEnd >= 0 && cLines < CONTEXT_LINES) ;
		if(rEnd < 0)
			rEnd = content.length()-1;
		
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


}
