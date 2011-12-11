package edu.washington.cs.rtrefactor.util;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import com.google.common.collect.HashMultiset;

import edu.washington.cs.rtrefactor.detect.SourceRegion;

/**
 * For each statement, recursively determine the number of <i>bottom-level</i> 
 * statements -- statements that do contain any other statements -- covered (and uncovered) by
 * a given source region.
 * @author Todd Schiller
 */
public class StatementCoverageVisitor extends ASTVisitor {
    private final SourceRegion query;
    
    private final HashMultiset<Statement> covered = HashMultiset.create();
    private final HashMultiset<Statement> uncovered = HashMultiset.create();
   
    /**
     * Constructor taking a query region
     * @param query the query region
     */
    public StatementCoverageVisitor(SourceRegion query) {
        super();
        this.query = query;
    }
   
    @Override
    public boolean visit(Block node){
        for (Object s : node.statements()){
            Statement statement = (Statement) s;
            
            if (isBottomLevel(statement)){
                HashMultiset<Statement> tracker = covers(statement, query) ? covered : uncovered;
                
                // traverse up the AST, added each statement to 
                // covered or uncovered, accordingly.
                ASTNode current = statement;
                do{
                    if (current instanceof Statement){
                        tracker.add((Statement) current);
                    }
                    current = current.getParent();
                }while(current != null);
            }else{
                // the statement will be visited automatically later 
                // during the tree traversal
            }
        }
        return true;
    }
    
    /**
     * Get the number of statements covered in <code>statement</code>
     * @param statement the query 
     * @return the number of statements covered in <code>statement</code>
     */
    public int getNumCovered(Statement statement){
        return covered.count(statement);
    }
    
    /**
     * Get the number of statements uncovered in <code>statement</code>
     * @param statement the query 
     * @return the number of statements uncovered in <code>statement</code>
     */
    public int getNumUncovered(Statement statement){
        return uncovered.count(statement);
    }
    
    /**
     * true iff <code>statement</code> <i>cannot</i> contain any sub-statements. Does
     * not check for sub-statements, though.
     * @param statement the statement to check
     * @return true iff <code>statement</code> cannot contain any sub-statements
     */
    public static boolean isBottomLevel(Statement statement){
        return ! (statement instanceof Block
                || statement instanceof DoStatement
                || statement instanceof ForStatement
                || statement instanceof WhileStatement
                || statement instanceof EnhancedForStatement
                || statement instanceof SwitchStatement
                || statement instanceof TryStatement
                || statement instanceof IfStatement);
    }
    
    /**
     * true iff <code>query</code> contains any part of <code>statement</code>
     * @param statement the statement
     * @param query the source region
     * @return true iff <code>query</code> contains any part of <code>statement</code>
     */
    public static boolean covers(Statement statement, SourceRegion query){
        int coverBegin = Math.max(statement.getStartPosition(), query.getStart().getGlobalOffset());
        int coverEnd = Math.min(statement.getStartPosition() + statement.getLength(), query.getEnd().getGlobalOffset());
        return (coverEnd - coverBegin > 0);
    }
}
