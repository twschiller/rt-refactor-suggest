package edu.washington.cs.rtrefactor.scorer;

import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.ui.IMarkerResolution;

import com.google.common.base.Predicate;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;

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
	
	// ADAPTIVE SCORING SYSTEM
	// 
	// The THRESHOLD stays constant, the scores for a particular
	// action are adjusted according to the following formula:
	//
	// Y = (X_{clone} * B_{action} + C_{action}) * (1-D)^I_{clone} * (1-M)^N_{clone}
	//
	// , where   X_{clone} is the action score (e.g., the similarity)
	//           B_{action} is a an action-specific scaling factor (preference)
	//           C_{action} is fixed constant for the action (normative information)
	//           D is the DEVELOPMENT_DECAY
	//           I_{clone} is the number of times one or more development actions occurred between clone views
	//		     M is the MAINTENANCE_DECAY
	//           N_{clone} is the number of times the clone has been viewed
	//			 

	private double calc(double base, int action, int cloneNumber){
		return (base * B_ACTION[JUMP] + C_ACTION[JUMP]) * decayAdjustment(cloneNumber);
	}
	
	private static int JUMP = 0;
	private static int PASTE = 1;
	private static int INSERT = 2;
	private static int EXTRACT = 2;
	
	private double C_ACTION[] = new double[] { 50., 20., 90., 85. };
	private double B_ACTION[] = new double[] { 1.0, 1.0, 1.0, 1.0 };
	
	private static final int THRESHOLD = 40;
	
	// DECAY SCENARIOS
	// 
	// (1) User views QF, doesn't select a fix, modifies source code a
	//     single time
	//
	//	   recordQuickFixActivation
	//	   calculateResolutions(clone)
	//
	// (2) User views QF, doesn't select a fix, modifies the source code
	//     multiple times
	//
	//     recordQuickFixActivation
	//     calculateResolutions(clone)+
	//
	// (3) User views QF, doesn't select a fix, views other QFs (no development)
	//
	//     recordQuickFixActivation
	//     recordQuickFixActivation
	//
	// 1 & 2 should have the same effect on the clone score
	
	/**
	 * Decay incurred when user views quick fixes multiple times
	 */
	private static double MAINTENANCE_DECAY = 0.05;
	
	/**
	 * Decay incurred when user views quick fixes multiple times, but
	 * performed development between views
	 */
	private static double DEVELOPMENT_DECAY = 0.2;

	/**
	 * Multiset tracking the number of times a clone has been viewed
	 */
	private Multiset<Integer> activationCnt = HashMultiset.create();
	
	/**
	 * Set of clones that have been viewed since the last development action
	 */
	private Set<Integer> since = Sets.newHashSet();
	
	/**
	 * tracks number of times a clone pair has been viewed and then development was performed
	 */
	private Multiset<Integer> developmentDecayCnt = HashMultiset.create();
	
	/**
	 * The singleton instance
	 */
	private static Scorer instance = null;
	
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
		// Scale the action preference B to reward actions with lower scores more; don't
		// penalize the other actions
		//
		// B_{action} = B_{action} * (1 + (100 - SCORE) / THRESHOLD)
		
		if (select instanceof CloneFix){
			scoreLog.debug(select.getClass().getName());
			
			CloneFix f = (CloneFix) select;
			
			Integer fix = null;
			if (select instanceof JumpToFix){
				fix = JUMP;
			}else if (select instanceof CopyPasteFix){
				fix = PASTE;
			}else if (select instanceof ExtractMethodFix){
				fix = EXTRACT;
			}else if (select instanceof InsertCallFix){
				fix = INSERT;
			}
			
			int relevance = f.getRelevance();
			
			int max = Integer.MIN_VALUE;
			for (IMarkerResolution x : fixes){
				max = Math.max(max, ((CloneFix) x).getRelevance());
			}
			
			double old = B_ACTION[fix];
			B_ACTION[fix] = B_ACTION[fix] * (1 + (100. - relevance) / THRESHOLD);
			
			scoreLog.debug("Fix " + f.getClass().getCanonicalName() + " selected with score " + relevance + 
					" (max score: " + max + ")");
			
			scoreLog.debug("Adjusted preference " + B_ACTION[fix] + " (old: " + old + ")");
			
		}
	}
	
	/**
	 * Record that the activation of the quick fix window
	 * @param fixes the fixes presented to the user
	 */
	public void recordQuickFixActivation(IMarkerResolution[] fixes){
		Set<Integer> cs = Sets.newHashSet();
		for (IMarkerResolution fix : fixes){
			cs.add(((CloneFix) fix).getCloneNumber());
		}
		
		scoreLog.debug("QuickFix activated with " + fixes.length + " fixes.");
		
		for (Integer c : cs){
			activationCnt.add(c);
			since.add(c);
			scoreLog.debug("Clone #" + c + " has been activated " + activationCnt.count(c) + " time(s)");
		}
	}
	
	/**
	 * Generate the marker resolutions for the given code clone <code>pair</code>.
	 * @param pair the code clone pair
	 * @see {@link Scorer#calculateResolutions(ClonePairData, CloneFixer)}
	 * @return the marker resolutions
	 */
	public List<CloneFix> calculateResolutions(ClonePairData pair) {
		onDevelopment();
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
				if (fix.getRelevance() < THRESHOLD){
					scoreLog.debug("Filtered fix " + fix.getClass().getName() + " (score: " + fix.getRelevance() + ") with threshold " + THRESHOLD);
					return false;
				}else{
					return true;
				}
			}
		}));
	}
	
	private List<CloneFix> generateJumpToCloneFixes(ClonePairData pair, CloneFixer parent){
		// TODO if the user is editing existing code (instead of writing new code), increase the score for "jump to clone"?
		double score = calc(pair.getSimilarity(), JUMP, pair.getCloneNumber());
		return Lists.<CloneFix>newArrayList(new JumpToFix(pair, truncate(score), parent));
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
		double coverage = Math.min(pair.getOtherRegion().getLength() / (double) methodSource.length(), 1.);
		
		double base = coverage * pair.getSimilarity() * Math.pow(0.95, m.getNumberOfParameters());
		double score = calc(base, INSERT, pair.getCloneNumber());
		
		return Lists.<CloneFix>newArrayList(new InsertCallFix(pair, truncate(score), parent));
	}
	
	private List<CloneFix> generateExtractMethodFixes(ClonePairData pair, CloneFixer parent){
		double score = calc(pair.getSimilarity(), EXTRACT, pair.getCloneNumber());
		return Lists.<CloneFix>newArrayList(new ExtractMethodFix(pair, truncate(score), parent));
	}
	
	private List<CloneFix> generatePasteCloneFixes(ClonePairData pair, CloneFixer parent){
		double score = calc(pair.getSimilarity(), PASTE, pair.getCloneNumber());
		return Lists.<CloneFix>newArrayList(new CopyPasteFix(pair, truncate(score), parent));
	}
	
	/**
	 * Force the relevance score to be an integer between 10 and 100
	 * @param x the score
	 * @return an integer score between 10 and 100
	 */
	private int truncate(double x){
		return Math.max(10, Math.min((int) x, 100));
	}

	/**
	 * Calculate the decay adjustment for <code>cloneNumber</code>
	 * @param cloneNumber the clone
	 * @return the multiplicative decay adjustment
	 */
	private double decayAdjustment(int cloneNumber){
		double ignoreDecay = activationCnt.contains(cloneNumber) ? Math.pow((1 - MAINTENANCE_DECAY), activationCnt.count(cloneNumber)) : 1.0;
		double developmentDecay = developmentDecayCnt.contains(cloneNumber) ? Math.pow((1 - DEVELOPMENT_DECAY), developmentDecayCnt.count(cloneNumber)) : 1.0;
		return ignoreDecay * developmentDecay;
	}
	
	/**
	 * Action to perform when development has occurred in the editor
	 */
	private void onDevelopment(){
		if (!since.isEmpty()){
			scoreLog.debug("Development decay for clones " + since.toString());
		}
	
		developmentDecayCnt.addAll(since);
		since.clear();
	}
	
}
