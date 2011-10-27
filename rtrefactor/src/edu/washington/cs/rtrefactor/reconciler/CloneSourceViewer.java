package edu.washington.cs.rtrefactor.reconciler;

import org.eclipse.jdt.internal.ui.javaeditor.JavaSourceViewer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.swt.widgets.Composite;

/**
 * The only point of overriding this  class to is make get/set reconciler 
 * methods visible, so that we can install a custom reconciler.
 * 
 * @author Travis Mandel
 */
public class CloneSourceViewer extends JavaSourceViewer {
	
	IReconciler fReconciler;

	public CloneSourceViewer(Composite parent, IVerticalRuler verticalRuler,
			IOverviewRuler overviewRuler, boolean showAnnotationsOverview,
			int styles, IPreferenceStore store) {
		super(parent, verticalRuler, overviewRuler, showAnnotationsOverview, styles,
				store);
		fReconciler = null;
	}
	
	/** This should override JavaSourceViewer.getReconciler(), but that
	 * method is only package-level visible, so depending on which package 
	 * you are in different methods will be called.
	 * 
	 * @return the current reconciler
	 */
	public IReconciler getReconciler() {
		return fReconciler;
	}
	
	
	/** This should override JavaSourceViewer.setReconciler(), but that
	 * method is only package-level visible, so depending on which package 
	 * you are in different methods will be called.
	 * 
	 * Also includes a check to ensure the reconciler is a CloneReconciler
	 * 
	 * @return the current reconciler
	 */
	public void setReconciler(IReconciler reconciler) {
		if(reconciler instanceof CloneReconciler) {
			fReconciler= reconciler;
		} else {
			//do nothing
			CloneReconciler.reconcilerLog.error("Bad reconciler passed to " +
					"SourceViewer " + reconciler);
		}
	}


}
