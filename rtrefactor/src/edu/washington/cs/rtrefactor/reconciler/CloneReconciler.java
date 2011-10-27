package edu.washington.cs.rtrefactor.reconciler;

import org.apache.log4j.Logger;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

import edu.washington.cs.rtrefactor.Activator;
import edu.washington.cs.rtrefactor.preferences.PreferenceConstants;

//TODO: Move this functionality elsewhere?
/** This class is overidden to check incremental preferences.
 * 
 * @author Travis Mandel
 */
public class CloneReconciler extends MonoReconciler{

	public static Logger reconcilerLog = Logger.getLogger("reconciler");
	
	public CloneReconciler(IReconcilingStrategy strategy) {
		super(strategy, true);
		
		updateIncrementalPreference();
		
		//Add a listener to update the incremental setting when it is changed
		Activator.getDefault().getPreferenceStore().addPropertyChangeListener(
				new IPropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty() == PreferenceConstants.P_INCREMENT) {
					updateIncrementalPreference();
				}
			}
		});
	}


	/** Update the incremental setting based on the current preference
	 */		
	protected void updateIncrementalPreference() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		Boolean incremental = store.getBoolean(PreferenceConstants.P_INCREMENT);
		reconcilerLog.info("Changing incremental to " + incremental);
		setIsIncrementalReconciler(incremental);
	}

}
