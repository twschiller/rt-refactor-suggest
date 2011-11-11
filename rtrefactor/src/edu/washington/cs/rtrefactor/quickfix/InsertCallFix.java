package edu.washington.cs.rtrefactor.quickfix;

import java.io.IOException;

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

	public InsertCallFix(int cNumber, SourceRegion sourceClone, SourceRegion otherClone,
			String sourceContent, boolean isSameFile, int relevance) throws IOException {
		super(cNumber, sourceClone, otherClone, sourceContent, isSameFile, relevance);
	}

	@Override
	public String getLabel() {
		return "Insert Call to clone #" + getCloneNumber() + " (" + getRelevance() + ")";
	}
	
	@Override
	public void run(IMarker marker) {
		MessageDialog.openInformation(null, "Insert Call Demo",
				"This insert call quick-fix is not yet implemented");
	}

	@Override
	public String getDescription() {
		//TODO: Show method name!
		if(isSameFile()) {
			return "Replaces code with a call to a method in this file containing" +
					" the following clone: <br/>" 
			+ super.getDescription();
		} else {
			return "Replaces code with a call to a method in " + getOtherRegion().getFile().getName() + 
					" containing the following clone:<br/>" 
			+ super.getDescription();
		}
	}

}
