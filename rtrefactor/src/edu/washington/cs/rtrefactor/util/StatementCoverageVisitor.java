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
   
    private boolean recorded(Statement statement){
        return covered.contains(statement) || uncovered.contains(statement);
    }
    
    /**
     * record coverage for the statement if it is a bottom level statement
     * and the information has not been recorded yet
     * @param statement
     */
    private void record(Statement statement){
        if (!recorded(statement) && isBottomLevel(statement)){
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
     
    @Override
    public boolean visit(WhileStatement node) {
        record(node.getBody());
        return true;
    }

    @Override
    public boolean visit(DoStatement node) {
        record(node.getBody());
        return true;
    }

    @Override
    public boolean visit(EnhancedForStatement node) {
        record(node.getBody());
        return true;
    }

    @Override
    public boolean visit(ForStatement node) {
        record(node.getBody());
        return true;
    }

    @Override
    public boolean visit(SwitchStatement node) {
        for (Object s : node.statements()){
            record((Statement) s);
        }
        return true;
    }

    @Override
    public boolean visit(IfStatement node) {
        if (node.getThenStatement() != null && !recorded(node.getThenStatement())){
            record(node.getThenStatement());
        }
        if (node.getElseStatement() != null && !recorded(node.getElseStatement())){
            record(node.getElseStatement());
        }
        return true;
    }

    @Override
    public boolean visit(Block node){
        for (Object s : node.statements()){
            record((Statement) s);
        }
        return true;
    }
    
    /**
     * Get the number of statements covered in <code>statement</code>
     * @param statement the query 
     * @return the number of statements covered in <code>statement</code>
     */
    public int getNumCovered(Statement statement){
        int cnt = covered.count(statement);
        if (cnt == 0){
            assert uncovered.count(statement) != 0;
        }
        return cnt;
    }
    
    /**
     * Get the number of statements uncovered in <code>statement</code>
     * @param statement the query 
     * @return the number of statements uncovered in <code>statement</code>
     */
    public int getNumUncovered(Statement statement){
        int cnt = uncovered.count(statement);
        if (cnt == 0){
            assert covered.count(statement) != 0;
        }
        return cnt;
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
