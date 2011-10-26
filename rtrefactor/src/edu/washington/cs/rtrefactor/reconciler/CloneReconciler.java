package edu.washington.cs.rtrefactor.reconciler;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.MonoReconciler;

import edu.washington.cs.rtrefactor.Activator;
import edu.washington.cs.rtrefactor.preferences.PreferenceConstants;

public class CloneReconciler extends MonoReconciler{

	Boolean fIncrementalPreference = null; 

	public CloneReconciler(IReconcilingStrategy strategy) {
		super(strategy, true);
		checkIncrementalPreference();
	}

	public void process(DirtyRegion dirtyRegion)
	{
		if(checkIncrementalPreference())
			super.process(null);
		else
			super.process(dirtyRegion);
	}

	/*
	 * returns whether preference changed
	 */		
	protected boolean checkIncrementalPreference() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		Boolean incremental = store.getBoolean(PreferenceConstants.P_INCREMENT);
		if(incremental != fIncrementalPreference)
		{
			System.out.println("changing incremental to " + incremental);
			fIncrementalPreference = incremental;
			setIsIncrementalReconciler(fIncrementalPreference);
			return true;
		}
		return false;
	}

}
