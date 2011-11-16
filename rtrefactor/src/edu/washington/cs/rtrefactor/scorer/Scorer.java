package edu.washington.cs.rtrefactor.scorer;

import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.ui.IMarkerResolution;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.washington.cs.rtrefactor.quickfix.CloneFix;
import edu.washington.cs.rtrefactor.quickfix.CloneFixer;
import edu.washington.cs.rtrefactor.quickfix.CopyPasteFix;
import edu.washington.cs.rtrefactor.quickfix.ExtractMethodFix;
import edu.washington.cs.rtrefactor.quickfix.FindMethod;
import edu.washington.cs.rtrefactor.quickfix.InsertCallFix;
import edu.washington.cs.rtrefactor.quickfix.JumpToFix;
import edu.washington.cs.rtrefactor.reconciler.ClonePairData;

/**
 * Methods for creating and scoring code clone resolutions
 * @author Todd Schiller
 */
public class Scorer {

	protected static Logger scoreLog = Logger.getLogger("scorer");
	
	private static final int BASE_JUMPTOCLONE_SCORE = 50;
	private static final int BASE_PASTECLONE_SCORE = 20;
	private static final int BASE_INSERTMETHODCALL_SCORE = 90;
	private static final int BASE_EXTRACTMETHODCALL_SCORE = 85;
	
	private static final int DEFAULT_THRESHOLD = 20;
	
	/**
	 * The singleton instance
	 */
	private static Scorer instance = null;
	
	/**
	 * Clone Number -> Display Threshold
	 */
	private final HashMap<Integer, Integer> thresholds = Maps.newHashMap();
	
	/**
	 * Clone Number -> Clone Fix
	 */
	private final HashMap<Integer, CloneFix> cache = Maps.newHashMap();
	
	/**
	 * Private constructor.
	 */
	private Scorer(){
	}
	
	/**
	 * Access the singleton scorer instance
	 * @return the singleton scorer instance
	 */
	public static Scorer getInstance(){
		if (instance == null){
			instance = new Scorer();
		}
		return instance;
	}
	
	/**
	 * Record a quick fix selection made by the user
	 * @param fixes the fixes presented to the user
	 * @param select the resolution selected by the user
	 */
	public void recordQuickFixSelection(IMarkerResolution[] fixes, IMarkerResolution select){
		// TODO add implementation
		scoreLog.debug("Fix of type " +((CloneFix)select).getClass().getCanonicalName() +
				" on clone " + ((CloneFix)select).getCloneNumber() + " was activated!");
	}
	
	/**
	 * Record that the activation of the quick fix window
	 * @param fixes the fixes presented to the user
	 */
	public void recordQuickFixActivation(IMarkerResolution[] fixes){
		// TODO add implementation
		scoreLog.debug("Notified of " + fixes.length + " fixes " + fixes);
	}
	
	/**
	 * @see calculateResolutions(ClonePairData pair, CloneFixer parent)
	 * 
	 * Generate fixes with no parent.
	 * 
	 * @param pair the code clone pair
	 * @return
	 */
	public List<CloneFix> calculateResolutions(ClonePairData pair) {
		return calculateResolutions(pair, null);
	}
	
	/**
	 * Generate the marker resolutions for the given code clone <code>pair</code>. Resolutions
	 * are given scores based on the estimated difficulty / usefulness of performing the action.
	 * 
	 * Multiple resolutions of the same type may be produced if the clone overlaps multiple methods.
	 * 
	 * @param pair the code clone pair
	 * @param parent the parent Clonefixer (can be null)
	 * @return the marker resolutions
	 */
	public List<CloneFix> calculateResolutions(ClonePairData pair, CloneFixer parent){
		List<CloneFix> rs = Lists.newArrayList();
		
		rs.addAll(generateJumpToCloneFixes(pair, parent));
		rs.addAll(generateInsertMethodCallFixes(pair, parent));
		rs.addAll(generateExtractMethodFixes(pair, parent));
		rs.addAll(generatePasteCloneFixes(pair, parent));
		
		return Lists.newArrayList(Iterables.filter(rs, new Predicate<CloneFix>(){
			@Override
			public boolean apply(CloneFix fix) {
				return fix.getRelevance() >= getThreshold(fix.getCloneNumber());
			}
		}));
	}
	
	private List<CloneFix> generateJumpToCloneFixes(ClonePairData pair, CloneFixer parent){
		// TODO if the user is editing existing code (instead of writing new code), increase the score for "jump to clone"?
		if(parent == null) {
			return Lists.<CloneFix>newArrayList(new JumpToFix(pair, BASE_JUMPTOCLONE_SCORE));
		}else  {
			return Lists.<CloneFix>newArrayList(new JumpToFix(pair, BASE_JUMPTOCLONE_SCORE, parent));
			
		}	
	}
	/**
	 * Generate Insert Method Call fixes, score according to the following criteria:
	 * (1) the base score (2) the number of arguments, and (3) the percent of the method covered by the clone.
	 * @param pair the clone pair
	 * @return scored insert method call fixes
	 */
	private List<CloneFix> generateInsertMethodCallFixes(ClonePairData pair, CloneFixer parent){
		IMethod m;
		try {
			m = FindMethod.findMethod(pair.getOtherRegion());
		} catch (CoreException e) {
			scoreLog.error("Error accessing method for clone pair", e);
			return Lists.newArrayList();
		}
		
		if (m == null){
			return Lists.newArrayList();
		}
		
		String methodSource;
		try {
			methodSource = m.getSource();
		} catch (JavaModelException e) {
			scoreLog.error("Error getting method source ", e);
			return Lists.newArrayList();
		}
		
		// TODO this is actually wrong. need to inspect each character in the other region to see if it is part of the method
		double coverage = Math.min(pair.getOtherRegion().getLength() / (double) methodSource.length(), 100.);
		
		double score = (coverage * BASE_INSERTMETHODCALL_SCORE) - (10. * m.getNumberOfParameters());
		
		if(parent == null) {
			return Lists.<CloneFix>newArrayList(new InsertCallFix(pair, (int) score));
		} else {
			return Lists.<CloneFix>newArrayList(new InsertCallFix(pair, (int) score, parent));
		}
	}
	
	private List<CloneFix> generateExtractMethodFixes(ClonePairData pair, CloneFixer parent){
		if(parent == null) {
			return Lists.<CloneFix>newArrayList(new ExtractMethodFix(pair, BASE_EXTRACTMETHODCALL_SCORE));
		} else {
			return Lists.<CloneFix>newArrayList(new ExtractMethodFix(pair, BASE_EXTRACTMETHODCALL_SCORE, parent));

		}
	}
	
	private List<CloneFix> generatePasteCloneFixes(ClonePairData pair, CloneFixer parent){
		if(parent == null) {
			return Lists.<CloneFix>newArrayList(new CopyPasteFix(pair, BASE_PASTECLONE_SCORE));
		} else {
			return Lists.<CloneFix>newArrayList(new CopyPasteFix(pair, BASE_PASTECLONE_SCORE, parent));
			
		}
	}
	
	/**
	 * Get the modified threshold for the given clone, or <code>DEFAULT_THRESHOLD</code> iff
	 * no modified threshold exists
	 * @param cloneNumber the unique identifier for the clone
	 * @return the threshold for the clone
	 */
	private int getThreshold(int cloneNumber){
		return thresholds.containsKey(cloneNumber) ? thresholds.get(cloneNumber) : DEFAULT_THRESHOLD;
	}
}
