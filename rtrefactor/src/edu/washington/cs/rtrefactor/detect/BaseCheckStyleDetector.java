package edu.washington.cs.rtrefactor.detect;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.IPreferenceStore;

import com.google.common.collect.BiMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.api.AbstractFileSetCheck;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.AuditListener;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;

import edu.washington.cs.rtrefactor.Activator;
import edu.washington.cs.rtrefactor.preferences.PreferenceUtil.Preference;

/**
 * Base class for CheckStyle detector plugins
 * @author Todd Schiller
 */
public abstract class BaseCheckStyleDetector<T extends AbstractFileSetCheck> implements IActiveDetector, IDetector{

	private final Checker checker;
	private final T check;
	
	private final List<AuditEvent> errs;
	
	/**
	 * 
	 * @param check the clone detector
	 * @param baseDir the base directory to run the checker from
	 */
	protected BaseCheckStyleDetector(T check, File baseDir){
		try{
			checker = new Checker();
		}catch(CheckstyleException ex){
			throw new RuntimeException("Error initializing CheckStyle", ex);
		}
		
		errs = Lists.newArrayList();
		
		this.check = check;
		
		checker.setBasedir(baseDir.getAbsolutePath());
		checker.addFileSetCheck(check);
		
		checker.addListener(new CloneAuditListener());
	}
	
	/**
	 * Get the check used by this detector
	 * @return the checked used by the detector
	 */
	protected T getCheck(){
		return check;
	}
	
	private class CloneAuditListener implements AuditListener{
		
		@Override
		public void addError(AuditEvent e) {
			errs.add(e);
		}

		@Override
		public void addException(AuditEvent e, Throwable t) {
		}

		@Override
		public void auditFinished(AuditEvent e) {
		}

		@Override
		public void auditStarted(AuditEvent e) {
		}

		@Override
		public void fileFinished(AuditEvent e) {
		}

		@Override
		public void fileStarted(AuditEvent e) {
		}
	}	
	
	/** 
	 * Set the preference set[{@code preference}] via reflection
	 * @param preference the preference name (case-sensitive)
	 * @param value value for the preference
	 */
	protected <Q> void setPreference(Preference<Q> preference){	
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		
		Class<?> clazz = getCheck().getClass();
		
		try {
			String name = "set" + preference.getName();
			Q val = preference.getDefault();
			
			if (val instanceof Integer){
				clazz.getMethod(name, int.class).invoke(getCheck(), store.getInt(preference.getKey()));
			}else if (val instanceof Boolean){
				clazz.getMethod(name, boolean.class).invoke(getCheck(), store.getBoolean(preference.getKey()));		
			}else if (val instanceof String){
				clazz.getMethod(name, String.class).invoke(getCheck(), store.getString(preference.getKey()));
			}else{
				throw new RuntimeException("Preference type " + val.getClass().getSimpleName() + " not supported");
			}
		} catch (Exception e) {
			throw new RuntimeException("Error setting preference " + preference, e);
		}
	}
	
	@Override
	/**
	 * {@inheritDoc}
	 */
	public Set<ClonePair> detect(Map<File, String> dirty, SourceRegion active) throws CoreException, IOException{	
		return new HashSet<ClonePair>(Sets.filter(detect(dirty), new DetectorUtil.ActiveRegion(active)));
	}
	
	/**
	 * If file path is relative, return absolute path from base directory; otherwise,
	 * return the file
	 * @param file the file
	 * @return the corresponding file object with an absolute path
	 */
	public File absolutePath(File file){
		if (file.isAbsolute()){
			return file;
		}else{
			return new File(checker.getBasedir(), file.getPath());
		}
	}
	
	/**
	 * Construct a clone pair from a detector audit event
	 * @param files map from result filenames to Eclipse resource filenames
	 * @param e the audit event
	 * @return the clone pair
	 */
	protected abstract ClonePair makeClonePair(BiMap<File,File> files, AuditEvent e);
	
	@Override
	/**
	 * {@inheritDoc}
	 */
	public Set<ClonePair> detect(Map<File, String> dirty) throws CoreException, IOException {
		DetectorUtil.detectLog.debug("Begin full clone detection with detector " + check.getClass().getName());
		
		// switch: view is result name -> Eclipse resource underlier
		BiMap<File,File> files = DetectorUtil.collect(dirty).inverse();
		
		DetectorUtil.detectLog.debug("Checking " + files.keySet().size() + " files");
		
		checker.process(Lists.newArrayList(files.keySet()));
		
		HashSet<ClonePair> result = new HashSet<ClonePair>();
		for (AuditEvent e : errs){
			result.add(makeClonePair(files, e));
		}
		
		// clear errors from the previous run
		errs.clear();
		
		for (File underlier : dirty.keySet()){
			File tmp = files.inverse().get(underlier);
			try{
				tmp.delete();
				DetectorUtil.detectLog.debug("Deleted temporary file " + tmp.getAbsolutePath());
			}catch(Exception ex){
				DetectorUtil.detectLog.debug("Errpr deleting temporary file " + tmp.getAbsolutePath(), ex);
			}
		}
		
		DetectorUtil.detectLog.debug("End full clone detection with detector " + check.getClass().getName());
		return result;
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	public void destroy() {
		checker.destroy();
	}
}
