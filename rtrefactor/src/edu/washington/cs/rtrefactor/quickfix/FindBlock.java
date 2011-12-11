package edu.washington.cs.rtrefactor.quickfix;

import java.util.LinkedHashSet;
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
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import soot.Modifier;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import edu.washington.cs.rtrefactor.detect.SourceRegion;
import edu.washington.cs.rtrefactor.util.StatementCoverageVisitor;

// TODO we need to penalize for occurrences of local variables use a variable that would be 
// defined locally in the extracted method

/**
 * Methods for finding and scoring the code to extract
 * @author Todd Schiller
 */
public class FindBlock {

	/**
	 * A group of consecutive statements, within a block
	 * @author Todd Schiller
	 */
	public static class StatementGroup{
		private final List<Statement> topLevelStatements;
		private final CompilationUnit cu;

		/**
		 * Construct taking a list of the top-level statements in the group
		 * @param topLevelStatements the top-level statements in the group
		 * @param cu the compilation unit
		 */
		protected StatementGroup(List<Statement> topLevelStatements, CompilationUnit cu) {
			if (topLevelStatements.isEmpty()){
				throw new IllegalArgumentException("Block cannot be empty");
			}else if (cu == null){
				throw new NullPointerException("Compilation Unit cannot be null");
			}
			
			this.topLevelStatements = Lists.newArrayList(topLevelStatements);
			this.cu = cu;
		}
		
		/**
		 * Get the starting global file offset
		 * @return the starting global file offset
		 */
		public int getStart(){
			return topLevelStatements.get(0).getStartPosition();
		}
		
		/**
		 * Get the compilation unit in which this block resides.
		 * @return the compilation unit in which this block resides.
		 */
		public CompilationUnit getCompilationUnit() {
			return cu;
		}
			
		/**
		 * Get the ending global file offset
		 * @return the ending global file offset
		 */
		public int getEnd(){
			Statement last =  topLevelStatements.get(topLevelStatements.size() - 1);
			return last.getStartPosition() + last.getLength();
		}
		
		/**
		 * Get the number of variables that would are captured by the statements
		 * in this block. Captured variables are those that are used, but not declared within
		 * the block (excluding static variables).
		 * @return the number of captured variables
		 */
		public int getNumCapturedVariable(){
			return getCapturedVariables().size();
		}
		
		/**
		 * Get the set of variables that  are captured by the statements
		 * in this block. Captured variables are those that are used, but not declared within
		 * the block (excluding static variables).
		 * @return the set of captured variables
		 */
		public LinkedHashSet<String> getCapturedVariables() {
			VariableCaptureCounter counter = new VariableCaptureCounter();
			for (Statement s : topLevelStatements){
				s.accept(counter);
			}
			return counter.captured;
		}
		
		/**
		 * Get a shallow reference to the top-level statements
		 * @return a shallow reference to the top-level statements
		 */
		public List<Statement> getTopLevelStatements(){
			return topLevelStatements;
		}
	}	
		
	
	/**
	 * Find the largest {@link StatementGroup} that is covered by <code>region<code> in the Eclipse
	 * workspace. Returns <code>null</code> if the region does not correspond to any statements.
	 * @param region the query region
	 * @return the largest {@link StatementGroup} that is covered by <code>region<code>
	 * @throws CoreException iff an error occurred when accessing a workspace resource
	 */
	public static StatementGroup findLargestBlock(SourceRegion region) throws CoreException{
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
	
	private static StatementGroup findLargestBlock(SourceRegion region, ICompilationUnit cu){
		if (!cu.getElementName().equals(region.getFile().getName())){
			throw new IllegalArgumentException("File name for source region query does not match the compilation unit's name");
		}
		
		CompilationUnit unit = parse(cu);
		
		StatementCoverageVisitor coverage = new StatementCoverageVisitor(region);
		unit.accept(coverage);
		
		LargestRegionVisitor f = new LargestRegionVisitor(unit, coverage);
		unit.accept(f);
		
		return f.maxGroup;
	}
	
	/**
	 * Parse the compilation unit using {@link AST#JLS3}, which supports Java 1.5 features.
	 * Resolved bindings.
	 * @param unit the Eclipse compilation unit
	 * @return the compilation unit in AST form
	 */
	private static CompilationUnit parse(ICompilationUnit unit) {
		ASTParser parser = ASTParser.newParser(AST.JLS3); 
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(unit); // set source
		parser.setResolveBindings(true); // we need bindings later on
		return (CompilationUnit) parser.createAST(null /* IProgressMonitor */); // parse
	}
	
	/**
	 * A {@link ASTVisitor} that calculates the largest {@link StatementGroup} with all the bottom-level
	 * statements covered by a query.
	 * @author Todd Schiller
	 */
	private static class LargestRegionVisitor extends ASTVisitor{
		
		private final CompilationUnit cu;
		private final StatementCoverageVisitor coverage;
		
		/**
		 * The largest group of statements seen so far
		 */
		private StatementGroup maxGroup = null;
		
		/**
		 * The number of bottom-level statements in {@link maxGroup}
		 */
		private int max = Integer.MIN_VALUE;
		
		/**
		 * Constructor taking a query region
		 * @param query the query region
		 */
		protected LargestRegionVisitor(CompilationUnit cu, StatementCoverageVisitor coverage) {
			super();
			this.cu = cu;
			this.coverage = coverage;
		}

        @Override
		public boolean visit(Block node){            
            // it's OK to search this way because the query source region is contiguous
            // i.e., each block only has a single source group that is covered
            
			List<Statement> inc = Lists.newArrayList();	
			
			// the number of bottom-level statements covered
			int size = 0;
			
			for (Object o : node.statements()){
			    Statement statement = (Statement) o;
			    if (coverage.getNumUncovered(statement) == 0){
			        inc.add(statement);
			        size += coverage.getNumCovered(statement);
			    }			    
			}
			
			if (size > 0 && size > max){
			    max = size;
			    maxGroup = new StatementGroup(inc, cu);
			}
			
			return true;
		}
	}
	
	/**
	 * Collects the set of captured variables, pass in a sequence of statements.
	 * @author Todd Schiller
	 */
	protected static class VariableCaptureCounter extends ASTVisitor{
		
		/**
		 * Variable ID of the first seen variable. <code>id < first</code> implies
		 * that variable with <code>id</code> has been captured.
		 */
		private int first = Integer.MAX_VALUE;
		
		/**
		 * Fully qualified names of captured variables
		 */
		protected LinkedHashSet<String> captured = Sets.newLinkedHashSet();
		
		@Override
		public boolean visit(VariableDeclarationStatement x){
			// only have to look at the first entry because we only care about the lowest number
			VariableDeclarationFragment f = (VariableDeclarationFragment) x.fragments().get(0);
			IVariableBinding binding = (IVariableBinding) f.getName().resolveBinding();
			first = Math.min(first, binding.getVariableId());
			return true;
		}
		
		@Override
		public boolean visit(SimpleName name){
			doName(name);
			return false;
		}
		
		@Override
		public boolean visit(QualifiedName name){
			doName(name);
			return false;
		}
		
		/**
		 * Add to set of captured variables if the binding is for a field, or the occurrence
		 * occurs after the first seen variable. Don't penalize for public static variables. 
		 * @param name the name of variable
		 */
		private void doName(Name name){
			if (!(name.getParent() instanceof VariableDeclarationFragment)){
				IBinding binding = name.resolveBinding();
				if (binding instanceof IVariableBinding){
					IVariableBinding vb = (IVariableBinding) binding;
					
					boolean isStatic = (vb.getModifiers() & Modifier.STATIC) > 0;
					
					if ((vb.getVariableId() < first || vb.isField() || vb.isParameter()) && !isStatic){
						captured.add(name.getFullyQualifiedName());
					}
				}
			}

		}
	}
}
