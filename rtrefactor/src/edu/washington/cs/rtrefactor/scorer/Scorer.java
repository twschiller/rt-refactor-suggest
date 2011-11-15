package edu.washington.cs.rtrefactor.scorer;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.ui.IMarkerResolution;

import com.google.common.collect.Lists;

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
	
	/**
	 * Generate the marker resolutions for the given code clone <code>pair</code>. Resolutions
	 * are given scores based on the estimated difficulty / usefulness of performing the action.
	 * 
	 * Multiple resolutions of the same type may be produced if the clone overlaps multiple methods.
	 * 
	 * @param pair the code clone pair
	 * @return the marker resolutions
	 */
	public static List<IMarkerResolution> calculateResolutions(ClonePairData pair){
		List<IMarkerResolution> rs = Lists.newArrayList();
		
		rs.addAll(generateJumpToCloneFixes(pair));
		rs.addAll(generateInsertMethodCallFixes(pair));
		rs.addAll(generateExtractMethodFixes(pair));
		rs.addAll(generatePasteCloneFixes(pair));
		
		return rs;
	}
	
	private static List<IMarkerResolution> generateJumpToCloneFixes(ClonePairData pair){
		// TODO if the user is editing existing code (instead of writing new code), increase the score for "jump to clone"?
		return Lists.<IMarkerResolution>newArrayList(new JumpToFix(pair, BASE_JUMPTOCLONE_SCORE));
	}
	
	/**
	 * Generate Insert Method Call fixes, score according to the following criteria:
	 * (1) the base score (2) the number of arguments, and (3) the percent of the method covered by the clone.
	 * @param pair the clone pair
	 * @return scored insert method call fixes
	 */
	private static List<IMarkerResolution> generateInsertMethodCallFixes(ClonePairData pair){
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
		
		return Lists.<IMarkerResolution>newArrayList(new InsertCallFix(pair, (int) score));
	}
	
	private static List<IMarkerResolution> generateExtractMethodFixes(ClonePairData pair){
		return Lists.<IMarkerResolution>newArrayList(new ExtractMethodFix(pair, BASE_EXTRACTMETHODCALL_SCORE));
	}
	
	private static List<IMarkerResolution> generatePasteCloneFixes(ClonePairData pair){
		return Lists.<IMarkerResolution>newArrayList(new CopyPasteFix(pair, BASE_PASTECLONE_SCORE));
	}
}
