package edu.washington.cs.rtrefactor.quickfix;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.ui.actions.IJavaEditorActionDefinitionIds;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;

import edu.washington.cs.rtrefactor.reconciler.ClonePairData;

/**
 * The quickfix which invokes the "Extract Method" command on the
 * system (other) side of the clone
 * @author Travis Mandel
 * @author Todd Schiller
 */
public class ExtractMethodFix extends CloneFix {

	public ExtractMethodFix(ClonePairData pairData, int relevance){
		super(pairData, relevance);
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
