package edu.washington.cs.rtrefactor.quickfix;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;

import edu.washington.cs.rtrefactor.quickfix.FindBlock.BlockInfo;
import edu.washington.cs.rtrefactor.quickfix.FindBlock.VariableCaptureCounter;
import edu.washington.cs.rtrefactor.reconciler.CloneEditor;
import edu.washington.cs.rtrefactor.reconciler.ClonePairData;
import edu.washington.cs.rtrefactor.reconciler.CloneReconciler;
import edu.washington.cs.rtrefactor.util.ASTUtil;

/**
 * The quickfix which copies & pastes the clone 
 * @author Travis Mandel
 * @author Todd Schiller
 */
public class CopyPasteFix extends CloneFix {

	public CopyPasteFix(ClonePairData pairData, int relevance, CloneFixer parent){
		super(pairData, relevance, parent);
	}

	
	/**
	 * Requires a valid parent. {@inheritDoc}
	 */
	@Override
	public String getLabel() {
		getParent().notifyFixesActivated();
		if(isSameFile()) {
			return "Copy and pastes local clone";
		} else {
			return "Copy and pastes clone from "+ 
			getOtherRegion().getFile().getName();
		}
	}
	
	
	/**
	 * Requires a valid parent. {@inheritDoc}
	 */
	@Override
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
			TextEdit te = replaceWithVariablesFrom(origVars, current, other, doc);
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
	
	/**
	 * <p>Replace any names in this block matching the external variables in the other block 
	 * with the specified variables names.</p>
	 * 
	 * <p>The variables to be replaced in this block do not need to be properly bound.</p>
	 * 
	 * <p>Currently order is used to determine the mapping between the two sets of variables.</p>
	 * 
	 * @param replaceVars Variable name replacements to be used.
	 * @param other The other block to read external variable names from
	 * @param original The document in which this block resides
	 * @return An edit to this document with the requested replacements.
	 */
	public TextEdit replaceWithVariablesFrom(LinkedHashSet<String> replaceVars, 
		BlockInfo before, BlockInfo other, IDocument original)
	{
		//Find external dependencies in the other block to know which variables to replace.
		VariableCaptureCounter otherCounter = new VariableCaptureCounter();
		for (Statement s : other.getStatements()){
			s.accept(otherCounter);
		}
		
		//Create the mapping between variables.  Currently only order is used.
		HashMap<String, String> variableReplacements = new HashMap<String, String>();
		Iterator<String> otherVars =  otherCounter.captured.iterator();
		for(String sourceVar : replaceVars){
			String otherVar = otherVars.next();
			CloneReconciler.reconcilerLog.debug("Replacing " + otherVar + " with " + sourceVar);
			variableReplacements.put(otherVar, sourceVar);
		}
		
		CompilationUnit myUnit = before.getCompilationUnit();
		
		//start recording modifications
		myUnit.recordModifications();
		
		//Modify the AST with the name replacements
		VariableReferenceReplacer replacer = 
				new VariableReferenceReplacer(variableReplacements);
		
		for (Statement s : before.getStatements()){
			s.accept(replacer);
		}
		
		return myUnit.rewrite(original, null);
	}
	
	
	/**
	 * Replaces one set of variable names in the AST with another
	 * @author Travis Mandel
	 */
	protected static class VariableReferenceReplacer extends ASTVisitor{
		
		private final Map<String, String> replacements;
		
		/**
		 * Constructor taking a replacement map
		 * @param refs A mapping from original variable names to replacements. 
		 */
		public VariableReferenceReplacer(Map<String, String> refs) {
			replacements = refs;
		}
		
		
		@Override
		public boolean visit(SimpleName name){
			doName(name);
			return false;
		}
		
		@Override
		public boolean visit(QualifiedName name){
			doName(name);
			return false;
		}
		
		/**
		 * Replaces the variable name with the new name if it is in our map.  
		 * @param name the name of variable
		 */
		private void doName(Name name){
			if (!(name.getParent() instanceof VariableDeclarationFragment)){
				//May not have any binding info, so no reason to do further checks
				String replacementName = replacements.get(name.getFullyQualifiedName());
				if (replacementName != null){
					ASTUtil.replace(name, name.getAST().newName(replacementName));
				}
				
			}
		}
	}
}
