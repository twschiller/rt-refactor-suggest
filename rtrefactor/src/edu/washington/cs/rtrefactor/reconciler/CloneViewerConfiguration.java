package edu.washington.cs.rtrefactor.reconciler;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.ui.text.IColorManager;
import org.eclipse.jdt.ui.text.JavaSourceViewerConfiguration;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.texteditor.ITextEditor;

public class CloneViewerConfiguration extends JavaSourceViewerConfiguration {

	public CloneViewerConfiguration(IColorManager cm, IPreferenceStore ps, ITextEditor te, String partitioning) {
		super(cm, ps, te, partitioning);

	}

	//For quick fixes: override getquickassistassistant?

	public IReconciler getReconciler(ISourceViewer sourceViewer) {
		final ITextEditor editor= getEditor();
		if (editor != null && editor.isEditable()) {
			CloneReconcilingStrategy cloneStrategy = new CloneReconcilingStrategy(sourceViewer, editor);
			CloneReconciler reconciler= new CloneReconciler(cloneStrategy);
			reconciler.setIsAllowedToModifyDocument(false);
			reconciler.setProgressMonitor(new NullProgressMonitor());
			reconciler.setDelay(500);

			return reconciler;
		}
		return null;
	}



}