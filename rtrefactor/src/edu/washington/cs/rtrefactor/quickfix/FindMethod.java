package edu.washington.cs.rtrefactor.quickfix;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import com.google.common.collect.HashMultiset;

import edu.washington.cs.rtrefactor.detect.SourceRegion;

/**
 * Utility methods for finding the methods corresponding to source
 * locations
 * @author Todd Schiller
 */
public class FindMethod {

	/**
	 * Get the surrounding method for a source element, or null iff
	 * the element is not surrounded by a method.
	 * @param elt the java source element
	 * @return the surrounding method for the element
	 */
	private static IMethod getSurroundingMethod(IJavaElement elt){
		if (elt.getElementType() == IJavaElement.METHOD){
			return (IMethod) elt;
		}else if (elt.getParent() != null){
			return getSurroundingMethod(elt.getParent());
		}else{
			return null;
		}
	}
	
	/**
	 * Get the method that corresponds to the given <code>region</code>. If the region
	 * overlaps multiple methods, returns the method that has the most overlapping
	 * characters
	 * @param region the query region
	 * @param cu the compilation unit
	 * @return the method that corresponds to the given <code>region</code>
	 */
	public static IMethod findMethod(SourceRegion region, ICompilationUnit cu){
		if (!cu.getElementName().equals(region.getFile().getName())){
			throw new IllegalArgumentException("File name for source region query does not match the compilation unit's name");
		}
		
		HashMultiset<IMethod> elts = HashMultiset.create();
		
		for (int i = region.getStart().getGlobalOffset(); i < region.getEnd().getGlobalOffset(); i++){
			try{
				IMethod m = getSurroundingMethod(cu.getElementAt(i));
				if (m != null){
					elts.add(m);
				}
			}catch (JavaModelException ex){
				// ignore
			}
		}
		
		Integer max = Integer.MIN_VALUE;
		IMethod maxE = null;
		for (IMethod elt : elts){
			if (elts.count(elt) > max){
				max = elts.count(elt);
				maxE = elt;
			}
		}
		
		return maxE;
	}
	
	/**
	 * Get the method that corresponds to the given <code>region</code> in the Eclipse
	 * workspace. Utilizes the {@link FindMethod#findMethod(SourceRegion, ICompilationUnit)}
	 * method to perform lookup. Returns <code>null</code> if the region does not correspond to 
	 * a method.
	 * @param region the query region
	 * @return the method that corresponds to the given <code>region</code> in the Eclipse workspace
	 * @throws CoreException iff an error occurred when accessing a workspace resource
	 */
	public static IMethod findMethod(SourceRegion region) throws CoreException{
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		
		// Get all projects in the workspace
		IProject[] projects = root.getProjects();
		// Loop over all projects
		for (IProject project : projects) {

			// Only work on open projects with the Java nature
			if (project.isOpen()
					&& project.isNatureEnabled("org.eclipse.jdt.core.javanature")) {
				IJavaProject javaProject = JavaCore.create(project);
				
				for (IPackageFragment p : javaProject.getPackageFragments())
				{
					if (p.getKind() == IPackageFragmentRoot.K_SOURCE) {
						for (ICompilationUnit cu : p.getCompilationUnits()){
							if (cu.getElementName().equals(region.getFile().getName())){
								return findMethod(region, cu);
							}
						}
					}
				}
			}
		}
		throw new RuntimeException("Compilation unit corresponding to query file " + region.getFile().getAbsolutePath() + " not found");
	}	
}
