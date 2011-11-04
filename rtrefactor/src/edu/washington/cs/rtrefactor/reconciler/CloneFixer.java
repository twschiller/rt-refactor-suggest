package edu.washington.cs.rtrefactor.reconciler;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator;

public class CloneFixer implements IMarkerResolutionGenerator {
	
    public IMarkerResolution[] getResolutions(IMarker mk) {
        System.out.println("marker res called!");
           String problem = "";
		try {
			problem = (String) mk.getAttribute("cloneArea");
		} catch (CoreException e) {
			e.printStackTrace();
		}
		System.out.println("Problem is: " + problem);
           return new IMarkerResolution[] {
              new CloneFix("Fix #1 for "+problem),
              new CloneFix("Fix #2 for "+problem),
           };
        
     }

}
