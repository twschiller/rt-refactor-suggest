package edu.washington.cs.rtrefactor.quickfix;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.dialogs.MessageDialog;

import edu.washington.cs.rtrefactor.detect.SourceRegion;

/**
 * The quickfix which extracts the clone pair to a method
 * 
 * Mostly unimplemented.
 * 
 * @author Travis Mandel
 *
 */
public class ExtractMethodFix extends CloneFix {

	public ExtractMethodFix(int cNumber, SourceRegion sourceClone, SourceRegion otherClone,
			String sourceContent, boolean isSameFile, int relevance) {
		super(cNumber, sourceClone, otherClone, sourceContent, isSameFile, relevance);
	}


	@Override
	public String getLabel() {
		return "Extract method with clone #" + getCloneNumber() + " (" + getRelevance() + ")";
	}
	
	@Override
	public void run(IMarker marker) {
		MessageDialog.openInformation(null, "Extract Method Demo",
				"This extract method quick-fix is not yet implemented");
	}

	@Override
	public String getDescription() {
		if(isSameFile())
			return "Extracts this code to a method with the following clone " +
					"(from the same file): <br/>" 
			+ super.getDescription();
		else
			return "Extracts this code to a method with the following clone (from "+ 
			getOtherRegion().getFile().getName()+  "):<br/>" 
			+ super.getDescription();
	}

}
