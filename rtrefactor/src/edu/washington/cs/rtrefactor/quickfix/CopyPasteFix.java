package edu.washington.cs.rtrefactor.quickfix;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;

import edu.washington.cs.rtrefactor.reconciler.CloneEditor;
import edu.washington.cs.rtrefactor.reconciler.ClonePairData;
import edu.washington.cs.rtrefactor.reconciler.CloneReconciler;
import edu.washington.cs.rtrefactor.util.FileUtil;

/**
 * The quickfix which copies & pastes the clone 
 * @author Travis Mandel
 * @author Todd Schiller
 */
public class CopyPasteFix extends CloneFix {

	public CopyPasteFix(ClonePairData pairData, int relevance, CloneFixer parent){
		super(pairData, relevance, parent);
	}

	@Override
	/**
	 * Requires a valid parent
	 */
	public String getLabel() {
		getParent().notifyFixesActivated();
		return "Copy and paste clone #" + getCloneNumber() + " (" + getRelevance() + ")";
	}
	
	@Override
	/**
	 * Requires a valid parent
	 */
	public void run(IMarker marker) {
		getParent().notifyFixSelected(this);
		
		//http://wiki.eclipse.org/FAQ_How_do_I_insert_text_in_the_active_text_editor%3F
		
		IEditorPart editor =  PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		IDocument doc = ((CloneEditor) editor).getDocumentProvider().getDocument(editor.getEditorInput());
		
		CloneReconciler.reconcilerLog.debug(
				"Copying clone from " + this.getOtherRegion().getFile().getName() + " (line: " + this.getOtherRegion().getStart().getLine() + ")" +
				" to " + this.getSourceRegion().getFile().getName() + " (line: " + this.getSourceRegion().getStart().getLine() + ")"
				);
			
		int start = this.getSourceRegion().getStart().getGlobalOffset();
		int len = this.getSourceRegion().getLength();
		
		try {
			doc.replace(start, len, FileUtil.get(this.getOtherContents(), this.getOtherRegion()));
		} catch (BadLocationException e) {
			MessageDialog.openError(null, "Paste Clone", "An error occured when pasting the clone");
		}
		
	}

	@Override
	public String getDescription() {
		if(isSameFile()) {
			return "Copy and pastes this clone from the same file: <br/>" 
			+ super.getDescription();
		} else {
			return "Copy and pastes this clone from "+ 
			getOtherRegion().getFile().getName()+  ":<br/>" 
			+ super.getDescription();
		}
	}

}
