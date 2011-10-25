package edu.washington.cs.rtrefactor.detect;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eposoft.jccd.data.ASourceUnit;
import org.eposoft.jccd.data.JCCDFile;
import org.eposoft.jccd.data.SimilarityGroupManager;
import org.eposoft.jccd.data.SimilarityPair;
import org.eposoft.jccd.data.SourceUnitPosition;
import org.eposoft.jccd.data.ast.ANode;
import org.eposoft.jccd.data.ast.NodeTypes;
import org.eposoft.jccd.detectors.APipeline;
import org.eposoft.jccd.detectors.ASTDetector;
import org.eposoft.jccd.preprocessors.APreprocessor;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import edu.washington.cs.rtrefactor.Activator;
import edu.washington.cs.rtrefactor.preferences.PreferenceUtil.Preference;

/**
 * A simple clone detector using the <a href="http://jccd.sourceforge.net/">JCCD clone detection pipeline</a>  
 * @author Todd Schiller
 */
public class JccdDetector implements IActiveDetector, IDetector{

//  Example code for sorting Similarity pairs by quality score
//	List<SimilarityPair> xxx = Lists.newArrayList(groups.getPairs());
//	Collections.sort(xxx, new Comparator<SimilarityPair>(){
//		@Override
//		public int compare(SimilarityPair o1, SimilarityPair o2) {
//			return Double.compare(qualityScore(o1), qualityScore(o2));
//		}
//	});
	
	public static final String NAME = "JCCD";
	
	private static final String PREPROCESSOR_PACKAGE = "org.eposoft.jccd.preprocessors.java";
	
	private final APipeline<?> detector = new ASTDetector();

	// TODO add the other configuration options
	
	public static final Preference<?> PREFERENCES[] = new Preference[]{
		new Preference<Boolean>(NAME, "GeneralizeMethodDeclarationNames", "Ignore method declaration names", true),
		new Preference<Boolean>(NAME, "GeneralizeVariableNames", "Ignore variable names", true),
		new Preference<Boolean>(NAME, "CompleteToBlock", "Adds blocks to single arguments around control flow", false),
		new Preference<Boolean>(NAME, "GeneralizeMethodArgumentTypes", "Ignore method argument types", false),
		new Preference<Boolean>(NAME, "GeneralizeMethodReturnTypes", "Ignore method return types", false),
		new Preference<Boolean>(NAME, "GeneralizeClassDeclarationNames", "Ignore class declaration names", true),
	};
	
	/**
	 * Create a JCCD code clone detector using the options from the Eclipse preference page
	 */
	public JccdDetector(){
		for (Preference<?> x : PREFERENCES){
			setPreference(x);
		}	
	}
	
	/**
	 * Set the JCCD preferences by reflectively instantiating corresponding operator
	 * @param preference the preference descriptor
	 */
	private <Q> void setPreference(Preference<Q> preference){	
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		
		try {
			String name = "set" + preference.getName();
			Q val = preference.getDefault();
			
			if (val instanceof Boolean){
				if (store.getBoolean(name)){
					detector.addOperator((APreprocessor) Class.forName(PREPROCESSOR_PACKAGE + "." + name).newInstance());
				}
			}else{
				throw new RuntimeException("Preference type " + val.getClass().getSimpleName() + " not supported");
			}
			
		} catch (Exception e) {
			throw new RuntimeException("Error setting preference " + preference, e);
		}
	}
	
	/**
	 * Get the {@link File} that the source unit is contained in
	 * @param node the source unit
	 * @return the file the source unit is contained in
	 */
	private static File getFile(ASourceUnit node){
		ANode fileNode = (ANode) node;
		while (fileNode.getType() != NodeTypes.FILE.getType()) {
			fileNode = fileNode.getParent();
		}
		return new File(fileNode.getText());
	}
	
	/**
	 * Get the number of lines in a source unit
	 * @param node the source unit
	 * @return the number of lines in the source unit
	 */
	private static int cloneLength(ASourceUnit node){
		SourceUnitPosition minPos = APipeline.getFirstNodePosition((ANode) node);
		SourceUnitPosition maxPos = APipeline.getLastNodePosition((ANode) node);	
		return maxPos.getLine() - minPos.getLine() + 1;
	}
	
	/**
	 * Compute the quality score for a clone pair
	 * @param clonePair the clone pair
	 * @return the quality score for the clone pair
	 */
	public static double qualityScore(SimilarityPair clonePair){
		return (cloneLength(clonePair.getFirstNode()) + cloneLength(clonePair.getSecondNode())) / 2.0;
	}
	
	/**
	 * Convert a JCCD node describing a region to the common region interface
	 * @param node JCCD analysis node
	 * @return 
	 */
	private static SourceRegion mkRegion(ANode node){
		SourceUnitPosition minPos = APipeline.getFirstNodePosition(node);
		SourceUnitPosition maxPos = APipeline.getLastNodePosition(node);	
		
		return new SourceRegion(
				new SourceLocation(getFile(node), minPos.getLine(), minPos.getCharacter()),
				new SourceLocation(getFile(node), maxPos.getLine(), maxPos.getCharacter())
				);
	}
	
	@Override
	/**
	 * {@inheritDoc}
	 */
	public Set<ClonePair> detect(Map<File, String> dirty, SourceRegion active)
			throws CoreException {
		return new HashSet<ClonePair>(Sets.filter(detect(dirty), new DetectorUtil.ActiveRegion(active)));
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	public Set<ClonePair> detect(Map<File, String> dirty) throws CoreException {
		Set<ClonePair> result = Sets.newHashSet();
		
		List<File> files = DetectorUtil.collect();
	
		List<JCCDFile> jccdFiles = Lists.transform(files, new Function<File, JCCDFile>(){
			@Override
			public JCCDFile apply(File input) {
				return new JCCDFile(input);
			}
		});
		
		detector.setSourceFiles(jccdFiles.toArray(new JCCDFile[]{}));
		
		SimilarityGroupManager m = detector.process();
		
		for (SimilarityPair p : m.getPairs()){
			result.add(
				new ClonePair(
					mkRegion((ANode) p.getFirstNode()),
					mkRegion((ANode) p.getSecondNode()),
					qualityScore(p)));
		}
	
		return result;
	}
	
	@Override
	/**
	 * {@inheritDoc}
	 */
	public String getName(){
		return NAME;
	}
}
