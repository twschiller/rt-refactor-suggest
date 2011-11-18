package edu.washington.cs.rtrefactor.reconciler;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.texteditor.MarkerAnnotation;
import org.eclipse.ui.texteditor.SimpleMarkerAnnotation;

/** This trivial class lets us quickly and easily find our own annotations 
 *  in the document. */ 

public class CloneAnnotation extends SimpleMarkerAnnotation {
	/** The clone annotation type. */
	public static final String[] CLONE_ANNOTATIONS = 
		{"rtrefactor.cloneAnnotation"} ; 
	
	public CloneAnnotation(IMarker marker, int annotationNumber) {
		super(CLONE_ANNOTATIONS[annotationNumber % CLONE_ANNOTATIONS.length], marker);
		
	}

}
