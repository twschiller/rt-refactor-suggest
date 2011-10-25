package edu.washington.cs.rtrefactor.reconciler;

import org.eclipse.jdt.internal.ui.javaeditor.JavaSourceViewer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.swt.widgets.Composite;

public class CloneSourceViewer extends JavaSourceViewer {
	
	IReconciler fReconciler;

	public CloneSourceViewer(Composite parent, IVerticalRuler verticalRuler,
			IOverviewRuler overviewRuler, boolean showAnnotationsOverview,
			int styles, IPreferenceStore store) {
		super(parent, verticalRuler, overviewRuler, showAnnotationsOverview, styles,
				store);
		fReconciler = null;
	}
	
	public IReconciler getReconciler() {
		return fReconciler;
	}
	
	
	public void setReconciler(IReconciler reconciler) {
		if(reconciler instanceof MonoReconciler) {
			fReconciler= reconciler;
		} else {
			System.out.println("BAD reconciler! " + reconciler);
		}
	}


}
