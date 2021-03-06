package edu.washington.cs.rtrefactor.preferences;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import edu.washington.cs.rtrefactor.Activator;
import edu.washington.cs.rtrefactor.detect.CheckStyleDetector;

/**
 * Preference Page for the CheckStyle code duplicate code detector
 * 
 * This class represents a preference page that
 * is contributed to the Preferences dialog. By 
 * subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows
 * us to create a page that is small and knows how to 
 * save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They
 * are stored in the preference store that belongs to
 * the main plug-in class. That way, preferences can
 * be accessed directly via the preference store.
 */

public class CheckStylePreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {

	
	public CheckStylePreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("CheckStyle Duplicate Code Detector Preferences");
	}
	
	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	public void createFieldEditors() {
	
		PreferenceUtil.createFieldEditor(getFieldEditorParent(), 
				new PreferenceUtil.FieldAdder() {
					@Override
					public void addField(FieldEditor editor) {
						CheckStylePreferencePage.this.addField(editor);
					}
				}, 
				CheckStyleDetector.PREFERENCES);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
	
}