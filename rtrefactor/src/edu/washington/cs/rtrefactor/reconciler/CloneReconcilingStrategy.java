package edu.washington.cs.rtrefactor.reconciler;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.ui.texteditor.ITextEditor;

import edu.washington.cs.rtrefactor.Activator;
import edu.washington.cs.rtrefactor.detect.CheckStyleDetector;
import edu.washington.cs.rtrefactor.detect.ClonePair;
import edu.washington.cs.rtrefactor.detect.IActiveDetector;
import edu.washington.cs.rtrefactor.detect.IDetector;
import edu.washington.cs.rtrefactor.detect.JccdDetector;
import edu.washington.cs.rtrefactor.detect.SimianDetector;
import edu.washington.cs.rtrefactor.detect.SourceLocation;
import edu.washington.cs.rtrefactor.detect.SourceRegion;
import edu.washington.cs.rtrefactor.preferences.PreferenceConstants;

public class CloneReconcilingStrategy implements IReconcilingStrategy,IReconcilingStrategyExtension{
	
	private IDocument fDocument;
	private IAnnotationModel fAnnotationModel;
	private ISourceViewer fViewer;
	private ITextEditor fEditor;
	
	private IDetector detector = null;
	
	//TODO: Is this the best way to determine if the preference has changed?
	private String detectorPreference;
	
	public CloneReconcilingStrategy(ISourceViewer viewer, ITextEditor editor)
	{
		fViewer = viewer;
		fAnnotationModel = fViewer.getAnnotationModel();
		fEditor = editor;
		checkPreferences();
		

		
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
		
		//Make sure no important preferences changed
		checkPreferences();
		
		IResource res = (IResource) fEditor.getEditorInput().getAdapter(IResource.class);
		IPath fPath = ResourcesPlugin.getWorkspace().getRoot().getLocation();
		fPath = fPath.append(res.getFullPath().makeAbsolute());
		File currentFile = fPath.toFile();
		
		Map<File, String> dirty = new HashMap<File, String>();
		dirty.put(currentFile, fDocument.get());
		
		
		int lines = fDocument.getNumberOfLines();
		int lastOff = 0;
		try {
			lastOff = fDocument.getLineLength(lines-1);
		} catch (BadLocationException e) {
			e.printStackTrace();
			return;
		}
		System.out.println("Running detector!");
		
		SourceRegion active = new SourceRegion(new SourceLocation(currentFile, 0, 0), new SourceLocation(currentFile, lines-1, lastOff));
		removeAnnotations(convertSourceLocation(active.getStart()), convertSourceLocation(active.getEnd()));
		Set<ClonePair> hs = null;
		try {
			hs = detector.detect(dirty);
		} catch (CoreException e) {
			e.printStackTrace();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		System.out.println("Detector returned " + hs.size() + " pairs");
		for(ClonePair cp : hs)
		{
			boolean added = false;
			System.out.println(cp.getFirst().getFile() +" "+ cp.getSecond().getFile());
			if(cp.getFirst().getFile().equals(currentFile))
			{
				addAnnotation(cp.getFirst());
				added = true;
			}
			if(cp.getSecond().getFile().equals(currentFile))
			{
				addAnnotation(cp.getSecond());
				added = true;
			}
			
			assert added ;
		}
		
		
		
	}

	@Override
	public void setProgressMonitor(IProgressMonitor monitor) {
		// TODO Where is this shown to the user?  Do we need it?
		
	}

	@Override
	public void initialReconcile() {
		// TODO Do we need to do anything here? (Before the user types)
		
	}
	
	//TODO: What other info is needed here?
	private void addAnnotation(SourceRegion r)
	{
		
		System.out.println("Adding annotation!");
		int off = convertSourceLocation(r.getStart());
		int len = convertSourceLocation(r.getEnd()) - off;
		fAnnotationModel.addAnnotation(new CloneAnnotation(), new Position(off, len));

	}
	
	/*
	 * Removes all annotations in a range
	 */
	private void removeAnnotations(int start, int end)
	{
		Iterator<Annotation> it = fAnnotationModel.getAnnotationIterator();
		while(it.hasNext())
		{
			Annotation an = it.next();
			if(an instanceof CloneAnnotation)
			{
				Position p = fAnnotationModel.getPosition(an);
				if(p.offset >= start || p.length+p.offset < end)
				{
					fAnnotationModel.removeAnnotation(an);
				}
			}
		}
	}
	
	private int convertSourceLocation(SourceLocation sl)
	{
		int off;
		try {
			if(sl.getLine() == 0)
				off = 0;
			else
				off = fDocument.getLineOffset(sl.getLine()-1);
		} catch (BadLocationException e) {
			e.printStackTrace();
			return 0;
		}
		
		off += sl.getOffset();
		
		return off;
	}
	
	private void checkPreferences()
	{
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		String name = store.getString(PreferenceConstants.P_CHOICE);
		if(!name.equals(detectorPreference))
		{
			detectorPreference = name;
			initializeDetector();
		}
		
	}
	
	private void initializeDetector()
	{
		if (detectorPreference.equalsIgnoreCase(JccdDetector.NAME)){
			detector = new JccdDetector();
		}else if (detectorPreference.equalsIgnoreCase(CheckStyleDetector.NAME)){
			detector = new CheckStyleDetector();
		}else if (detectorPreference.equalsIgnoreCase(SimianDetector.NAME)){
			detector = new SimianDetector();
		}
	}
	
	
	

}
