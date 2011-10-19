package edu.washington.cs.rtrefactor.detect;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.EnumSet;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eposoft.jccd.data.ASourceUnit;
import org.eposoft.jccd.data.JCCDFile;
import org.eposoft.jccd.data.SimilarityGroupManager;
import org.eposoft.jccd.data.SimilarityPair;
import org.eposoft.jccd.data.SourceUnitPosition;
import org.eposoft.jccd.data.ast.ANode;
import org.eposoft.jccd.data.ast.NodeTypes;
import org.eposoft.jccd.detectors.APipeline;
import org.eposoft.jccd.detectors.ASTDetector;
import org.eposoft.jccd.preprocessors.java.*;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

/**
 * A simple clone detector using the <a href="http://jccd.sourceforge.net/">JCCD clone detection pipeline</a>  
 * @author Todd Schiller
 */
public abstract class CloneDetector {

//  Example code for sorting Similarity pairs by quality score
//	List<SimilarityPair> xxx = Lists.newArrayList(groups.getPairs());
//	Collections.sort(xxx, new Comparator<SimilarityPair>(){
//		@Override
//		public int compare(SimilarityPair o1, SimilarityPair o2) {
//			return Double.compare(qualityScore(o1), qualityScore(o2));
//		}
//	});

	/**
	 * JCCD Clone Detection Options
	 * @author Todd Schiller
	 */
	public enum JccdOptions{
		GeneralizeMethodDeclarationNames,
		GeneralizeVariableNames,
		CompleteToBlock,
		GeneralizeMethodArgumentTypes,
		GeneralizeMethodReturnTypes,
		GeneralizeVariableDeclarationTypes,
		GeneralizeClassDeclarationNames
	}
	
	/**
	 * Add the {@code .java} files contained within {@code resource} to the list {@code files}
	 * @param resource the Eclipse resource
	 * @param files a non-null list of files
	 * @throws CoreException iff a resource is not accessible
	 */
	private static void collect(IResource resource, List<File> files) throws CoreException{
		if (resource instanceof IFile){
			IFile f = (IFile) resource;
			
			if (f.getFileExtension() != null && f.getFileExtension().equals("java")){
				files.add(f.getLocation().toFile());
			}
		}
		else if (resource instanceof IFolder){
			for (IResource s : ((IFolder) resource).members(false)){
				collect(s, files);
			}
		}
	}
	
	/**
	 * Get the {@link File} that the source unit is contained in
	 * @param node the source unit
	 * @return the file the source unit is contained in
	 */
	public static File getFile(ASourceUnit node){
		ANode fileNode = (ANode) node;
		while (fileNode.getType() != NodeTypes.FILE.getType()) {
			fileNode = fileNode.getParent();
		}
		return new File(fileNode.getText());
	}
	
	/**
	 * Get the source <i>lines</i> associated with {@code node}
	 * @param node the source unit
	 * @return the source lines corresponding to the source unit
	 * @throws IOException iff there is an error reading the file referred to by the source unit
	 */
	public static String getSource(ASourceUnit node) throws IOException{
		List<String> lines = Files.readLines(getFile(node), Charset.defaultCharset());

		SourceUnitPosition minPos = APipeline.getFirstNodePosition((ANode) node);
		SourceUnitPosition maxPos = APipeline.getLastNodePosition((ANode) node);	

		return Joiner.on(System.getProperty("line.separator")).join(lines.subList(minPos.getLine(), maxPos.getLine()));
	}
	
	/**
	 * Get the number of lines in a source unit
	 * @param node the source unit
	 * @return the number of lines in the source unit
	 */
	public static int cloneLength(ASourceUnit node){
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
	 * Compute the probable clone pairs across <i>all</i> open projects in the workspace
	 * @param options the set of JCCD operators to use
	 * @return probable clone pairs across all open projects in the workspace
	 * @throws CoreException iff a resource cannot be accessed
	 */
	public static SimilarityGroupManager detect(EnumSet<JccdOptions> options) throws CoreException{
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			
		APipeline<?> detector = new ASTDetector();
		
		List<File> files = Lists.newArrayList();
		for (IProject project : root.getProjects()){
			for (IResource r : project.members(false)){
				collect(r, files);
			}
		}
	
		List<JCCDFile> jccdFiles = Lists.transform(files, new Function<File, JCCDFile>(){
			@Override
			public JCCDFile apply(File input) {
				return new JCCDFile(input);
			}
		});
		
		detector.setSourceFiles(jccdFiles.toArray(new JCCDFile[]{}));
		
		if (options.contains(JccdOptions.GeneralizeClassDeclarationNames)){
			detector.addOperator(new GeneralizeClassDeclarationNames());
		}
		if (options.contains(JccdOptions.GeneralizeMethodDeclarationNames)){
			detector.addOperator(new GeneralizeMethodDeclarationNames());
		}
		if (options.contains(JccdOptions.GeneralizeVariableNames)){
			detector.addOperator(new GeneralizeVariableNames());
		}
		if (options.contains(JccdOptions.CompleteToBlock)){
			detector.addOperator(new CompleteToBlock());
		}
		if (options.contains(JccdOptions.GeneralizeMethodArgumentTypes)){
			detector.addOperator(new GeneralizeMethodArgumentTypes());
		}
		if (options.contains(JccdOptions.GeneralizeMethodReturnTypes)){
			detector.addOperator(new GeneralizeMethodReturnTypes());
		}
		if (options.contains(JccdOptions.GeneralizeVariableDeclarationTypes)){
			 detector.addOperator(new GeneralizeVariableDeclarationTypes());
		}
		
		return detector.process();
	}
}
