package edu.washington.cs.rtrefactor.reconciler;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.ui.text.IColorManager;
import org.eclipse.jdt.ui.text.JavaSourceViewerConfiguration;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.quickassist.IQuickAssistAssistant;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.ide.IDE;

/**
 * This class is overriden to create a CloneReconciler and CloneReconcilingStrategy
 * 
 * @author Travis Mandel
 */
public class CloneViewerConfiguration extends JavaSourceViewerConfiguration {

	public CloneViewerConfiguration(IColorManager cm, IPreferenceStore ps, ITextEditor te, String partitioning) {
		super(cm, ps, te, partitioning);

	}

	//TODO: For quick fixes: override getquickassistassistant?
	// Description: 1. Override  getquickassistassistant to retrun clone assistant
	//		2. Overrride asssisstant class.  use setcorrectionprocessor to add our processor
	//				also must change static class to the class methods ;(
	///  	3. Override processor class.  override hascorrections(annotation), 
	///    			and collectProposals, which should make a call to our 
	///				class to checkfor/get proposals
	
	//This method is overriden to return our QuickAssistAssistant, which handles quick fixes.
	/*public IQuickAssistAssistant getQuickAssistAssistant(ISourceViewer sourceViewer) {
		if (getEditor() != null)
			return new CloneCorrectionAssistant(getEditor());
		return null;
	}*/


	//This method is overridden to return our reconciler and strategy
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