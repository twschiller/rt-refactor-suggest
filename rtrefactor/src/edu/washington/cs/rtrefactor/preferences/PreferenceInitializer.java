package edu.washington.cs.rtrefactor.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import edu.washington.cs.rtrefactor.Activator;
import edu.washington.cs.rtrefactor.detect.CheckStyleDetector;
import edu.washington.cs.rtrefactor.detect.JccdDetector;
import edu.washington.cs.rtrefactor.detect.SimianDetector;
import edu.washington.cs.rtrefactor.preferences.PreferenceUtil.Preference;


/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(PreferenceConstants.P_CHOICE, "Simian");
	
		initializeDefaultPreferences(store, JccdDetector.PREFERENCES);
		initializeDefaultPreferences(store, CheckStyleDetector.PREFERENCES);
		initializeDefaultPreferences(store, SimianDetector.PREFERENCES);
	}
	
	private void initializeDefaultPreferences(IPreferenceStore store, Preference<?> preferences[]){
		for (Preference<?> x : preferences){
			Object v = x.getDefault();
			
			if (v instanceof Boolean){
				store.setDefault(x.getKey(), (Boolean) v);
			}else if (v instanceof Integer){
				store.setDefault(x.getKey(), (Integer) v);
			}else if (v instanceof String){
				store.setDefault(x.getKey(), (String) v);
			}else{
				throw new RuntimeException("Preference type " + v.getClass().getSimpleName() + " not supported");
			}
		}	
	}

}
