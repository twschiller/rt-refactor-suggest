package edu.washington.cs.rtrefactor.reconciler;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
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
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.texteditor.ITextEditor;

import edu.washington.cs.rtrefactor.Activator;
import edu.washington.cs.rtrefactor.detect.CheckStyleDetector;
import edu.washington.cs.rtrefactor.detect.ClonePair;
import edu.washington.cs.rtrefactor.detect.IActiveDetector;
import edu.washington.cs.rtrefactor.detect.JccdDetector;
import edu.washington.cs.rtrefactor.detect.SimianDetector;
import edu.washington.cs.rtrefactor.detect.SourceLocation;
import edu.washington.cs.rtrefactor.detect.SourceRegion;
import edu.washington.cs.rtrefactor.preferences.PreferenceConstants;

public class CloneReconcilingStrategy implements IReconcilingStrategy,IReconcilingStrategyExtension{
	
	private IDocument fDocument;
	private File fFile;
	private IAnnotationModel fAnnotationModel;
	private ISourceViewer fViewer;
	private ITextEditor fEditor;
	
	
	private IActiveDetector detector = null;
	
	//TODO: Is this the best way to determine if the preference has changed?
	/* The latest value of the detector preference*/
	private String detectorPreference;
	
	public CloneReconcilingStrategy(ISourceViewer viewer, ITextEditor editor)
	{
		fViewer = viewer;
		fAnnotationModel = fViewer.getAnnotationModel();
		fEditor = editor;
		/*This will initialize the detector*/
		checkPreferences();
		
	}
	
	@Override
	public void setDocument(IDocument document) {
		fDocument = document;
		
		/*Get the File corresponding to the Document and store it in a field */
		IResource res = (IResource) fEditor.getEditorInput().getAdapter(IResource.class);
		IPath fPath = ResourcesPlugin.getWorkspace().getRoot().getLocation();
		fPath = fPath.append(res.getFullPath().makeAbsolute());
		fFile= fPath.toFile();
	}

	@Override
	public void reconcile(DirtyRegion dirtyRegion, IRegion subRegion) {
		doReconcile(dirtyRegion);
	}

	@Override
	public void reconcile(IRegion partition) {
		doReconcile(null);
	}
	
	/* Performs the clone detection.
	 * dirtyRegion is null if we should reconcile the entire file
	 */
	private void doReconcile(DirtyRegion dirtyRegion)
	{
		//Make sure no important preferences changed
		checkPreferences();
		
		
		/* For now, only one file is dirty (the current one)*/
		/* This is the same way java does parsing/building */
		Map<File, String> dirty = new HashMap<File, String>();
		dirty.put(fFile, fDocument.get());
		
		/*Get the line,offset of the last character in the file*/
		int lines = fDocument.getNumberOfLines();
		int lastOff = 0;
		try {
			lastOff = fDocument.getLineLength(lines-1);
		} catch (BadLocationException e) {
			e.printStackTrace();
			return;
		}
		
		System.out.println("Running detector!");
		
		/*Perform either incremental or non-incremental reconcile*/
		SourceRegion active;
		if(dirtyRegion != null)
		{
			active = new SourceRegion(convertOffset(dirtyRegion.getOffset()), 
					convertOffset(dirtyRegion.getOffset() + dirtyRegion.getLength()) );
			System.out.println("incremental detection, dirty region is " + dirtyRegion.getText());
		}
		else
			active = new SourceRegion(new SourceLocation(fFile, 0, 0), 
					new SourceLocation(fFile, lines-1, lastOff));
		
		/*Clear the annotations that overlap with the target area*/
		removeAnnotations(convertSourceLocation(active.getStart()), convertSourceLocation(active.getEnd()));
		
		/*Run the detection*/
		Set<ClonePair> hs = null;
		try {
			hs = detector.detect(dirty, active);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("detector failed!");
			return;
		} 
		
		System.out.println("Detector returned " + hs.size() + " pairs");
		/*Mark the clones in the file*/
		for(ClonePair cp : hs)
		{
			boolean added = false;
			System.out.println(cp.getFirst().getFile() +" "+ cp.getSecond().getFile());
			if(cp.getFirst().getFile().equals(fFile))
			{
				addAnnotation(cp.getFirst());
				added = true;
			}
			if(cp.getSecond().getFile().equals(fFile))
			{
				addAnnotation(cp.getSecond());
				added = true;
			}
			
			/* We should always add at least one annotation per pair*/
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
	/* Adds a clone annotation to the given source region*/
	private void addAnnotation(SourceRegion r)
	{
		
		System.out.println("Adding annotation!");
		int off = convertSourceLocation(r.getStart());
		int len = convertSourceLocation(r.getEnd()) - off;
		fAnnotationModel.addAnnotation(new CloneAnnotation(), new Position(off, len));

	}
	
	/*
	 * Removes all annotations that overlap with the given offset range
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
	
	
	/* Check to see if the detector preference changed.
	 * If it did, switch detectors. 
	 */
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
	
	/* Initialize the detector based on the "detectorPreference" field */
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
	
	
	//TODO: Maybe these conversion methods could go elsewhere?
	
	/* 
	 * Given a source location, return the corresponding character 
	 * offset in the file
	 */
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
	
	/*
	 * Given a character offset into the current document, returns a new 
	 * SourceLocation representing the same location.
	 */
	private SourceLocation convertOffset(int offset)
	{
		int line =0, newOffset =0;
		try {
			line = fDocument.getLineOfOffset(offset);
			int lineOff;
			if(line == 0)
				lineOff = 0;
			else
				lineOff = fDocument.getLineOffset(line-1);
			newOffset = offset - lineOff;
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		return new SourceLocation(fFile, line, newOffset);
	}
	
	
	

}
