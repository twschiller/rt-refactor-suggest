package edu.washington.cs.rtrefactor.quickfix;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;

import com.google.common.base.Joiner;

import edu.washington.cs.rtrefactor.reconciler.CloneEditor;
import edu.washington.cs.rtrefactor.reconciler.ClonePairData;

/**
 * The quickfix which inserts a call to the system (other) clone
 * @author Travis Mandel
 * @author Todd Schiller
 */
public class InsertCallFix extends CloneFix {

	public InsertCallFix(ClonePairData pairData, int relevance){
		super(pairData, relevance);
	}
	
	public InsertCallFix(ClonePairData pairData, int relevance, CloneFixer parent){
		super(pairData, relevance, parent);
	}

	@Override
	/**
	 * Requires a valid parent
	 */
	public String getLabel() {
		getParent().notifyFixesActivated();
		return "Insert Call to clone #" + getCloneNumber() + " (" + getRelevance() + ")";
	}
	
	@Override
	/**
	 * Requires a valid parent
	 */
	public void run(IMarker marker) {
		
		getParent().notifyFixSelected(this);
		
		IMethod m;
		try {
			m = FindMethod.findMethod(this.getOtherRegion());
		} catch (CoreException e) {
			MessageDialog.openError(null, "Insert Call", "Error accessing workspace resource: " + e.getMessage());
			return;
		}
		
	    //http://wiki.eclipse.org/FAQ_How_do_I_insert_text_in_the_active_text_editor%3F
		IEditorPart editor =  PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		IDocument doc = ((CloneEditor) editor).getDocumentProvider().getDocument(editor.getEditorInput());
			
		int start = this.getSourceRegion().getStart().getGlobalOffset();
		int len = this.getSourceRegion().getLength();
		
		try {
			doc.replace(start, len, m.getElementName() + "(" + Joiner.on(',').join(m.getParameterNames()) + ")");
		} catch (Exception e) {
			MessageDialog.openError(null, "Insert Call", "An error occured when inserting the method call: " + e.getMessage());
		}
	}

	@Override
	public String getDescription() {
		IMethod m;
		String source;
		String name;
		try {
			m = FindMethod.findMethod(this.getOtherRegion());
			name = m.getElementName();
			source = m.getSource();
		} catch (CoreException e) {
			return "An error occured when locating the method: " + e.getMessage();
		}
	
		// TODO fix this so that spacing and tabs work
		source = source.replaceAll("[\r\n]+", "<br/>");
	
		if(isSameFile()) {
			return "Replaces code with a call to " + name + " in this file:<br/>" + source;
		} else {
			return "Replaces code with a call to " + name + " in " + getOtherRegion().getFile().getName() + ":<br/>" 
			+ source;
		}
	}

}
