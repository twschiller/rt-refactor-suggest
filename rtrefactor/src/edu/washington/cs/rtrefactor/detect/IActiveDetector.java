package edu.washington.cs.rtrefactor.detect;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;

/**
 * A detector that can detect code that is similar to code within the active region
 * @author Todd Schiller
 */
public interface IActiveDetector {

	/**
	 * Get the set of code clone pairs for which one of the clones resides in the specified active region
	 * @param dirty the active buffer content for dirty files
	 * @param active the active region
	 * @return the set of code clone pairs
	 * @throws CoreException iff there is an error accessing a resource
	 * @throws IOException iff a temporary file could not be created for a dirty buffer
	 */
	Set<ClonePair> detect(Map<File, String> dirty, SourceRegion active) throws CoreException, IOException;
	
	/**
	 * Get the name of the detector
	 * @return the name of the detector
	 */
	String getName();
	
	/**
	 * Perform cleanup on the detector
	 */
	void destroy();
}
