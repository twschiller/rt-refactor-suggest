package edu.washington.cs.rtrefactor.scorer;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.ui.IMarkerResolution;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;

import edu.washington.cs.rtrefactor.Activator;
import edu.washington.cs.rtrefactor.detect.SourceRegion;
import edu.washington.cs.rtrefactor.preferences.PreferenceConstants;
import edu.washington.cs.rtrefactor.quickfix.CloneFix;
import edu.washington.cs.rtrefactor.quickfix.CloneResolutionGenerator;
import edu.washington.cs.rtrefactor.quickfix.CopyPasteFix;
import edu.washington.cs.rtrefactor.quickfix.ExtractMethodFix;
import edu.washington.cs.rtrefactor.quickfix.FindBlock;
import edu.washington.cs.rtrefactor.quickfix.FindBlock.Mode;
import edu.washington.cs.rtrefactor.quickfix.FindBlock.StatementGroup;
import edu.washington.cs.rtrefactor.quickfix.FindMethod;
import edu.washington.cs.rtrefactor.quickfix.InsertCallFix;
import edu.washington.cs.rtrefactor.quickfix.JumpToFix;
import edu.washington.cs.rtrefactor.reconciler.ClonePairData;

/**
 * Methods for creating and scoring code clone resolutions
 * @author Todd Schiller
 */
strictfp public class Scorer {

	protected static final Logger scoreLog = Logger.getLogger("scorer");
	
	public static final int MAX_SCORE = 100;
	public static final int MIN_SCORE = 10;
	public static final int THRESHOLD = 0;
	
	// ADAPTIVE SCORING SYSTEM
	// 
	// The THRESHOLD stays constant, the scores for a particular
	// action are adjusted according to the following formula:
	//
	// ADJ = (RAW_{clone,action} * PREF_{action}) * (1-DevDecay)^#DevClone_{clone} * (1-MainDecay)^#DisplayClone_{clone}
	//
	// , where   RAW_{clone,action} is the action score 
	//           PREF_{action} is a an action-specific scaling factor (preference)
	//           DevDecay is the DEVELOPMENT_DECAY
	//           #DevClone_{clone} is the number of times one or more development actions occurred between clone views
	//		     MainDecay is the MAINTENANCE_DECAY
	//           #DisplayClone_{clone} is the number of times the clone has been viewed
	//			 

	private int calc(double raw, int action, int cloneNumber){
		double maintenanceDecay = activationCnt.contains(cloneNumber) ? Math.pow((1 - MAINTENANCE_DECAY), activationCnt.count(cloneNumber)) : 1.0;
		double developmentDecay = developmentDecayCnt.contains(cloneNumber) ? Math.pow((1 - DEVELOPMENT_DECAY), developmentDecayCnt.count(cloneNumber)) : 1.0;
		
		double sumPref = 0;
		for (int i = 0; i < preferences.length; i++){
			sumPref += preferences[i];
		}
		
		return truncate(truncate(raw) * (1. + preferences[action] / sumPref) * maintenanceDecay * developmentDecay);
	}
	
	private static final int JUMP = 0;
	private static final int PASTE = 1;
	private static final int INSERT = 2;
	private static final int EXTRACT = 3;

	private double preferences[] = new double[] { 50., 20., 90., 85. };
		
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
	 * Penalty incurred for the number of arguments in the method when
	 * scoring an InsertCall action
	 */
	private static double ARGS_PENALTY = 0.05;
	
	/**
	 * Penalty incurred for the number of non-local when
	 * scoring an Extract Method action
	 */
	private static double NONLOCAL_PENALTY = 0.05;
	
	/**
	 * Penalty incurred for identifier mismatches when scoring
	 * a Paste action
	 */
	private static double MISMATCH_PENALTY = 0.05;
	
	/**
	 * Penalty incurred when scoring a Jump To action in Development mode
	 */
	private static double DEV_PENALTY = 0.35;
	
	/**
	 * Multiset tracking the number of times a clone has been viewed
	 */
	private final Multiset<Integer> activationCnt = HashMultiset.create();
	
	/**
	 * Set of clones that have been viewed since the last development action
	 */
	private final Set<Integer> since = Sets.newHashSet();
	
	/**
	 * tracks number of times a clone pair has been viewed and then development was performed
	 */
	private final Multiset<Integer> developmentDecayCnt = HashMultiset.create();
	
	/**
	 * the last marker resolutions displayed in a QuickFix window
	 */
	private IMarkerResolution[] lastSeen = null;
	
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
		// preferences_{action} = preferences_{action} * (1 + (100 - SCORE) / THRESHOLD)
		
		if (select instanceof CloneFix){
			scoreLog.debug(select.getClass().getName());
			
			CloneFix f = (CloneFix) select;
			
			int fix = actionIndex(f);
			
			int relevance = f.getRelevance();
			
			int max = Integer.MIN_VALUE;
			for (IMarkerResolution x : fixes){
				max = Math.max(max, ((CloneFix) x).getRelevance());
			}
			
			double old = preferences[fix];
			preferences[fix] = preferences[fix] * (1. + (MAX_SCORE - relevance) / (double) THRESHOLD);
			
			
			Activator.evaluationLog.info("Fix " + f.getClass().getSimpleName() +  "(#" + f.getCloneNumber() + ") selected with score " + relevance + ". "
					+ "Others: " + fixList(fixes));
			
			scoreLog.debug("Fix " + f.getClass().getCanonicalName() + " selected with score " + relevance + 
					" (max score: " + max + ")");
			
			scoreLog.debug("Adjusted preference " + preferences[fix] + " (old: " + old + ")");
			
		}
	}
	
	/**
	 * Comma-separated list of clone fixes and relevances
	 * @param fixes the marker fixes
	 * @return comma-separated list of clone fixes and relevances
	 */
	private static String fixList(IMarkerResolution[] fixes){
		return "[" + Joiner.on(", ").join(
				Iterables.transform(Iterables.filter(Lists.newArrayList(fixes), CloneFix.class),
						new Function<CloneFix, String>(){
					@Override
					public String apply(CloneFix fix) {
						return fix.getClass().getSimpleName() + "(#" + fix.getCloneNumber() + ") : " + + fix.getRelevance();
					}
				})) + "]";
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
		
		Activator.evaluationLog.info("QuickFix activated with " + fixes.length + " fixes: " + fixList(fixes));
		
		for (Integer c : cs){
			activationCnt.add(c);
			since.add(c);
			scoreLog.debug("Clone #" + c + " has been activated " + activationCnt.count(c) + " time(s)");
		}
		
		lastSeen = fixes;
	}
	
	/**
	 * Generate the marker resolutions for the given code clone <code>pair</code>.
	 * @param pair the code clone pair
	 * @see {@link Scorer#calculateResolutions(ClonePairData, CloneResolutionGenerator)}
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
	public List<CloneFix> calculateResolutions(ClonePairData pair, CloneResolutionGenerator parent){
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
	
	private List<CloneFix> generateJumpToCloneFixes(ClonePairData pair, CloneResolutionGenerator parent){
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();	
	
		double base = truncate(pair.getSimilarity() * 
				(store.getBoolean(PreferenceConstants.P_INCREMENT) ? (1. - DEV_PENALTY) : 1.0));
				
		int score = calc(base, JUMP, pair.getCloneNumber());
		return Lists.<CloneFix>newArrayList(new JumpToFix(pair, score, parent));
	}
	
	/**
	 * Generate Insert Method Call fixes, score according to the following criteria:
	 * (1) the base score (2) the number of arguments, and (3) the percent of the method covered by the clone.
	 * @param pair the clone pair
	 * @return scored insert method call fixes
	 */
	private List<CloneFix> generateInsertMethodCallFixes(ClonePairData pair, CloneResolutionGenerator parent){
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
			
		double coverage = FindMethod.methodCoverage(m, pair.getOtherRegion());

		double base = truncate(coverage * pair.getSimilarity() * Math.pow(ARGS_PENALTY, m.getNumberOfParameters()));
		int score = calc(base, INSERT, pair.getCloneNumber());
		
		return Lists.<CloneFix>newArrayList(new InsertCallFix(pair, score, parent));
	}
	
	private List<CloneFix> generateExtractMethodFixes(ClonePairData pair, CloneResolutionGenerator parent){
		StatementGroup b;
		try {
			b = FindBlock.findLargestBlock(pair.getOtherRegion(), Mode.EXTRACT);
		} catch (CoreException e) {
			scoreLog.error("Error accessing block for clone pair", e);
			return Lists.newArrayList();
		}
		
		if (b == null || !ExtractMethodFix.canExtract(b)){
			return Lists.newArrayList();
		}
	
		double base = truncate(pair.getSimilarity() * Math.pow(NONLOCAL_PENALTY, b.getNumCapturedVariable()));
		
		int score = calc(base, EXTRACT, pair.getCloneNumber());
		
		SourceRegion region;
		try {
			region = new SourceRegion(pair.getOtherRegion().getFile(), new Document(pair.getOtherContents()), b.getStart(), b.getEnd());
		} catch (BadLocationException e) {
			throw new RuntimeException(e);
		}
		
		return Lists.<CloneFix>newArrayList(new ExtractMethodFix(pair, score, parent, region));
	}
	
	private List<CloneFix> generatePasteCloneFixes(ClonePairData pair, CloneResolutionGenerator parent){
		StatementGroup copyBlock;
		StatementGroup pasteBlock;
		
		try {
			copyBlock = FindBlock.findLargestBlock(pair.getOtherRegion(), Mode.PASTE);
			pasteBlock = FindBlock.findLargestBlock(pair.getSourceRegion(), Mode.PASTE);
		} catch (CoreException e) {
			scoreLog.error("Error accessing block for clone pair", e);
			return Lists.newArrayList();
		}
		if (copyBlock == null || pasteBlock == null){
			return Lists.newArrayList();
		}
		
		copyBlock = copyBlock.extend();
		
		LinkedHashSet<String> copyIds = copyBlock.getCapturedVariables();
		LinkedHashSet<String> pasteIds = pasteBlock.getCapturedVariables();
		
		int unmatched = Math.max(0, pasteIds.size() - copyIds.size());
		
		double base = truncate(pair.getSimilarity() * Math.pow(MISMATCH_PENALTY, unmatched));
		int score = calc(base, EXTRACT, pair.getCloneNumber());
		
		return Lists.<CloneFix>newArrayList(new CopyPasteFix(pair, score, parent, pasteBlock, copyBlock));
	}
		
	/**
	 * Force the relevance score to be an integer between {@link Scorer#MIN_SCORE} and {@link Scorer#MAX_SCORE}
	 * @param x the score
	 * @return an integer score between {@link Scorer#MIN_SCORE} and {@link Scorer#MAX_SCORE}
	 */
	private int truncate(double x){
		return Math.max(MIN_SCORE, Math.min((int) x, MAX_SCORE));
	}

	/**
	 * Action to perform when development has occurred in the editor
	 */
	private void onDevelopment(){
		if (!since.isEmpty()){
			scoreLog.debug("Development decay for clones " + since.toString());
		
			if (lastSeen != null){
				Activator.evaluationLog.info("User ignored fixes (with development): " + fixList(lastSeen));
			}
		}
	
		developmentDecayCnt.addAll(since);
		since.clear();
		lastSeen = null;
	}
	
	/**
	 * Scale the value <code>x</code> in range <code>(oMin, oMax)</code> to the range <code>(nMin, nMax)</code>
	 * @param x a value in <code>(oMin, oMax)</code>
	 * @param oMin minimum value of the original range
	 * @param oMax maximum value of the original range
	 * @param nMin minimum value of the new range
	 * @param nMax maximum value of the new range
	 * @return the scaled value
	 */
	public static double scale(double x, double oMin, double oMax, double nMin, double nMax){
	    double oRng = (oMax - oMin);
	    double nRng = (nMax - nMin);
	    double result = (((x - oMin) * nRng) / oRng) + nMin;
	    
		if (result > nMax || result < nMin){
		    throw new RuntimeException("Internal calculation error");
		}
	    
	    return result;
	}
	
	private int actionIndex(CloneFix fix){
		if (fix instanceof JumpToFix){
			return JUMP;
		}else if (fix instanceof CopyPasteFix){
			return PASTE;
		}else if (fix instanceof ExtractMethodFix){
			return EXTRACT;
		}else if (fix instanceof InsertCallFix){
			return INSERT;
		}else{
			throw new IllegalArgumentException();
		}
	}
	
}
