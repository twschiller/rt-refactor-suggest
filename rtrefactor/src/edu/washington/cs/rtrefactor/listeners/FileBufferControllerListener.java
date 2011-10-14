package edu.washington.cs.rtrefactor.listeners;

import org.eclipse.core.filebuffers.IFileBuffer;
import org.eclipse.core.filebuffers.IFileBufferListener;
import org.eclipse.core.runtime.IPath;

/**
 * Example Listener for the Buffer Controller (e.g., new buffer created).
 * This is a good place to control document listeners
 * @author Todd Schiller
 */
public class FileBufferControllerListener implements IFileBufferListener {

	@Override
	public void bufferCreated(IFileBuffer buffer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void bufferDisposed(IFileBuffer buffer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void bufferContentAboutToBeReplaced(IFileBuffer buffer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void bufferContentReplaced(IFileBuffer buffer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stateChanging(IFileBuffer buffer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dirtyStateChanged(IFileBuffer buffer, boolean isDirty) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stateValidationChanged(IFileBuffer buffer,
			boolean isStateValidated) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void underlyingFileMoved(IFileBuffer buffer, IPath path) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void underlyingFileDeleted(IFileBuffer buffer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stateChangeFailed(IFileBuffer buffer) {
		// TODO Auto-generated method stub
		
	}

}
