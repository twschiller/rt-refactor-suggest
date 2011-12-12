package edu.washington.cs.rtrefactor.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.swt.widgets.Composite;

/**
 * Utilities for making preference pages for simple values
 * @author Todd Schiller
 */
public abstract class PreferenceUtil {

	/**
	 * Preference descriptor
	 * @author Todd Schiller
	 * @param <T> the preference value type
	 */
	public static class Preference<T>{
		private final String key;
		private final String name;
		private final String description;
		private final T def;
		
		public Preference(String prefix, String name, String description, T def) {
			super();
			this.key = prefix + name;
			this.name = name;
			this.description = description;
			this.def = def;
		}

		public String getKey() {
			return key;
		}
		
		public String getName(){
			return name;
		}

		public String getDescription() {
			return description;
		}

		public T getDefault() {
			return def;
		}
	}
	
	public interface FieldAdder{
		void addField(FieldEditor editor);
	}
	
	/**
	 * Add a field editor via the {@link FieldAdder#addField(FieldEditor)} method of {@code page}.
	 * @param parent the parent UI component
	 * @param adder callback to add the field editor
	 * @param prefix prefix for the preference id used to avoid collisions
	 * @param preferences array of preferences to add
	 */
	public static void createFieldEditor(Composite parent, FieldAdder adder, Preference<?> preferences[]){
		for (Preference<?> x : preferences){
			if (x.getDefault() instanceof Integer){
				adder.addField(new IntegerFieldEditor(
						x.getKey(),
						x.getDescription(),
						parent));
			}else if (x.getDefault() instanceof Boolean){
				adder.addField(new BooleanFieldEditor(
						x.getKey(),
						x.getDescription(),
						parent));
			}else{
				throw new RuntimeException("Preference type " + x.getDefault().getClass().getSimpleName() + " not supported");
			}
		}
	}
	
}
