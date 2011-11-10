package edu.washington.cs.rtrefactor.quickfix;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import edu.washington.cs.rtrefactor.detect.SourceRegion;
import edu.washington.cs.rtrefactor.reconciler.CloneEditor;
import edu.washington.cs.rtrefactor.reconciler.CloneReconciler;

/**
 * The quickfix which simply jumps to the second clone
 * 
 * Mostly unimplemented.
 * 
 * @author Travis Mandel
 *
 */
public class JumpToFix extends CloneFix {

	public JumpToFix(int cNumber, SourceRegion otherClone, SourceRegion sourceClone,
			String dirtyContent, boolean isSameFile, int relevance) {
		super(cNumber, otherClone, sourceClone, dirtyContent, isSameFile, relevance);
	}


	public String getLabel() {
		return "Jump to clone #" + getCloneNumber() + " (" + getRelevance() + ")";
	}
	
	public void run(IMarker marker) {
		int line =  this.getOtherRegion().getStart().getLine();
		
		//http://wiki.eclipse.org/FAQ_How_do_I_open_an_editor_on_a_file_in_the_workspace%3F
		//http://wiki.eclipse.org/FAQ_How_do_I_open_an_editor_programmatically%3F
		
		if (isSameFile()){
			//http://stackoverflow.com/questions/1619623/eclipse-plugin-how-to-get-current-text-editor-corsor-position
			
			IEditorPart editor =  PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
			IDocument doc = ((CloneEditor) editor).getDocumentProvider().getDocument(editor.getEditorInput());
			
			MessageDialog.openInformation(null, "Jump To Clone",
					"This jump to quick-fix is not yet for clones in the same file");
		}else{
			
			IPath p = new Path(this.getOtherRegion().getFile().getAbsolutePath());
			IFile f = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(p);
			
			//http://wiki.eclipse.org/FAQ_How_do_I_find_the_active_workbench_page%3F
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			
			try{
				IMarker m = f.createMarker(IMarker.TEXT);
				marker.setAttribute(IMarker.LINE_NUMBER, line);
				IDE.openEditor(page, m);
				m.delete();
				
				CloneReconciler.reconcilerLog.debug("Jumped to clone at line " + line + " in file " + f.getName());
				
			}catch(CoreException e){
				MessageDialog.openError(null, "Jump To Clone",
						"Error jumping to the system clone: " + e.getMessage());
				
				CloneReconciler.reconcilerLog.error("Error jumping to clone in file " + f.getName(), e);
			}
		
		}
	}

	@Override
	public String getDescription() {
		if(isSameFile())
			return "Jumps to this clone in the same file: <br/>" 
			+ super.getDescription();
		else
			return "Jumps to this clone from "+ 
			getOtherRegion().getFile().getName()+  ":<br/>" 
			+ super.getDescription();
	}

}
