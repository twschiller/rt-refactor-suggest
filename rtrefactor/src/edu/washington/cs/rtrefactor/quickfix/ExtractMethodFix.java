package edu.washington.cs.rtrefactor.quickfix;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.ui.actions.IJavaEditorActionDefinitionIds;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;

import edu.washington.cs.rtrefactor.detect.SourceRegion;
import edu.washington.cs.rtrefactor.reconciler.ClonePairData;

/**
 * The quickfix which invokes the "Extract Method" command on the
 * system (other) side of the clone
 * @author Travis Mandel
 * @author Todd Schiller
 */
public class ExtractMethodFix extends CloneFix {
	
	private final SourceRegion extractRegion;
	
	/**
	 * Instantiates a clone clone quick fix
	 * @param pairData The clone pair data
	 * @param relevance A score from 10-100 indicating the relevance of this suggestion
	 * @param parent The parent CloneFixer (can be null)
	 * @param extractRegion the source region to extract
	 */
	public ExtractMethodFix(ClonePairData pairData, int relevance, CloneResolutionGenerator parent, SourceRegion extractRegion){
		super(pairData, relevance, parent);
		this.extractRegion = extractRegion;
	}
	
	/**
	 * Requires a valid parent. {@inheritDoc}
	 */
	@Override
	public String getLabel() {
		getParent().notifyFixesActivated();
		if(isSameFile()) {
			return "Extract method with local clone" + super.getLabelDetails();
		} else {
			return "Extract method with clone from "+ 
			getOtherRegion().getFile().getName() + super.getLabelDetails();
		}
	}
	
	/**
	 * Requires a valid parent. {@inheritDoc}
	 */
	@Override
	public void run(IMarker marker) {
		getParent().notifyFixSelected(this);
		
		// jump to the region and invoke the Extract Method menu command
		
		JumpToFix.jumpToRegion(extractRegion, this.isSameFile());
		
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
		String description = CloneResolutionGenerator.getCloneString(
				extractRegion.getStart().getGlobalOffset(), 
				extractRegion.getEnd().getGlobalOffset(), 
				super.getOtherContents());
		
		if(isSameFile()) {
			return "Extracts this code to a method with the following clone " +
					"(from the same file): <br/>" 
					+ description;
		} else {
			return "Extracts this code to a method with the following clone (from "+ 
					getOtherRegion().getFile().getName()+  "):<br/>" 
					+ description;
		}
	}

}
