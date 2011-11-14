package edu.washington.cs.rtrefactor.reconciler;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
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
import edu.washington.cs.rtrefactor.quickfix.CloneFixer;
import edu.washington.cs.rtrefactor.util.FileUtil;

/**
 * A reconciling strategy which can be incremental (or not)
 * 
 * It runs the clone detection on the dirty files, and annotates the 
 * cloned code with CloneAnnotations. 
 * 
 * @author Travis Mandel
 *
 */
public class CloneReconcilingStrategy implements IReconcilingStrategy,IReconcilingStrategyExtension{

	private IDocument fDocument;
	private File fFile;
	private IAnnotationModel fAnnotationModel;
	private ISourceViewer fViewer;
	private ITextEditor fEditor;

	private IActiveDetector detector = null;
	
	/** The clone marker type*/
	public static final String CLONE_MARKER = "rtrefactor.cloneMarker";
	

	public CloneReconcilingStrategy(ISourceViewer viewer, ITextEditor editor)
	{
		fViewer = viewer;
		fAnnotationModel = fViewer.getAnnotationModel();
		fEditor = editor;
		//Initial initialization
		initializeDetector();

		//Add a listener so we know when the detector property changed
		Activator.getDefault().getPreferenceStore().addPropertyChangeListener(
				new IPropertyChangeListener() {

					@Override
					public void propertyChange(PropertyChangeEvent event) {
						if (event.getProperty() == PreferenceConstants.P_CHOICE) {
							initializeDetector();
						}
					}
				});

	}

	@Override
	public void setDocument(IDocument document) {
		fDocument = document;

		//Get the File corresponding to the Document and store it in a field 
		IResource res = getResource(); 
		fFile = res.getRawLocation().makeAbsolute().toFile();
		System.out.println(fFile);
	}

	@Override
	public void reconcile(DirtyRegion dirtyRegion, IRegion subRegion) {
		doReconcile(dirtyRegion);
	}

	@Override
	public void reconcile(IRegion partition) {
		doReconcile(null);
	}

	/** 
	 * Performs the clone detection.
	 * 
	 * @param dirtyRegion The region that has changed since the last reconciler,
	 * 		is null if we should reconcile the entire file
	 */
	private void doReconcile(DirtyRegion dirtyRegion)
	{
		// the buffer needs to be reloaded
		if (fDocument.getLength() == 0){
			return;
		}
		
		// For now, only one file is dirty (the current one)
		// This is the same way java does parsing/building 
		Map<File, String> dirty = new HashMap<File, String>();
		dirty.put(fFile, fDocument.get());
		
		CloneReconciler.reconcilerLog.debug("Running clone detector");

		//Perform either incremental or non-incremental reconcile
		SourceRegion active;
		try {
			if(dirtyRegion != null)
			{
				active = new SourceRegion(
						convertOffset(dirtyRegion.getOffset()), 
						convertOffset(dirtyRegion.getOffset() + dirtyRegion.getLength()) );
			}
			else{
				active = new SourceRegion(
						new SourceLocation(fFile, 0, fDocument), 
						new SourceLocation(fFile, fDocument.getLength(), fDocument));
			}
		} catch (BadLocationException e) {
			CloneReconciler.reconcilerLog.error("Could not create active region to pass to clone detector", e);
			return;
		}

		//Clear the annotations that overlap with the target area
		removeAnnotations(active.getStart().getGlobalOffset(), active.getEnd().getGlobalOffset());

		//Run the detection
		Set<ClonePair> hs = null;
		try {
			hs = detector.detect(dirty, active);
		} catch (Exception e) {
			CloneReconciler.reconcilerLog.error("Clone detection failed", e);
			return;
		} 

		CloneReconciler.reconcilerLog.debug("Detector returned " + hs.size() + " pairs");

		//Mark the clones in the file
		int cloneNumber = 1;
		for(ClonePair cp : hs)
		{
			boolean added = false;

			CloneReconciler.reconcilerLog.debug("Marking clone pair: " + cp.getFirst().getFile().getName() + " " + cp.getSecond().getFile().getName());

			if(cp.getFirst().getFile().equals(fFile))
			{
				addAnnotation(cp.getFirst(), cp.getSecond(), cloneNumber);
				added = true;
			}
			if(cp.getSecond().getFile().equals(fFile))
			{
				addAnnotation(cp.getSecond(), cp.getFirst(), cloneNumber);
				added = true;
			}

			cloneNumber++;
			// We should always add at least one annotation per pair
			assert added ;
		}
	}

	@Override
	public void setProgressMonitor(IProgressMonitor monitor) {
		// TODO Where is this shown to the user?  Do we need it?

	}

	@Override
	/**
	 * Fully parses the whole document.  Should only be called if this is not
	 * an incremental reconciler.
	 */
	public void initialReconcile() {
		doReconcile(null);

	}

	//TODO: What other info is needed here?
	/** 
	 * Adds a clone annotation to the given source region
	 * 
	 *  @param source The region indicating where the annotation should appear
	 *  @param other The region containing the second (matched) clone, possibly
	 *  	in a different file
	 *  @param cloneNumber The number/id assigned to the clone
	 */
	private void addAnnotation(SourceRegion source, SourceRegion other, int cloneNumber)
	{
		CloneReconciler.reconcilerLog.debug("Adding annotation to source region");
		
		IResource res = getResource();

		IMarker cloneMarker;
		try {
			cloneMarker = res.createMarker(CLONE_MARKER);
			
			cloneMarker.setAttribute(CloneFixer.CLONE_NUMBER, cloneNumber);
			
			cloneMarker.setAttribute(CloneFixer.SOURCE_START_OFFSET, source.getStart().getGlobalOffset());
			cloneMarker.setAttribute(CloneFixer.SOURCE_END_OFFSET, source.getEnd().getGlobalOffset());
			cloneMarker.setAttribute(CloneFixer.SOURCE_TEXT, fDocument.get());
			
			cloneMarker.setAttribute(CloneFixer.OTHER_START_OFFSET, other.getStart().getGlobalOffset());
			cloneMarker.setAttribute(CloneFixer.OTHER_END_OFFSET, other.getEnd().getGlobalOffset());
			cloneMarker.setAttribute(CloneFixer.OTHER_FILE, other.getFile().getAbsolutePath());
		} catch (CoreException e) {
			CloneReconciler.reconcilerLog.error("Cannot add annotation to document, marker does not have required field", e);
			return;
		}
		
		fAnnotationModel.addAnnotation(
				new CloneAnnotation(cloneMarker), 
				new Position(source.getStart().getGlobalOffset(), source.getLength()));
	}

	/**
	 * Removes all annotations that overlap with the given offset range
	 * 
	 * @param start The offset into the current file at which the range starts
	 * @param end The offset into the current file at which the rang eends
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
				if(p.offset >= start || p.length + p.offset < end)
				{
					fAnnotationModel.removeAnnotation(an);
				}
			}
		}
		IResource res = getResource();
		try {
			res.deleteMarkers(CLONE_MARKER, true, IResource.DEPTH_INFINITE);
		} catch (CoreException e) {
			CloneReconciler.reconcilerLog.error("Could not delete markers from a previous round", e);
		}

	}



	/**
	 *  Initialize the detector based on the "detectorPreference" field 
	 *  */
	private void initializeDetector()
	{
		destroyDetector();

		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		String detectorPreference = store.getString(PreferenceConstants.P_CHOICE);
		CloneReconciler.reconcilerLog.debug("Changing detector to " + detectorPreference);
		if (detectorPreference.equalsIgnoreCase(JccdDetector.NAME)){
			detector = new JccdDetector();
		}else if (detectorPreference.equalsIgnoreCase(CheckStyleDetector.NAME)){
			detector = new CheckStyleDetector();
		}else if (detectorPreference.equalsIgnoreCase(SimianDetector.NAME)){
			detector = new SimianDetector();
		}
	}

	/**
	 * Perform cleanup on the active detector, if there is an active detector.
	 */
	public void destroyDetector(){
		if (detector != null){
			detector.destroy();			
		}
	}


	
	/** 
	 * Helper method to call FileUtil.convertOffset on the current
	 * document
	 * @throws BadLocationException if offset is not a global offset into the file
	 * 
	 * @see FileUtil.convertOffset()
	 */
	private SourceLocation convertOffset(int offset) throws BadLocationException
	{
		return new SourceLocation(fFile, offset, fDocument);
	}
	
	/** 
	 * Helper method to get the Resource of the current document 
	 * 
	 * @return the currently open document
	 */
	private IResource getResource()
	{
		return (IResource) fEditor.getEditorInput().getAdapter(IResource.class);
	}
	
	/** 
	 * Helper method to convert a File into a Document 
	 * 
	 * @param f a File, assumed to be open in the editor
	 * @return the corresponding document
	 * @throws IOException If file does not exist or is unreadable
	 */
	private Document fileToDocument(File f) throws IOException
	{
		Document d = new Document(FileUtil.read(f));
		return d;
	}

}
