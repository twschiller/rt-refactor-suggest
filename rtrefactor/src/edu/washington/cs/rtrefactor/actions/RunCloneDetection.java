package edu.washington.cs.rtrefactor.actions;

import java.io.File;
import java.util.HashMap;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import edu.washington.cs.rtrefactor.Activator;
import edu.washington.cs.rtrefactor.detect.CheckStyleDetector;
import edu.washington.cs.rtrefactor.detect.IDetector;
import edu.washington.cs.rtrefactor.detect.JccdDetector;
import edu.washington.cs.rtrefactor.detect.SimianDetector;
import edu.washington.cs.rtrefactor.preferences.PreferenceConstants;

/**
 * Our sample action implements workbench action delegate.
 * The action proxy will be created by the workbench and
 * shown in the UI. When the user tries to use the action,
 * this delegate will be created and execution will be 
 * delegated to it.
 * @see IWorkbenchWindowActionDelegate
 */
public class RunCloneDetection implements IWorkbenchWindowActionDelegate {
	private IWorkbenchWindow window;
	/**
	 * The constructor.
	 */
	public RunCloneDetection() {
	}

	/**
	 * The action has been activated. The argument of the
	 * method represents the 'real' action sitting
	 * in the workbench UI.
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	public void run(IAction action) {
		MessageDialog.openInformation(
			window.getShell(),
			"Real-time Refactoring Suggestions",
			"Starting clone detection");
		
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		String name = store.getString(PreferenceConstants.P_CHOICE);
		
		IDetector detector = null;
		
		if (name.equalsIgnoreCase(JccdDetector.NAME)){
			detector = new JccdDetector();
		}else if (name.equalsIgnoreCase(CheckStyleDetector.NAME)){
			detector = new CheckStyleDetector();
		}else if (name.equalsIgnoreCase(SimianDetector.NAME)){
			detector = new SimianDetector();
		}
		
		try {
			detector.detect(new HashMap<File, String>());
		} catch (Exception ex) {
			ex.printStackTrace();
			
			MessageDialog.openError(
					window.getShell(),
					"Real-time Refactoring Suggestions",
					"Error running clone detection: " + ex.getMessage());
			return;
		}
		
		MessageDialog.openInformation(
				window.getShell(),
				"Real-time Refactoring Suggestions",
				"Finished running clone detection");
	}

	/**
	 * Selection in the workbench has been changed. We 
	 * can change the state of the 'real' action here
	 * if we want, but this can only happen after 
	 * the delegate has been created.
	 * @see IWorkbenchWindowActionDelegate#selectionChanged
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

	/**
	 * We can use this method to dispose of any system
	 * resources we previously allocated.
	 * @see IWorkbenchWindowActionDelegate#dispose
	 */
	public void dispose() {
	}

	/**
	 * We will cache window object in order to
	 * be able to provide parent shell for the message dialog.
	 * @see IWorkbenchWindowActionDelegate#init
	 */
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}
}