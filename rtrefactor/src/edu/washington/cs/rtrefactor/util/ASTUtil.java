package edu.washington.cs.rtrefactor.util;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeParameter;

/**
 * Utility functions for rewriting Eclipse ASTs
 * @author Todd Schiller
 */
public abstract class ASTUtil {

	@SuppressWarnings("unchecked")
	public static <T extends ASTNode> T copy(T node){
		return (T) ASTNode.copySubtree(node.getAST(), node);
	}
	
	public static Type convert(AST ast, ITypeBinding binding){
		if (binding.isParameterizedType()){
			ParameterizedType nt = ast.newParameterizedType(ast.newSimpleType(ast.newName(binding.getErasure().getQualifiedName())));
			
			for (ITypeBinding arg : binding.getTypeArguments()){
				nt.typeArguments().add(convert(ast, arg));
			}
		
			return nt;
		}else{
			return ast.newSimpleType(ast.newName(binding.getQualifiedName()));
		}
		
	}
	
	public static void replace(ASTNode before, ASTNode after){
		StructuralPropertyDescriptor location = before.getLocationInParent();
		
		if (location.isChildProperty() || location.isSimpleProperty()){
			before.getParent().setStructuralProperty(location, after);
		}else{
			assert location.isChildListProperty();
			
			if (before.getParent() instanceof MethodInvocation){
				//assume it's an argument
				MethodInvocation method = (MethodInvocation) before.getParent();
				for (int i = 0; i < method.arguments().size(); i++){
					if (method.arguments().get(i) == before){
						method.arguments().set(i, after);
						return;
					}
				}
				throw new IllegalArgumentException("Expected method argument: " + before.toString() );
			}else if (before.getParent() instanceof ClassInstanceCreation){
				//assume it's an argument
				ClassInstanceCreation method = (ClassInstanceCreation) before.getParent();
				for (int i = 0; i < method.arguments().size(); i++){
					if (method.arguments().get(i) == before){
						method.arguments().set(i, after);
						return;
					}
				}
				throw new IllegalArgumentException("Expected constructor argument: " + before.toString() );
			}else if (before.getParent() instanceof SwitchStatement){
				SwitchStatement swtch = (SwitchStatement) before.getParent();
				for (int i = 0; i < swtch.statements().size(); i++){
					if (swtch.statements().get(i) == before){
						swtch.statements().set(i, after);
						return;
					}
				}
				throw new IllegalArgumentException("Expected switch body statement: " + before.toString() );
			}else if (before.getParent() instanceof InfixExpression){
				InfixExpression expr = (InfixExpression) before.getParent();
				
				if (expr.getLeftOperand() == before){
					expr.setLeftOperand((Expression)after);
				}else if (expr.getRightOperand() == before){
					expr.setRightOperand((Expression)after);
				}	
				
				for (int i = 0; i < expr.extendedOperands().size(); i++){
					if (expr.extendedOperands().get(i) == before){
						expr.extendedOperands().set(i, after);
						return;
					}
				}
				
				throw new IllegalArgumentException("Expected operand: " + before.toString() );
			}else if (before.getParent() instanceof TypeParameter){
				TypeParameter param = (TypeParameter) before.getParent();
				
				for (int i = 0; i < param.typeBounds().size(); i++){
					if (param.typeBounds().get(i) == before){
						param.typeBounds().set(i, after);
						return;
					}
				}
				throw new IllegalArgumentException("Expected type bound: " + before.toString() );
			}else if (before.getParent() instanceof TypeDeclaration){
				TypeDeclaration dec = (TypeDeclaration) before.getParent();
				
				for (int i = 0; i < dec.superInterfaceTypes().size(); i++){
					if (dec.superInterfaceTypes().get(i) == before){
						dec.superInterfaceTypes().set(i, after);
						return;
					}
				}
				throw new IllegalArgumentException("Expected super interface type: " + before.toString() );
				
			}else{
				throw new IllegalArgumentException("AST node is a child list: " + before.toString() );
			}
		
		}
	}
	
	public static void replaceInBlock(Statement node, ASTNode replacement){
		if (replacement instanceof Block){
			replaceInBlock(node, (Block) replacement);
		}else if (replacement instanceof Statement){
			replaceInBlock(node, (Statement) replacement);
		}else{
			throw new IllegalArgumentException("replacement has type " + replacement.getClass().getName());
		}
	}
	
	@SuppressWarnings("unchecked")
	public static void replaceInBlock(Statement node, Block replacement){
		replaceInBlock(node, (List<Statement>) replacement.statements());
	}
	
	public static void replaceInBlock(Statement node, Statement replacement){
		if (replacement instanceof Block){
			replaceInBlock(node, (Block) replacement);
		}else{
			List<Statement> ss = new ArrayList<Statement>();
			ss.add(replacement);
			replaceInBlock(node, ss);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static void replaceInBlock(Statement node, List<Statement> replacements){
		if (!(node.getParent() instanceof Block)){
			throw new IllegalArgumentException("Original statement must be a member of a block");
		}
		
		Block block = (Block) node.getParent();
		
		for (int i = 0; i < block.statements().size(); i++){
			if (block.statements().get(i) == node){
				block.statements().remove(i);
				
				for (int j = replacements.size() - 1; j >= 0; j--){
					Statement replacement = replacements.get(j);
					if (replacement.getAST() != node.getAST()){
						throw new IllegalArgumentException("One or more replacements is not from the same AST as the node");
					}
					
					block.statements().add(i, ASTNode.copySubtree(node.getAST(), replacement));
				}
				
			}
		}		
	}
}
