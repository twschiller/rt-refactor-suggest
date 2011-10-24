package edu.washington.cs.rtrefactor.reconciler;

import java.lang.annotation.Annotation;
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.internal.ui.javaeditor.JavaMarkerAnnotation;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;

public class CloneReconcilingStrategy implements IReconcilingStrategy,IReconcilingStrategyExtension{
	
	private IDocument fDocument;
	private IAnnotationModel fAnnotationModel;
	 private ISourceViewer fViewer;
	 private ITextEditor fEditor;
	
	
	public CloneReconcilingStrategy(ISourceViewer viewer, ITextEditor editor)
	{
		fViewer = viewer;
		fAnnotationModel = fViewer.getAnnotationModel();
		fEditor = editor;
	}
	
	@Override
	public void setDocument(IDocument document) {
		fDocument = document;
	}

	@Override
	public void reconcile(DirtyRegion dirtyRegion, IRegion subRegion) {
		// TODO Make sure this is called
		
	}

	@Override
	public void reconcile(IRegion partition) {
	
		IResource res = (IResource) fEditor.getEditorInput().getAdapter(IResource.class);

		fAnnotationModel.addAnnotation(new CloneAnnotation(), new Position(3, 100));

		
	}

	@Override
	public void setProgressMonitor(IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void initialReconcile() {
		// TODO Auto-generated method stub
		
	}
	

}
