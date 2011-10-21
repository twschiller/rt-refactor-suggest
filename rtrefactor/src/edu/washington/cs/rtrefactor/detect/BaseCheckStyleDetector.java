package edu.washington.cs.rtrefactor.detect;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.api.AbstractFileSetCheck;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.AuditListener;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;

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
	
	@Override
	/**
	 * {@inheritDoc}
	 */
	public Set<ClonePair> detect(Map<File, String> dirty, SourceRegion active) throws CoreException{	
		return new HashSet<ClonePair>(Sets.filter(detect(dirty), new DetectorUtil.ActiveRegion(active)));
	}
	
	/**
	 * Construct a clone pair from a detector audit event
	 * @param e the audit event
	 * @return the clone pair
	 */
	protected abstract ClonePair makeClonePair(AuditEvent e);
	
	@Override
	/**
	 * {@inheritDoc}
	 */
	public Set<ClonePair> detect(Map<File, String> dirty) throws CoreException {
		checker.process(DetectorUtil.collect());
		checker.destroy();
		
		HashSet<ClonePair> result = new HashSet<ClonePair>();
		for (AuditEvent e : errs){
			result.add(makeClonePair(e));
		}
		return result;
	}	
	
}
