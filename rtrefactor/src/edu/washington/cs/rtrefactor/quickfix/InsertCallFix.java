package edu.washington.cs.rtrefactor.quickfix;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.dialogs.MessageDialog;

import edu.washington.cs.rtrefactor.detect.SourceRegion;

/**
 * The quickfix which inserts a call to the second clone's method
 * 
 * Mostly unimplemented.
 * 
 * @author Travis Mandel
 *
 */
public class InsertCallFix extends CloneFix {

	public InsertCallFix(int cNumber, SourceRegion otherClone, String dirtyContent,
			boolean isSameFile, int relevance) {
		super(cNumber, otherClone, dirtyContent, isSameFile, relevance);
	}

	public String getLabel() {
		return "Insert Call to clone #" + getCloneNumber() + " (" + getRelevance() + ")";
	}
	
	public void run(IMarker marker) {
		MessageDialog.openInformation(null, "Insert Call Demo",
				"This insert call quick-fix is not yet implemented");
	}

	@Override
	public String getDescription() {
		//TODO: Show method name!
		if(isSameFile())
			return "Replaces code with a call to a method in this file containing" +
					" the following clone: <br/>" 
			+ super.getDescription();
		else
			return "Replaces code with a call to a method in " + getOtherRegion().getFile().getName() + 
					" containing the following clone:<br/>" 
			+ super.getDescription();
	}

}
