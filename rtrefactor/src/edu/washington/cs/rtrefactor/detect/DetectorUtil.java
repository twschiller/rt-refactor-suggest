package edu.washington.cs.rtrefactor.detect;

import java.io.File;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

/**
 * Common utility methods for code clone detectors
 * @author Todd Schiller
 */
public abstract class DetectorUtil {

	/**
	 * Get all {@code .java} files in the workspace
	 * @return all {@code .java} files in the workspace
	 * @throws CoreException iff a resource is not accessible
	 */
	public static List<File> collect() throws CoreException{
		List<File> files = Lists.newArrayList();
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		collect(root, files);
		return files;
	}
	
	
	/**
	 * Add the {@code .java} files contained within {@code resource} to the list {@code files}
	 * @param resource the Eclipse resource
	 * @param files a non-null list of files
	 * @throws CoreException iff a resource is not accessible
	 */
	public static void collect(IResource resource, List<File> files) throws CoreException{
		if (resource instanceof IFile){
			IFile f = (IFile) resource;
			
			if (f.getFileExtension() != null && f.getFileExtension().equals("java")){
				files.add(f.getLocation().toFile());
			}
		}
		else if (resource instanceof IFolder){
			for (IResource s : ((IFolder) resource).members(false)){
				collect(s, files);
			}
		}
	}
	
	/**
	 * Add the {@code .java} files contained within the workspace to the list {@code files}. Skips
	 * projects that are closed.
	 * @param workspace the workspace
	 * @throws CoreException iff a resource is not accessible
	 */
	public static void collect(IWorkspaceRoot workspace, List<File> files) throws CoreException{
		for (IProject project : workspace.getProjects()){
			if (project.isOpen()){
				for (IResource r : project.members(false)){
					collect(r, files);
				}
			}
		}
	}
	
	/**
	 * Predicate for filtering out non-active regions
	 * @author Todd Schiller
	 */
	public static class ActiveRegion implements Predicate<ClonePair>{
		private final SourceRegion active;
		
		public ActiveRegion(SourceRegion active) {
			super();
			this.active = active;
		}

		@Override
		public boolean apply(ClonePair pair) {
			return active.overlaps(pair.getFirst()) || active.overlaps(pair.getSecond());
		}
	}
	
}
