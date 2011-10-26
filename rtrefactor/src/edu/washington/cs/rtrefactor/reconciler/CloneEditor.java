package edu.washington.cs.rtrefactor.reconciler;


import org.eclipse.core.runtime.CoreException;
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
import org.eclipse.ui.IEditorInput;

public class CloneEditor extends CompilationUnitEditor {

	public CloneEditor() {
		super();
		//int a = createJavaSourceViewerConfiguration();
		//System.out.println("SVConfig: " + getSourceViewerConfiguration());
		//getSourceViewerConfiguration().
		//setSourceViewerConfiguration(new XMLConfiguration());
		//JavaSourceViewer sv = (JavaSourceViewer) getSourceViewer();
		//IReconciler ir = sv.setReconciler(null);
	}

	//copied from eclipse, override
	protected JavaSourceViewerConfiguration createJavaSourceViewerConfiguration() {
		System.out.println("New config");
		JavaTextTools textTools= JavaPlugin.getDefault().getJavaTextTools();

		CloneViewerConfiguration tc =  new CloneViewerConfiguration(textTools.getColorManager(), getPreferenceStore(), this, IJavaPartitions.JAVA_PARTITIONING);
		ISourceViewer sourceViewer = getSourceViewer();
		if(sourceViewer != null && sourceViewer instanceof CloneSourceViewer)
		{
			System.out.println("changing reconciler " + sourceViewer);
			IReconciler reconciler= tc.getReconciler(sourceViewer);
			if (reconciler != null) {
				reconciler.install(sourceViewer);
				((CloneSourceViewer)sourceViewer).setReconciler(reconciler);
			}
		}

		return tc;
	}

	protected ISourceViewer createJavaSourceViewer(Composite parent, IVerticalRuler verticalRuler, IOverviewRuler overviewRuler, boolean isOverviewRulerVisible, int styles, IPreferenceStore store) {
		return new CloneSourceViewer(parent, verticalRuler, getOverviewRuler(), isOverviewRulerVisible(), styles, store);
	}

	protected void doSetInput(IEditorInput input) throws CoreException {
		System.out.println("calling setInput");
		ISourceViewer sourceViewer1 = getSourceViewer();
		System.out.println(sourceViewer1);
		super.doSetInput(input);
		CloneSourceViewer javaSourceViewer= null;
		ISourceViewer sourceViewer = getSourceViewer();
		System.out.println(sourceViewer);
		if (sourceViewer instanceof CloneSourceViewer)
			javaSourceViewer= (CloneSourceViewer)sourceViewer;


		if (javaSourceViewer != null && javaSourceViewer.getReconciler() == null) {
			System.out.println("Changing reconciler");
			IReconciler reconciler= getSourceViewerConfiguration().getReconciler(javaSourceViewer);
			if (reconciler != null) {
				reconciler.install(javaSourceViewer);
				javaSourceViewer.setReconciler(reconciler);
			}
		}
	}

}
