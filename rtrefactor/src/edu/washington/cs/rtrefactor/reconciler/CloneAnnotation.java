package edu.washington.cs.rtrefactor.reconciler;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.texteditor.MarkerAnnotation;

/** This trivial class lets us quickly and easily find our own annotations 
 *  in the document. */ 

public class CloneAnnotation extends MarkerAnnotation {
	/** The clone annotation type. */
	public static final String CLONE_ANNOTATION = "rtrefactor.cloneAnnotation"; 
	
	public CloneAnnotation(IMarker marker) {
		super(CLONE_ANNOTATION, marker);
	}

}
