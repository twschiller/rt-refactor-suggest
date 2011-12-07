package edu.washington.cs.rtrefactor.quickfix;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;

import edu.washington.cs.rtrefactor.detect.SourceRegion;
import edu.washington.cs.rtrefactor.reconciler.CloneEditor;
import edu.washington.cs.rtrefactor.reconciler.ClonePairData;
import edu.washington.cs.rtrefactor.reconciler.CloneReconciler;

/**
 * The quickfix which simply jumps to (and selects) the system (other) clone
 * @author Travis Mandel
 * @author Todd Schiller
 */
public class JumpToFix extends CloneFix {
	
	public JumpToFix(ClonePairData pairData, int relevance, CloneResolutionGenerator parent){
		super(pairData, relevance, parent);
	}

	/**
	 * Requires a valid parent. {@inheritDoc}
	 */
	@Override
	public String getLabel() {
		getParent().notifyFixesActivated();
		if(isSameFile()) {
			return "Jump to local clone";
		} else {
			return "Jump to clone from "+ 
			getOtherRegion().getFile().getName();
		}
	}
	
	/**
	 * Jump to the region and select it, opening a new buffer iff <code>isSameFile == false</code>
	 * @param region the region to jump to
	 * @param isSameFile true iff the region is in the active buffer
	 */
	protected static void jumpToRegion(SourceRegion region, boolean isSameFile){
	
		int start = region.getStart().getGlobalOffset();
		int line =  region.getStart().getLine();
		int len = region.getLength();
		
		//http://wiki.eclipse.org/FAQ_How_do_I_open_an_editor_on_a_file_in_the_workspace%3F
		//http://wiki.eclipse.org/FAQ_How_do_I_open_an_editor_programmatically%3F
		
		if (isSameFile){
			//http://stackoverflow.com/questions/1619623/eclipse-plugin-how-to-get-current-text-editor-corsor-position
			
			IEditorPart editor =  PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
			IDocument doc = ((CloneEditor) editor).getDocumentProvider().getDocument(editor.getEditorInput());
			
			ITextEditor txt = ((ITextEditor) editor);
			txt.getSelectionProvider().setSelection(new TextSelection(doc, start, len ));
	
			CloneReconciler.reconcilerLog.debug("Jumped to clone at line " + line);
		}else{
			
			IPath p = new Path(region.getFile().getAbsolutePath());
			IFile f = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(p);
			
			//http://wiki.eclipse.org/FAQ_How_do_I_find_the_active_workbench_page%3F
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			
			try{
				IMarker m = f.createMarker(IMarker.TEXT);
				m.setAttribute(IMarker.LINE_NUMBER, line);
				IEditorPart editor = IDE.openEditor(page, m);
				m.delete();
				
				ITextEditor txt = ((ITextEditor) editor);
				IDocument doc = ((CloneEditor) editor).getDocumentProvider().getDocument(editor.getEditorInput());
				
				txt.getSelectionProvider().setSelection(new TextSelection(doc, start, len ));
				
				CloneReconciler.reconcilerLog.debug("Jumped to clone at line " + line + " in file " + f.getName());
				
			}catch(CoreException e){
				MessageDialog.openError(null, "Jump To Clone",
						"Error jumping to the system clone: " + e.getMessage());
				
				CloneReconciler.reconcilerLog.error("Error jumping to clone in file " + f.getName(), e);
			}
		}
	}
	

	/**
	 * Requires a valid parent. {@inheritDoc}
	 */
	@Override 
	public void run(IMarker marker) {
		
		getParent().notifyFixSelected(this);
		
		jumpToRegion(super.getOtherRegion(), super.isSameFile());
	}

	@Override
	public String getDescription() {
		if(isSameFile()) {
			return "Jumps to this clone in the same file: <br/>" 
			+ super.getDescription();
		} else {
			return "Jumps to this clone from "+ 
			getOtherRegion().getFile().getName()+  ":<br/>" 
			+ super.getDescription();
		}
	}

}
