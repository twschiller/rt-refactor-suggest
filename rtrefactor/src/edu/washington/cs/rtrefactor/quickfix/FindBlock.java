package edu.washington.cs.rtrefactor.quickfix;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Statement;

import com.google.common.collect.Lists;

import edu.washington.cs.rtrefactor.detect.SourceRegion;

/**
 * Methods for finding and scoring the code to extract
 * @author Todd Schiller
 */
public class FindBlock {

	/**
	 * Contains information about the statements to extract
	 * @author Todd Schiller
	 */
	public static class BlockInfo{
		private List<Statement> statements;

		protected BlockInfo(List<Statement> statements) {
			if (statements.isEmpty()){
				throw new IllegalArgumentException("Block cannot be empty");
			}
			
			this.statements = Lists.newArrayList(statements);
		}
		
		/**
		 * Get the starting global file offset
		 * @return the starting global file offset
		 */
		public int getStart(){
			return statements.get(0).getStartPosition();
		}
		
		/**
		 * Get the ending global file offset
		 * @return the ending global file offset
		 */
		public int getEnd(){
			Statement last =  statements.get(statements.size() - 1);
			return last.getStartPosition() + last.getLength();
		}
	}

	/**
	 * Find the largest block (list of statements) that overlaps <code>region</code> in the Eclipse
	 * workspace. Returns <code>null</code> if the region does not correspond to 
	 * a block.
	 * @param region the query region
	 * @return the largest block <code>region</code> in the Eclipse workspace
	 * @throws CoreException iff an error occurred when accessing a workspace resource
	 */
	public static BlockInfo findLargestBlock(SourceRegion region) throws CoreException{
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		
		// Get all projects in the workspace
		IProject[] projects = root.getProjects();
		// Loop over all projects
		for (IProject project : projects) {

			// Only work on open projects with the Java nature
			if (project.isOpen()
					&& project.isNatureEnabled("org.eclipse.jdt.core.javanature")) {
				IJavaProject javaProject = JavaCore.create(project);
				
				for (IPackageFragment p : javaProject.getPackageFragments())
				{
					if (p.getKind() == IPackageFragmentRoot.K_SOURCE) {
						for (ICompilationUnit cu : p.getCompilationUnits()){
							if (cu.getElementName().equals(region.getFile().getName())){
								return findLargestBlock(region, cu);
							}
						}
					}
				}
			}
		}
		throw new RuntimeException("Compilation unit corresponding to query file " + region.getFile().getAbsolutePath() + " not found");
	}	
	
	private static BlockInfo findLargestBlock(SourceRegion region, ICompilationUnit cu){
		if (!cu.getElementName().equals(region.getFile().getName())){
			throw new IllegalArgumentException("File name for source region query does not match the compilation unit's name");
		}
		
		CompilationUnit unit = parse(cu);
		
		RegionFinder f = new RegionFinder(region);
		unit.accept(f);
		
		return f.maxRegion;
	}
	
	/**
	 * Parse the compilation unit using {@link AST#JLS3}, which supports Java 1.5 features.
	 * Does not perform type resolution.
	 * @param unit the Eclipse compilation unit
	 * @return the compilation unit in AST form
	 */
	private static CompilationUnit parse(ICompilationUnit unit) {
		ASTParser parser = ASTParser.newParser(AST.JLS3); 
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(unit); // set source
		parser.setResolveBindings(false); // we need bindings later on
		return (CompilationUnit) parser.createAST(null /* IProgressMonitor */); // parse
	}
	
	/**
	 * true iff <code>query</code> contains any part of <code>statement</code>
	 * @param statement the statement
	 * @param query the source region
	 * @return true iff <code>query</code> contains any part of <code>statement</code>
	 */
	private static boolean covers(Statement statement, SourceRegion query){
		int coverBegin = Math.max(statement.getStartPosition(), query.getStart().getGlobalOffset());
		int coverEnd = Math.min(statement.getStartPosition() + statement.getLength(), query.getEnd().getGlobalOffset());
		return (coverEnd - coverBegin > 0);
	}
	
	/**
	 * Class to traverse the AST and find the largest block (list of consecutive statements
	 * covered by the specified source region. 
	 * @author Todd Schiller
	 */
	private static class RegionFinder extends ASTVisitor{
		private int max = Integer.MIN_VALUE;
		private final SourceRegion query;
	
		/**
		 * The longest block (list of consecutive statements) covered by the region
		 */
		protected BlockInfo maxRegion = null;
		
		/**
		 * Constructor taking a query region
		 * @param query the query region
		 */
		protected RegionFinder(SourceRegion query) {
			super();
			this.query = query;
		}

		@Override
		public boolean visit(Block node){
			List<Statement> inc = Lists.newArrayList();
			
			for (Object s : node.statements()){
				if (covers((Statement) s, query)){
					inc.add((Statement)s);
				}
			}
			
			if (!inc.isEmpty()){
				if (inc.size() > max){
					max = inc.size();
					maxRegion = new BlockInfo(inc);
				}
			}
			
			return true;
		}
	}
}
