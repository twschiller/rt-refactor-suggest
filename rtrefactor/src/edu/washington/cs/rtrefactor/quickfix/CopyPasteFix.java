package edu.washington.cs.rtrefactor.quickfix;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.dialogs.MessageDialog;

import edu.washington.cs.rtrefactor.detect.SourceRegion;

/**
 * The quickfix which copies & pastes the clone
 * 
 * Mostly unimplemented.
 * 
 * @author Travis Mandel
 *
 */
public class CopyPasteFix extends CloneFix {

	public CopyPasteFix(int cNumber, SourceRegion otherClone, String dirtyContent,
			boolean isSameFile, int relevance) {
		super(cNumber, otherClone, dirtyContent, isSameFile, relevance);
	}

	public String getLabel() {
		return "Copy and paste clone #" + getCloneNumber() + " (" + getRelevance() + ")";
	}
	
	public void run(IMarker marker) {
		MessageDialog.openInformation(null, "Copy & Paste Demo",
				"This copy and paste quick-fix is not yet implemented");
	}

	@Override
	public String getDescription() {
		if(isSameFile())
			return "Copy and pastes this clone from the same file: <br/>" 
			+ super.getDescription();
		else
			return "Copy and pastes this clone from "+ 
			getOtherRegion().getFile().getName()+  ":<br/>" 
			+ super.getDescription();
	}

}
