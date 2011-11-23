package edu.washington.cs.rtrefactor.actions;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import edu.washington.cs.rtrefactor.Activator;
import edu.washington.cs.rtrefactor.preferences.PreferenceConstants;

/**
 * Action to switch the clone detection mode (development v. maintenance).
 * @author Todd Schiller
 */
public class SwitchDetectionMode implements IWorkbenchWindowActionDelegate {
	
	private ImageDescriptor developmentIcon;
	private ImageDescriptor maintenanceIcon;
	
	/**
	 * The constructor.
	 */
	public SwitchDetectionMode() {
	}

	/**
	 * The action has been activated. The argument of the
	 * method represents the 'real' action sitting
	 * in the workbench UI.
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	public void run(IAction action) {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setValue(PreferenceConstants.P_INCREMENT, action.isChecked());
		setTooltip(action);
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();	
		action.setChecked(store.getBoolean(PreferenceConstants.P_INCREMENT));
		setTooltip(action);
	}
	
	private void setTooltip(IAction action){
		if (action.isChecked()){
			action.setToolTipText("Development Mode (click to switch to Maintenance Mode)");
			action.setImageDescriptor(developmentIcon);
		}else{
			action.setToolTipText("Maintenance Mode (click to switch to Development Mode)");
			action.setImageDescriptor(maintenanceIcon);
		}
	}

	@Override
	public void dispose() {
	}

	@Override
	public void init(IWorkbenchWindow window) {
		URL d = null;
		URL m = null;
		try {
			d = new URL(Activator.getDefault().getDescriptor().getInstallURL(), "icons/keyboard-add-icon.png");
			m = new URL(Activator.getDefault().getDescriptor().getInstallURL(), "icons/keyboard-magnify-icon.png");
		} catch (MalformedURLException e) {
			return;
		}
		developmentIcon = ImageDescriptor.createFromURL(d);
		maintenanceIcon = ImageDescriptor.createFromURL(m);
	}
}