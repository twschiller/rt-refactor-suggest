package edu.washington.cs.rtrefactor.detect;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

import com.google.common.base.Predicate;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.io.Files;

/**
 * Common utility methods for code clone detectors
 * @author Todd Schiller
 */
public abstract class DetectorUtil {

	private static final String DETECTOR_LOG_NAME = "detector";
	public static final Logger detectLog = Logger.getLogger(DETECTOR_LOG_NAME);
	
	// TODO introduce types so that the BiMap type parameters aren't the same (i.e., qualified types?)
	
	/**
	 * Get all {@code .java} files in the workspace
	 * @param dirty the active buffer content for dirty files
	 * @return map from underlying {@code .java} buffers to files containing their content
	 * @throws CoreException iff a resource is not accessible
	 * @throws IOException iff a temporary file could not be created for a dirty buffer
	 */
	public static BiMap<File,File> collect(Map<File, String> dirty) throws CoreException, IOException{
		BiMap<File,File> files = HashBiMap.create();
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		collect(root, dirty, files);
		return files;
	}
	
	
	/**
	 * Add the {@code .java} files contained within {@code resource} to the map {@code files}
	 * @param resource the Eclipse resource
	 * @param dirty the active buffer content for dirty files
	 * @param files map from underlying {@code .java} buffers to files containing their content
	 * @throws CoreException iff a resource is not accessible
	 * @throws IOException iff a temporary file could not be created for a dirty buffer
	 */
	public static void collect(IResource resource, Map<File, String> dirty, BiMap<File,File> files) throws CoreException, IOException{
		if (resource instanceof IFile){
			IFile f = (IFile) resource;
			
			if (f.getFileExtension() != null && f.getFileExtension().equals("java")){
				
				boolean isDirty = false;
				
				for (File df : dirty.keySet()){
					if (df.equals(f.getLocation().toFile())){
						File tmp = File.createTempFile("rtrefactor", ".java");
						Files.write(dirty.get(df), tmp, Charset.defaultCharset());
						
						detectLog.debug("Registered temporary file " + tmp.getAbsolutePath() + " for dirty buffer " + df.getAbsolutePath());
						
						files.put(f.getLocation().toFile(), tmp);
						
						isDirty = true;
						break;
					}
				}
				
				if (!isDirty){
					files.put(f.getLocation().toFile(), f.getLocation().toFile());
				}
			}
		}
		else if (resource instanceof IFolder){
			for (IResource s : ((IFolder) resource).members(false)){
				collect(s, dirty, files);
			}
		}
	}
	
	/**
	 * Add the {@code .java} files contained within the workspace to the list {@code files}. Skips
	 * projects that are closed.
	 * @param workspace the workspace
	 * @param dirty the active buffer content for dirty files
	 * @param files map from underlying {@code .java} buffers to files containing their content
	 * @throws CoreException iff a resource is not accessible
	 * @throws IOException iff a temporary file could not be created for a dirty buffer
	 */
	public static void collect(IWorkspaceRoot workspace, Map<File, String> dirty, BiMap<File,File> files) throws CoreException, IOException{
		for (IProject project : workspace.getProjects()){
			if (project.isOpen()){
				for (IResource r : project.members(false)){
					collect(r, dirty, files);
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
	
	/**
	 * Calculate clone length, ignoring <i>Javadoc</i> comments and whitespace.
	 * @param content
	 */
	public static int cloneLength(String content){
	    String nws = content.replaceAll("\\s", "");
	    
	    while (nws.indexOf("/**") > 0){
	        int start = nws.indexOf("/**");
	        int end = nws.indexOf("*/", start);
	        
	        nws = end >= 0 
	                ? nws.substring(0, start) + nws.substring(end + "*/".length())
	                : nws.substring(0, start);
	    }
	    
	    return nws.length();
	}
}
