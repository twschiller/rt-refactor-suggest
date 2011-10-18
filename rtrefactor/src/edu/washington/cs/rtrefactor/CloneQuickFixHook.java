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
