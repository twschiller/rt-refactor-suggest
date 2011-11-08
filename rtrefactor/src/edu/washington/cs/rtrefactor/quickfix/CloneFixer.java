package edu.washington.cs.rtrefactor.quickfix;

import java.io.BufferedReader;

import org.eclipse.jdt.internal.ui.text.spelling.WordCorrectionProposal;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator;

import edu.washington.cs.rtrefactor.detect.SourceLocation;
import edu.washington.cs.rtrefactor.detect.SourceRegion;

public class CloneFixer implements IMarkerResolutionGenerator {
	
	//public static final int MIN_CONTEXT_CHARACTERS = 10;
	public static final int CONTEXT_LINES = 2;
	
	public IMarkerResolution[] getResolutions(IMarker mk) {
		System.out.println("marker res called! ");
		String problem = "";
		int cloneNum = -1;
		String cloneFile = null;
		int cloneStart = -1;;
		int cloneEnd = -1;;
		String dirtyText = null;
		
		try {
			cloneNum = (Integer)mk.getAttribute("cloneNumber");
			cloneFile = (String)mk.getAttribute("cloneFile");
			cloneStart = (Integer)mk.getAttribute("cloneStartOffset");
			cloneEnd = (Integer)mk.getAttribute("cloneEndOffset");
			dirtyText = (String)mk.getAttribute("dirtyFileText");
		} catch (CoreException e) {
			e.printStackTrace();
		}
		File otherFile = new File(cloneFile);
		File thisFile = mk.getResource().getRawLocation().makeAbsolute().toFile();
		boolean sameFile = thisFile.equals(otherFile);
		//-1 indicates offset only
		SourceRegion otherClone = new SourceRegion(
					new SourceLocation(otherFile, 0, cloneStart), 
					new SourceLocation(otherFile, 0, cloneEnd));
		System.out.println("Problem is: " + problem);
		return new IMarkerResolution[] {
				new CopyFix(cloneNum, otherClone, dirtyText, sameFile),

				//new CopyFix("Extract method for clone #"+problem),
				//new CopyFix("Insert method call to clone #"+problem),

				//  new CopyFix("Jump to clone #"+problem),
		};

	}


	public static String getCloneString(int startOffOrig, int endOffOrig, String content)
	{
		
		//remove whitespace
		int startOff = startOffOrig, endOff = endOffOrig;
		while(startOff > 0 && Character.isWhitespace(content.charAt(startOff)))
			startOff--;
		while(endOff < content.length()-1 && 
				Character.isWhitespace(content.charAt(endOff)))
			endOff++;
		//We need to expand the region to give a little context
		//TODO: Add bolding, etc.
	
		String newline = System.getProperty("line.separator");
		int rStart = startOff;
		int cLines = 0;
		do {
			rStart = content.lastIndexOf(newline, rStart-newline.length());
			cLines++;
		} while(rStart >= 0 && cLines < CONTEXT_LINES);
		if(rStart< 0)
			rStart = 0;
		
		int rEnd = endOff;
		cLines = 0;
		do{
			rEnd = content.indexOf(newline, rEnd+newline.length());
			cLines++;
		} while(rEnd >= 0 && cLines < CONTEXT_LINES) ;
		if(rEnd < 0)
			rEnd = content.length()-1;
		
		String ret = "..." +content.substring(rStart, startOffOrig) + "<b>" +
				content.substring(startOffOrig, endOffOrig) + "</b>" + 
				content.substring(endOffOrig, rEnd) + "<br/>...";
		
		ret = ret.replaceAll(newline, "<br/>").replaceAll(" ", "&#160;").
				replaceAll("\t", "&#160;&#160;&#160;&#160;&#160;") ;
		
		return ret;
	}

}
