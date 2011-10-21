package edu.washington.cs.rtrefactor.detect;

import java.io.File;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;

/**
 * A detector that can detect similar code across the whole code base
 * @author Todd Schiller
 */
public interface IDetector {

	/**
	 * Get the set of code clone pairs across the whole workspace
	 * @param dirty the active buffer content for dirty files
	 * @return the set of code clone pairs
	 * @throws CoreException iff there is an error accessing a resource
	 */
	Set<ClonePair> detect(Map<File, String> dirty) throws CoreException;
	
	/**
	 * Get the name of the detector
	 * @return the name of the detector
	 */
	String getName();
}
