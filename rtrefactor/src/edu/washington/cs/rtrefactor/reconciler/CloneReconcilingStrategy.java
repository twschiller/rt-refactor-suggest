package edu.washington.cs.rtrefactor.reconciler;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
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
import edu.washington.cs.rtrefactor.scorer.Scorer;

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

	private static int currentCloneNumber = 1;
	
	private int numAnnotations;
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
		numAnnotations = 0;
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
		//Retrieve data about each deletion
		List<DeletedAnnotationData> removedAnnotations= removeAnnotations(active.getStart().getGlobalOffset(), 
									active.getEnd().getGlobalOffset());

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
		for(ClonePair cp : hs)
		{
			
			
			int cloneNumber = -1;
			boolean found = false;
			//Check if this clone pair is equivalent to an old one, if so, reuse the old number
			for(DeletedAnnotationData oldAnnotation : removedAnnotations)
			{
				if(marksClone(oldAnnotation, cp))
				{
					cloneNumber = oldAnnotation.getCloneNumber();
					found = true;
					CloneReconciler.reconcilerLog.debug("Re-marking old clone pair, number " + 
							cloneNumber);
					break;
				}
			}
			
			//If it's a new pair, assign it a new number
			if(!found)
			{
				cloneNumber = currentCloneNumber;
				currentCloneNumber++;
				CloneReconciler.reconcilerLog.debug("Marking new clone pair with number "+cloneNumber 
						+" in files: " + cp.getFirst().getFile().getName() + " " + 
						cp.getSecond().getFile().getName());
			} 
			
			//Check: Are there any fixes for this clone?  If not, don't show
			ClonePairData cpData;
			try {
				cpData = cp.toClonePairData(fFile, cloneNumber, fDocument.get());
			} catch (IOException e) {
				throw new RuntimeException("Can't read file indicated by clone detector " + e.getMessage());
			}
			if(Scorer.getInstance().calculateResolutions(cpData).size() <= 0){
				continue;
			}
			
			boolean added = false;
			if(cp.getFirst().getFile().equals(fFile))
			{
				addAnnotation(cp.getFirst(), cp.getSecond(), cloneNumber, cp.getScore());
				added = true;
			}
			if(cp.getSecond().getFile().equals(fFile))
			{
				addAnnotation(cp.getSecond(), cp.getFirst(), cloneNumber, cp.getScore());
				added = true;
			}
			numAnnotations++;

			// We should always add at least one annotation per pair
			assert added ;
		}
	}
	
	/**
	 * Returns true iff the either region of the clone pair is pointed to by the clone marker
	 *  
	 *  For now, checks if region is wholly subsumed by the pair (to account for expanding clones).
	 *  
	 *  Because it uses data collected from annotation, robust to text inserted between detection
	 *  phases.
	 *  
	 * @param marker A clone marker on the current document
	 * @param pair A detected clone pair
	 * @return true iff the first region of the clone pair is pointed to by the clone marker
	 */
	private boolean marksClone(DeletedAnnotationData oldAnnotation, ClonePair pair) {
		int oldStart = oldAnnotation.getPos().offset;
		int oldEnd = oldStart + oldAnnotation.getPos().length;
		for(int c=0; c<2; c++)
		{
			SourceRegion newRegion = (c==0) ? pair.getFirst() : pair.getSecond();
			if(oldStart >= newRegion.getStart().getGlobalOffset()
					&& oldEnd <= newRegion.getEnd().getGlobalOffset()) {
				return true;
			}
		}
		
		return false;
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
	 *  @param similarity the similarity between the clones, measured by the detector
	 */
	private void addAnnotation(SourceRegion source, SourceRegion other, int cloneNumber,
			 double similarity)
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
			
			cloneMarker.setAttribute(CloneFixer.CLONE_SIMILARITY, Double.toString(similarity));
		} catch (CoreException e) {
			CloneReconciler.reconcilerLog.error("Cannot add annotation to document, marker does not have required field", e);
			return;
		}
		
		fAnnotationModel.addAnnotation(
				new CloneAnnotation(cloneMarker, numAnnotations), 
				new Position(source.getStart().getGlobalOffset(), source.getLength()));
	}

	/**
	 * Removes all annotations that overlap with the given offset range
	 * 
	 * @param start The offset into the current file at which the range starts
	 * @param end The offset into the current file at which the rang ends
	 * @return an array of clone markers that we deleted from the document
	 */
	private List<DeletedAnnotationData> removeAnnotations(int start, int end)
	{
		List<DeletedAnnotationData> removedAnnotations = new LinkedList<DeletedAnnotationData>();
		Iterator<Annotation> it = fAnnotationModel.getAnnotationIterator();
		while(it.hasNext())
		{
			Annotation an = it.next();
			if(an instanceof CloneAnnotation)
			{
				Position p = fAnnotationModel.getPosition(an);
				if(p.offset >= start || p.length + p.offset < end)
				{
					int cloneNumber = -1;
					try {
						cloneNumber = (Integer) ((CloneAnnotation) an).getMarker().getAttribute(CloneFixer.CLONE_NUMBER);
					} catch (CoreException e) {
						throw new RuntimeException("Marker attached to clone annotation has no clone number!" + 
									e.getMessage());
					}
					removedAnnotations.add(new DeletedAnnotationData(p, cloneNumber));
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
		return removedAnnotations;

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

}
