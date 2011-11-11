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

	public CopyPasteFix(int cNumber, SourceRegion otherClone, SourceRegion sourceClone,
			String dirtyContent, boolean isSameFile, int relevance) throws IOException {
		super(cNumber, otherClone, sourceClone, dirtyContent, isSameFile, relevance);
	}

	public String getLabel() {
		return "Copy and paste clone #" + getCloneNumber() + " (" + getRelevance() + ")";
	}
	
	public void run(IMarker marker) {
		//http://wiki.eclipse.org/FAQ_How_do_I_insert_text_in_the_active_text_editor%3F
		
		IEditorPart editor =  PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		IDocument doc = ((CloneEditor) editor).getDocumentProvider().getDocument(editor.getEditorInput());
		
		int start = this.getSourceRegion().getStart().getGlobalOffset();
		int len =  this.getSourceRegion().getEnd().getGlobalOffset() - start;
		
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
