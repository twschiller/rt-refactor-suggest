package edu.washington.cs.rtrefactor.quickfix;

import java.io.IOException;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.ui.actions.IJavaEditorActionDefinitionIds;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;

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

	public ExtractMethodFix(int cloneNumber, SourceRegion sourceClone, SourceRegion otherClone,
			String sourceContent, boolean isSameFile, int relevance) throws IOException {
		super(cloneNumber, sourceClone, otherClone, sourceContent, isSameFile, relevance);
	}


	@Override
	public String getLabel() {
		return "Extract method with clone #" + getCloneNumber() + " (" + getRelevance() + ")";
	}
	
	@Override
	public void run(IMarker marker) {
		// jump to the region and invoke the Extract Method menu command
		
		JumpToFix.jumpToRegion(this.getOtherRegion(), this.isSameFile());
		
		try{
			IWorkbench workbench = PlatformUI.getWorkbench();
			ICommandService cs = (ICommandService) workbench.getService(ICommandService.class);
			
			ParameterizedCommand command = cs.deserialize(IJavaEditorActionDefinitionIds.EXTRACT_METHOD);
			
			IHandlerService hs = (IHandlerService) workbench.getService(IHandlerService.class);
			hs.executeCommand(command, null);
		}catch(Exception e){
			MessageDialog.openError(null, "Extract Method Demo", "Error opening the Extract Method dialog: " + e.getMessage());
			return;
		}
		
	}

	@Override
	public String getDescription() {
		if(isSameFile()) {
			return "Extracts this code to a method with the following clone " +
					"(from the same file): <br/>" 
			+ super.getDescription();
		} else {
			return "Extracts this code to a method with the following clone (from "+ 
			getOtherRegion().getFile().getName()+  "):<br/>" 
			+ super.getDescription();
		}
	}

}
