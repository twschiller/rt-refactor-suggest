package edu.washington.cs.rtrefactor.reconciler;

import org.eclipse.jdt.internal.ui.javaeditor.JavaSourceViewer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.swt.widgets.Composite;

/*
 * The only point of this class to is make get/set reconciler methods visible
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
	
	/* This should override, but the other method is only package-level visible, so
	 * depending on which package you are in different methods will be called.
	 * 
	 * Returns the current reconciler
	 */
	public IReconciler getReconciler() {
		return fReconciler;
	}
	
	
	/* This should override, but the other method is only package-level visible, so
	 * depending on which package you are in different methods will be called.
	 * 
	 * We include an extra check to make sure the reconciler is a CLoneReconciler.
	 */
	public void setReconciler(IReconciler reconciler) {
		if(reconciler instanceof CloneReconciler) {
			fReconciler= reconciler;
		} else {
			System.out.println("BAD reconciler! " + reconciler);
		}
	}


}
