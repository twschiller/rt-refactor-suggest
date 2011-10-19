package edu.washington.cs.rtrefactor.actions;

import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eposoft.jccd.data.SimilarityGroupManager;
import org.eposoft.jccd.data.SimilarityPair;

import com.google.common.collect.Lists;

import edu.washington.cs.rtrefactor.detect.CloneDetector;

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
		
		SimilarityGroupManager m = null;
		
		try {
			m = CloneDetector.detect(EnumSet.allOf(CloneDetector.JccdOptions.class));
		} catch (CoreException e) {
			e.printStackTrace();
			MessageDialog.openError(
				window.getShell(),
				"Real-time Refactoring Suggestions",
				"Core error running clone detection");
			return;
		} catch (Exception e){
			e.printStackTrace();
			MessageDialog.openError(
				window.getShell(),
				"Real-time Refactoring Suggestions",
				"Error running clone detection");
			return;
		}
		
		//Example code for sorting Similarity pairs by quality score
		List<SimilarityPair> xxx = Lists.newArrayList(m.getPairs());
		Collections.sort(xxx, new Comparator<SimilarityPair>(){
			@Override
			public int compare(SimilarityPair o1, SimilarityPair o2) {
				return Double.compare(CloneDetector.qualityScore(o1), CloneDetector.qualityScore(o2));
			}
		});
		
		for (SimilarityPair p : xxx){
			try{
				String t1 = CloneDetector.getSource(p.getFirstNode());
				String t2 = CloneDetector.getSource(p.getSecondNode());
				System.out.println("=================================");
				System.out.println("=================================");
				
				System.out.println(CloneDetector.getFile(p.getFirstNode()).getPath() + " ---------------");
				System.out.println(t1);
				
				System.out.println(CloneDetector.getFile(p.getSecondNode()).getPath() + " ---------------");
				System.out.println(t2);
			}catch(Exception e){
				e.printStackTrace();
			}
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