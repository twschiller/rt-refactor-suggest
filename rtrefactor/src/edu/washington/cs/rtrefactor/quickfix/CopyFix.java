package edu.washington.cs.rtrefactor.quickfix;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolution2;

import edu.washington.cs.rtrefactor.detect.SourceRegion;
import edu.washington.cs.rtrefactor.util.FileUtil;





//TODO: This should be several classes
public class CopyFix implements IMarkerResolution2{
	
	private int cloneNumber;
	private SourceRegion otherRegion;
	private String dirtyText;
	private String otherContent;
	private boolean sameFile;
    
    public CopyFix(int cNumber, SourceRegion otherClone, String dirtyContent, 
    		boolean isSameFile) {
		cloneNumber = cNumber;
		otherRegion = otherClone;
		dirtyText = dirtyContent;
		sameFile= isSameFile;
		if(!sameFile)
			otherContent = FileUtil.readFileToString(otherRegion.getFile());
		else
			otherContent = dirtyText;
    }
    public String getLabel() {
       return "Copy and paste clone #" + cloneNumber;
    }
    public void run(IMarker marker) {
       MessageDialog.openInformation(null, "QuickFix Demo",
          "This quick-fix is not yet implemented");
    }
	@Override
	public String getDescription() {
		String otherClone;
		otherClone = CloneFixer.getCloneString(otherRegion.getStart().getOffset(), 
					otherRegion.getEnd().getOffset(), otherContent);
		
		if(sameFile)
			return "Copy and pastes this clone from the same file: <br/>" + otherClone;
		else
			return "Copy and pastes this clone from "+otherRegion.getFile().getName()+ 
					":<br/>" + otherClone;
	}
	@Override
	public Image getImage() {
		//TODO: Insert cool graphics here
		
		return null;
	}
	

}
