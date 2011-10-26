package edu.washington.cs.rtrefactor.reconciler;


import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jdt.ui.text.IJavaPartitions;
import org.eclipse.jdt.ui.text.JavaSourceViewerConfiguration;
import org.eclipse.jdt.ui.text.JavaTextTools;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.swt.widgets.Composite;

/* This editor is like a java editor, but extending with clone detection
 * functionality.
 */
public class CloneEditor extends CompilationUnitEditor {

	public CloneEditor() {
		super();
	}

	/*We override this method to hook up the CloneViewerConfiguration,
	 *  and to install the reconciler on the viewer.
	 * @see org.eclipse.jdt.internal.ui.javaeditor.JavaEditor#createJavaSourceViewerConfiguration()
	 */
	protected JavaSourceViewerConfiguration createJavaSourceViewerConfiguration() {
		JavaTextTools textTools= JavaPlugin.getDefault().getJavaTextTools();

		CloneViewerConfiguration tc =  new CloneViewerConfiguration(textTools.getColorManager(), getPreferenceStore(), this, IJavaPartitions.JAVA_PARTITIONING);
		ISourceViewer sourceViewer = getSourceViewer();
		if(sourceViewer != null && sourceViewer instanceof CloneSourceViewer)
		{
			IReconciler reconciler= tc.getReconciler(sourceViewer);
			if (reconciler != null) {
				reconciler.install(sourceViewer);
				/* We know it is our own viewer because we override 
				 *  createJavaSourceViewer().
				 *  
				 *  A normal sourceViewer does not make setReconciler visible,
				 *  	but ours does.
				 */
				((CloneSourceViewer)sourceViewer).setReconciler(reconciler);
			}
		}

		return tc;
	}
	
	/* This is overidden to return our modified viewer */
	protected ISourceViewer createJavaSourceViewer(Composite parent, IVerticalRuler verticalRuler, IOverviewRuler overviewRuler, boolean isOverviewRulerVisible, int styles, IPreferenceStore store) {
		return new CloneSourceViewer(parent, verticalRuler, getOverviewRuler(), isOverviewRulerVisible(), styles, store);
	}


}
