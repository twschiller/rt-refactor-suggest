package edu.washington.cs.rtrefactor;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.commands.NotHandledException;

/**
 * A demo class that logs Eclipse actions
 * @author Todd Schiller
 */
public class EclipseActionLogger implements IExecutionListener {

	public static Logger actionLog = Logger.getLogger("action");
	
	@Override
	public void notHandled(String commandId, NotHandledException exception) {
	}

	@Override
	public void postExecuteFailure(String commandId,
			ExecutionException exception) {
	}

	@Override
	public void postExecuteSuccess(String commandId, Object returnValue) {
	}

	@Override
	public void preExecute(String commandId, ExecutionEvent event) {
		actionLog.debug(commandId);
	}

}
