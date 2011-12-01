package edu.washington.cs.rtrefactor.quickfix;

import java.util.LinkedHashSet;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;

import edu.washington.cs.rtrefactor.quickfix.FindBlock.BlockInfo;
import edu.washington.cs.rtrefactor.reconciler.CloneEditor;
import edu.washington.cs.rtrefactor.reconciler.ClonePairData;
import edu.washington.cs.rtrefactor.reconciler.CloneReconciler;

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
		if(isSameFile()) {
			return "Copy and pastes local clone";
		} else {
			return "Copy and pastes clone from "+ 
			getOtherRegion().getFile().getName();
		}
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
			//Save the names of the variables originally in this source
			BlockInfo origSource = FindBlock.findLargestBlock(this.getSourceRegion());
			LinkedHashSet<String> origVars = origSource.getCapturedVariables();
			
			//Get the block containing the clone
			BlockInfo other = FindBlock.findLargestBlock(this.getOtherRegion());
			String otherBlockText = this.getOtherContents().substring(other.getStart(), other.getEnd());
			
			// Paste the cloned block verbatim
			doc.replace(start, len, otherBlockText);
			
			//Replace the variable names in the new block with the old names.
			BlockInfo current = FindBlock.findLargestBlock(this.getSourceRegion());
			TextEdit te = current.replaceWithVariablesFrom(origVars, other, doc);
			te.apply(doc);
			
		} catch (BadLocationException e) {
			MessageDialog.openError(null, "Paste Clone", "An error occured when pasting the clone");
			CloneReconciler.reconcilerLog.error("Bad location when copy & pasting " + e.getMessage());
		} catch (CoreException e) {
			MessageDialog.openError(null, "Paste Clone", "An error occured when pasting the clone");
			CloneReconciler.reconcilerLog.error("Bad file when copy & pasting " + e.getMessage());
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
