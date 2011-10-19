package edu.washington.cs.rtrefactor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.IQuickFixProcessor;

/**
 * A shell Quick Fix hook (to add quick fix suggestions for a given problem type).
 * Added mostly to add the needed references to the project

 * http://www.eclipse.org/forums/index.php/mv/tree/68017/#page_top
 * We currently have a gap here: quick fix indication for warnings and 
 * errors is hard-coded to the "org.eclipse.jdt.ui.warning" and 
 * "org.eclipse.jdt.ui.error" types (excluding sub-types) i.e. if you 
 * create markers of that type you should get the light bulb.
 * 
 * @author Todd Schiller
 */
// TODO activate this hook in the activator?
public class CloneQuickFixHook implements IQuickFixProcessor{

	public CloneQuickFixHook(){
		
	}
	
	@Override
	public boolean hasCorrections(ICompilationUnit unit, int problemId) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public IJavaCompletionProposal[] getCorrections(IInvocationContext context,
			IProblemLocation[] locations) throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

}
