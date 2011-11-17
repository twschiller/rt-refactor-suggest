package edu.washington.cs.rtrefactor.reconciler;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.texteditor.MarkerAnnotation;

/** This trivial class lets us quickly and easily find our own annotations 
 *  in the document. */ 

public class CloneAnnotation extends MarkerAnnotation {
	/** The clone annotation type. */
	public static final String[] CLONE_ANNOTATIONS = 
		{"rtrefactor.cloneAnnotation","rtrefactor.cloneAnnotation2",
			"rtrefactor.cloneAnnotation3"} ; 
	
	public CloneAnnotation(IMarker marker, int annotationNumber) {
		super(CLONE_ANNOTATIONS[annotationNumber % CLONE_ANNOTATIONS.length], marker);
		
	}

}
