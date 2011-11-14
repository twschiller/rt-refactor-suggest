package edu.washington.cs.rtrefactor.quickfix;

import java.io.IOException;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;

import edu.washington.cs.rtrefactor.detect.SourceRegion;
import edu.washington.cs.rtrefactor.reconciler.CloneEditor;
import edu.washington.cs.rtrefactor.reconciler.CloneReconciler;
import edu.washington.cs.rtrefactor.util.FileUtil;

/**
 * The quickfix which copies & pastes the clone
 * 
 * Mostly unimplemented.
 * 
 * @author Travis Mandel
 *
 */
public class CopyPasteFix extends CloneFix {

	public CopyPasteFix(int cloneNumber, SourceRegion sourceClone, SourceRegion otherClone,
			String sourceContent, boolean isSameFile, int relevance) throws IOException {
		super(cloneNumber, sourceClone, otherClone, sourceContent, isSameFile, relevance);
	}

	@Override
	public String getLabel() {
		return "Copy and paste clone #" + getCloneNumber() + " (" + getRelevance() + ")";
	}
	
	@Override
	public void run(IMarker marker) {
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
