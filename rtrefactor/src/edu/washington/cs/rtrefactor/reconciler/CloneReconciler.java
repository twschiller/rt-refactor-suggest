package edu.washington.cs.rtrefactor.reconciler;

import org.apache.log4j.Logger;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.MonoReconciler;

import edu.washington.cs.rtrefactor.Activator;
import edu.washington.cs.rtrefactor.preferences.PreferenceConstants;

/* This class is needed to check incremental preferences
 * before we reconcile.
 */
public class CloneReconciler extends MonoReconciler{

	public static Logger reconcilerLog = Logger.getLogger("reconciler");
	
	/*The latest value of the incremental preference*/
	Boolean fIncrementalPreference = null; 

	public CloneReconciler(IReconcilingStrategy strategy) {
		super(strategy, true);
		checkIncrementalPreference();
	}

	/*Overrides in order to check preferences*/ 
	public void process(DirtyRegion dirtyRegion)
	{
		/*If they changed the preference, the dirtyRegion isn't valid
		 * Unfortunately, there's no way to get the dirty region at this point
		 * if incremental was turned on, so there will be a single reconcile
		 * delay before the change takes effect.
		 * */
		if(checkIncrementalPreference())
			super.process(null);
		else
			super.process(dirtyRegion);
	}

	/* Update setting if the incremental preference has changed
	 * @return true if preference changed
	 */		
	protected boolean checkIncrementalPreference() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		Boolean incremental = store.getBoolean(PreferenceConstants.P_INCREMENT);
		if(incremental != fIncrementalPreference)
		{
			reconcilerLog.info("Changing incremental to " + incremental);
			fIncrementalPreference = incremental;
			setIsIncrementalReconciler(fIncrementalPreference);
			return true;
		}
		return false;
	}

}
