package edu.washington.cs.rtrefactor.detect;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
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
import com.google.common.collect.BiMap;
import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;

import edu.washington.cs.rtrefactor.Activator;
import edu.washington.cs.rtrefactor.preferences.PreferenceUtil.Preference;
import edu.washington.cs.rtrefactor.util.FileUtil;

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
	private static final String AST_COMPARATORS_PACKAGE = "org.eposoft.jccd.comparators.ast.java";

	private final APipeline<?> detector = new ASTDetector();

	// TODO add the other configuration options

	public static final Preference<?> PREFERENCES[] = new Preference[]{
		new Preference<Boolean>(NAME, "GeneralizeMethodDeclarationNames", "Ignore method declaration names", true),
		new Preference<Boolean>(NAME, "GeneralizeVariableNames", "Ignore variable names", true),
		
		new Preference<Boolean>(NAME, "GeneralizeClassDeclarationNames", "Ignore class declaration names", true),
		
		new Preference<Boolean>(NAME, "GeneralizeMethodArgumentTypes", "Ignore method argument types", false),
		new Preference<Boolean>(NAME, "GeneralizeMethodCallNames", "Ignore method calls", false),
		new Preference<Boolean>(NAME, "GeneralizeMethodDeclarationNames", "Ignore method declarations", true),
		new Preference<Boolean>(NAME, "GeneralizeMethodReturnTypes", "Ignore method return types", false),
		
		new Preference<Boolean>(NAME, "RemoveThrow", "Ignore throws introduced with throw keyword", false),
		new Preference<Boolean>(NAME, "RemoveThrowsClause", "Ignore throws part of method declarations", false),
		
		new Preference<Boolean>(NAME, "RemoveSemicolons", "Ignore semicolons", false),
		new Preference<Boolean>(NAME, "RemoveRedundantParentheses", "Ignore unnecessary parentheses", true),
		new Preference<Boolean>(NAME, "RemoveAnnotations", "Ignore all annotations", true),
		new Preference<Boolean>(NAME, "RemoveAssertions", "Ignore all assertions", true),
		
		new Preference<Boolean>(NAME, "ReduceIdentifiersToCapitalLetterSubstring", "Ignore package qualifiers on types", false),
		new Preference<Boolean>(NAME, "ReduceIdentifiersToLastSubstring", "Ignore package and class qualifiers on types", true),
	
		new Preference<Boolean>(NAME, "AcceptIdentifiers", "Ignore all identifiers", true),
		new Preference<Boolean>(NAME, "AcceptVariableIdentifiers", "Ignore all variable identifiers", false),
		new Preference<Boolean>(NAME, "CompleteToBlock", "Adds blocks to single arguments around control flow", true),
		new Preference<Boolean>(NAME, "AcceptNumberLiterals", "Ignore number literals", true),
		new Preference<Boolean>(NAME, "AcceptNumberTypeNames", "Ignore primitive number types", true),
		new Preference<Boolean>(NAME, "AcceptPrimitives", "Ignore primitive values", false),
		new Preference<Boolean>(NAME, "AcceptPrimitiveTypes", "Ignore primitive types", false),
		new Preference<Boolean>(NAME, "AcceptStringLiterals", "Ignores all string literals", false),
		new Preference<Boolean>(NAME, "GeneralizeTypes", "Ignores all types", false),
		new Preference<Boolean>(NAME, "RemoveGenericTypes", "Ignores all generics", false),
		new Preference<Boolean>(NAME, "RemoveVariableDeclerationNodes", "Ignores all variable declarations", false),
		new Preference<Boolean>(NAME, "RenameVariableNamesLocalConsistent", "Ignores local variable renaming", true),
		
	};

	/**
	 * Create a JCCD code clone detector using the options from the Eclipse preference page
	 */
	public JccdDetector(){
		for (Preference<?> x : PREFERENCES){
			setPreference(x);
		}	
	}

	private boolean exists(String pkg, String name){
		try{
			Class.forName(pkg + "." + name);
			return true; 
		}catch (Exception ex){
			return false;
		}
	}
	private Class getClass(String pkg, String name){
		try {
			return Class.forName(pkg + "." + name);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
	
	/**
	 * Set the JCCD preferences by reflectively instantiating corresponding operator
	 * @param preference the preference descriptor
	 */
	private <Q> void setPreference(Preference<Q> preference){	
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();

		try{
			Q val = preference.getDefault();

			if (val instanceof Boolean){
				if (store.getBoolean(preference.getKey())){
					String name = preference.getName();
					
					if (exists(PREPROCESSOR_PACKAGE, preference.getName())){
						detector.addOperator((APreprocessor)getClass(PREPROCESSOR_PACKAGE, name).newInstance());
					}else if (exists(AST_COMPARATORS_PACKAGE, preference.getName())){
						detector.addOperator((APreprocessor)getClass(AST_COMPARATORS_PACKAGE, name).newInstance());
					}else{
						throw new RuntimeException("Preference " + name + " not supported");
					}
				}
			}else{
				throw new RuntimeException("Preference type " + val.getClass().getSimpleName() + " not supported");
			}
		}catch(Exception e){

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
	 * @param map from result filenames to Eclipse resource filenames
	 * @param node JCCD analysis node
	 * @return a source region
	 * @throws BadLocationException if location indicated by node is bad
	 * @throws IOException if cannot read file indicated by node
	 */
	private static SourceRegion mkRegion(BiMap<File, File> files, ANode node) throws BadLocationException, IOException{
		SourceUnitPosition minPos = APipeline.getFirstNodePosition(node);
		SourceUnitPosition maxPos = APipeline.getLastNodePosition(node);	

		File surface = getFile(node);
		File underlying = files.get(surface);

		Document underlierDoc = new Document(FileUtil.read(surface));

		return new SourceRegion(
				new SourceLocation(underlying, minPos.getLine(), 
						minPos.getCharacter(), underlierDoc),
						new SourceLocation(underlying, maxPos.getLine(), 
								maxPos.getCharacter(), underlierDoc)
				);

	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	public Set<ClonePair> detect(Map<File, String> dirty, SourceRegion active) throws CoreException, IOException {

		return new HashSet<ClonePair>(Sets.filter(detect(dirty), new DetectorUtil.ActiveRegion(active)));
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	public Set<ClonePair> detect(Map<File, String> dirty) throws CoreException, IOException {
		DetectorUtil.detectLog.debug("Begin full clone detection with detector JCCD");

		Set<ClonePair> result = Sets.newHashSet();

		// switch direction: result name -> resource name
		BiMap<File, File> files = DetectorUtil.collect(dirty).inverse();

		Collection<JCCDFile> jccdFiles = Collections2.transform(files.keySet(), new Function<File, JCCDFile>(){
			@Override
			public JCCDFile apply(File input) {
				return new JCCDFile(input);
			}
		});

		detector.setSourceFiles(jccdFiles.toArray(new JCCDFile[]{}));

		SimilarityGroupManager m = detector.process();

		try {
			for (SimilarityPair p : m.getPairs()){
				result.add(
						new ClonePair(
								mkRegion(files, (ANode) p.getFirstNode()),
								mkRegion(files, (ANode) p.getSecondNode()),
								qualityScore(p)));
			}
		} catch (BadLocationException ex) {
			throw new RuntimeException("Bad location in JCCD node conversion " + ex.getMessage());
		}

		for (File underlier : dirty.keySet()){
			File tmp = files.inverse().get(underlier);
			try{
				tmp.delete();
				DetectorUtil.detectLog.debug("Deleted temporary file " + tmp.getAbsolutePath());
			}catch(Exception ex){
				DetectorUtil.detectLog.debug("Error deleting temporary file " + tmp.getAbsolutePath(), ex);
			}
		}

		DetectorUtil.detectLog.debug("End full clone detection with detector JCCD");
		return result;
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	public String getName(){
		return NAME;
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	public void destroy() {
		// no cleanup is necessary
	}
}
