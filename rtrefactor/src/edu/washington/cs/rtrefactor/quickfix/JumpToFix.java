package edu.washington.cs.rtrefactor.quickfix;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.dialogs.MessageDialog;

import edu.washington.cs.rtrefactor.detect.SourceRegion;

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
		
		MessageDialog.openInformation(null, "Jump To Demo",
				"This jump to quick-fix is not yet implemented");
	}

	@Override
	public String getDescription() {
		if(isSameFile())
			return "Jumps to this clone in the same file: <br/>" 
			+ super.getDescription();
		else
			return "Jumps to this clone from "+ 
			getOtherCloneRegion().getFile().getName()+  ":<br/>" 
			+ super.getDescription();
	}

}
