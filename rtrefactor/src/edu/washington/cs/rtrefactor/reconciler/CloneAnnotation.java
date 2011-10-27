package edu.washington.cs.rtrefactor.reconciler;

import org.eclipse.jface.text.quickassist.IQuickFixableAnnotation;
import org.eclipse.jface.text.source.Annotation;

/**
 * A custom annotation to indicate a cloned region of code.
 *  
 * @author Travis Mandel
 *
 */
public class CloneAnnotation extends Annotation implements IQuickFixableAnnotation {

	/** The clone annotation type. */
	public static final String TYPE= "rtrefactor.cloneAnnotation"; //$NON-NLS-1$



	//TODO: This probably needs to take args with clone info
	public CloneAnnotation() {
		super(TYPE, false, "New Clone");
	}

	/**
	 * @see org.eclipse.jface.text.quickassist.IQuickFixableAnnotation#isQuickFixable()
	 */
	public boolean isQuickFixable() {
		return true;
	}

	/**
	 * @see org.eclipse.jface.text.quickassist.IQuickFixableAnnotation#isQuickFixableStateSet()
	 */
	public boolean isQuickFixableStateSet() {
		return true;
	}

	/**
	 * @see org.eclipse.jface.text.quickassist.IQuickFixableAnnotation#setQuickFixable(boolean)
	 */
	public void setQuickFixable(boolean state) {
		// always true
	}
}

