package edu.washington.cs.rtrefactor.reconciler;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jdt.internal.ui.javaeditor.JavaSourceViewer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IWidgetTokenKeeper;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.formatter.FormattingContextProperties;
import org.eclipse.jface.text.formatter.IFormattingContext;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;

/**
 * The only point of overriding this  class to is make get/set reconciler 
 * methods visible, so that we can install a custom reconciler.
 * 
 * @author Travis Mandel
 */
public class CloneSourceViewer extends JavaSourceViewer {

	private IReconciler fReconciler;
	private CloneEditor fEditor;


	private static final boolean CODE_ASSIST_DEBUG= "true".equalsIgnoreCase(Platform.getDebugOption("org.eclipse.jdt.ui/debug/ResultCollector")); //$NON-NLS-1$//$NON-NLS-2$

	public CloneSourceViewer(Composite parent, IVerticalRuler verticalRuler,
			IOverviewRuler overviewRuler, boolean showAnnotationsOverview,
			int styles, IPreferenceStore store, CloneEditor editor) {
		super(parent, verticalRuler, overviewRuler, showAnnotationsOverview, styles,
				store);
		fReconciler = null;
	}

	/** This should override JavaSourceViewer.getReconciler(), but that
	 * method is only package-level visible, so depending on which package 
	 * you are in different methods will be called.
	 * 
	 * @return the current reconciler
	 */
	public IReconciler getReconciler() {
		return fReconciler;
	}


	/** This should override JavaSourceViewer.setReconciler(), but that
	 * method is only package-level visible, so depending on which package 
	 * you are in different methods will be called.
	 * 
	 * Also includes a check to ensure the reconciler is a CloneReconciler
	 * 
	 * @return the current reconciler
	 */
	public void setReconciler(IReconciler reconciler) {
		if(reconciler instanceof CloneReconciler) {
			fReconciler= reconciler;
		} else {
			throw new IllegalArgumentException("Expecting clone reconciler");
		}
	}




	public IContentAssistant getContentAssistant() {
		return fContentAssistant;
	}

	/*
	 * @see ITextOperationTarget#doOperation(int)
	 */
	public void doOperation(int operation) {

		if (getTextWidget() == null)
			return;

		switch (operation) {
		case CONTENTASSIST_PROPOSALS:
			long time= CODE_ASSIST_DEBUG ? System.currentTimeMillis() : 0;
			String  msg= fContentAssistant.showPossibleCompletions();
			if (CODE_ASSIST_DEBUG) {
				long delta= System.currentTimeMillis() - time;
				System.err.println("Code Assist (total): " + delta); //$NON-NLS-1$
			}
			// setStatusLineErrorMessage(msg);
			///TODO: change to logger
			System.out.println(msg);
			return;
		case QUICK_ASSIST:
			/*
			 * XXX: We can get rid of this once the SourceViewer has a way to update the status line
			 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=787
			 */
			msg= fQuickAssistAssistant.showPossibleQuickAssists();
			//TODO: change to logger
			System.out.println(msg);
			return;
		}

		super.doOperation(operation);
	}

	/*
	 * @see IWidgetTokenOwner#requestWidgetToken(IWidgetTokenKeeper)
	 */
	public boolean requestWidgetToken(IWidgetTokenKeeper requester) {
		if (PlatformUI.getWorkbench().getHelpSystem().isContextHelpDisplayed())
			return false;
		return super.requestWidgetToken(requester);
	}

	/*
	 * @see IWidgetTokenOwnerExtension#requestWidgetToken(IWidgetTokenKeeper, int)
	 * @since 3.0
	 */
	public boolean requestWidgetToken(IWidgetTokenKeeper requester, int priority) {
		if (PlatformUI.getWorkbench().getHelpSystem().isContextHelpDisplayed())
			return false;
		return super.requestWidgetToken(requester, priority);
	}

	/*
	 * @see org.eclipse.jface.text.source.SourceViewer#createFormattingContext()
	 * @since 3.0
	 */
	public IFormattingContext createFormattingContext() {
		IFormattingContext context= new CommentFormattingContext();

		Map  preferences;
		IJavaElement inputJavaElement= fEditor.retrieveInputJavaElement();
		IJavaProject javaProject= inputJavaElement != null ? inputJavaElement.getJavaProject() : null;
		if (javaProject == null)
			preferences= new HashMap (JavaCore.getOptions());
		else
			preferences= new HashMap (javaProject.getOptions(true));

		context.setProperty(FormattingContextProperties.CONTEXT_PREFERENCES, preferences);

		return context;
	}



}
