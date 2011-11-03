package edu.washington.cs.rtrefactor.reconciler;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.internal.ui.javaeditor.IJavaAnnotation;
import org.eclipse.jface.text.quickassist.IQuickFixableAnnotation;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.ui.texteditor.MarkerAnnotation;
import org.eclipse.ui.texteditor.SimpleMarkerAnnotation;
//import org.eclipse.jdt.internal.ui.javaeditor.

/**
 * A custom annotation to indicate a cloned region of code.
 *  
 * @author Travis Mandel
 *
 */
public class CloneAnnotation extends MarkerAnnotation  {

	/** The clone annotation type. */
	public static final String TYPE= "rtrefactor.cloneAnnotation"; //$NON-NLS-1$



	//TODO: This probably needs to take args with clone info
	public CloneAnnotation(IMarker m) {
		
		super(TYPE, m);
		//super(TYPE, false, "New Clone");
	}

	
}

