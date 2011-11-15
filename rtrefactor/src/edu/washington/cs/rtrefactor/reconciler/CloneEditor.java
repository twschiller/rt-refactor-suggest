package edu.washington.cs.rtrefactor.reconciler;


import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jdt.internal.ui.text.ContentAssistPreference;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.text.IJavaPartitions;
import org.eclipse.jdt.ui.text.JavaSourceViewerConfiguration;
import org.eclipse.jdt.ui.text.JavaTextTools;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;


/** This editor is like a java editor, but extending with clone detection
 * functionality.
 * 
 * @author Travis Mandel
 */
public class CloneEditor extends CompilationUnitEditor {
	
	//These are copied for use in handlePreferenceStoreChanged
	/** Preference key for code formatter tab size */
	    private final static String CODE_FORMATTER_TAB_SIZE= DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE;
	     /** Preference key for inserting spaces rather than tabs */
	     private final static String SPACES_FOR_TABS= DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR;
	     /** Preference key for automatically closing strings */
	     private final static String CLOSE_STRINGS= PreferenceConstants.EDITOR_CLOSE_STRINGS;
	     /** Preference key for automatically closing brackets and parenthesis */
	     private final static String CLOSE_BRACKETS= PreferenceConstants.EDITOR_CLOSE_BRACKETS;
	     
	     //overriden, can;t access in handlePreferenceStoreChanged()
	   //  private BracketInserter fBracketInserter= new BracketInserter();

	

	public CloneEditor() {
		super();
	}
	
	/** 
	 * Returns the clone reconciler associated with this editor
	 * @return The current CloneReconciler
	 */
	public CloneReconciler getCloneReconciler()
	{
		return (CloneReconciler)((CloneSourceViewer)getSourceViewer()).getReconciler();
	}
	
	//must be accessible
	public ITypeRoot retrieveInputJavaElement() {
		return getInputJavaElement();
	}

	/** 
	 * We override this method to hook up the CloneViewerConfiguration,
	 *  and to install the reconciler on the viewer.
	 *  
	 * @return a new CloneViewerConfiguration
	 * @see org.eclipse.jdt.internal.ui.javaeditor.JavaEditor#createJavaSourceViewerConfiguration()
	 */
	protected JavaSourceViewerConfiguration createJavaSourceViewerConfiguration() {
		JavaTextTools textTools= JavaPlugin.getDefault().getJavaTextTools();

		CloneViewerConfiguration tc =  new CloneViewerConfiguration(textTools.getColorManager(), getPreferenceStore(), this, IJavaPartitions.JAVA_PARTITIONING);
		ISourceViewer sourceViewer = getSourceViewer();
		if(sourceViewer != null && sourceViewer instanceof CloneSourceViewer)
		{
			IReconciler reconciler= tc.getReconciler(sourceViewer);
			if (reconciler != null) {
				reconciler.install(sourceViewer);
				/* We know it is our own viewer because we override 
				 *  createJavaSourceViewer().
				 *  
				 *  A normal sourceViewer does not make setReconciler visible,
				 *  	but ours does.
				 */
				((CloneSourceViewer)sourceViewer).setReconciler(reconciler);
			}
		}

		return tc;
	}
	

	/** This is overidden to return our modified CloneSourceViewer */
	protected ISourceViewer createJavaSourceViewer(Composite parent, IVerticalRuler verticalRuler, IOverviewRuler overviewRuler, boolean isOverviewRulerVisible, int styles, IPreferenceStore store) {
		return new CloneSourceViewer(parent, verticalRuler, getOverviewRuler(), isOverviewRulerVisible(), styles, store, this);
	}
	
	
	// This contains an explicit cast, must override
	protected void handlePreferenceStoreChanged(PropertyChangeEvent event) {
		
		        try {
		
		            CloneSourceViewer asv= (CloneSourceViewer) getSourceViewer();
		            if (asv != null) {
		
		                String p= event.getProperty();
		                //way too messy to handle these preferences for now: private bracketinserter (instance of private class)
		              /*  if (CLOSE_BRACKETS.equals(p)) {
		                    fBracketInserter.setCloseBracketsEnabled(getPreferenceStore().getBoolean(p));
		                    return;
		                }
		
		                if (CLOSE_STRINGS.equals(p)) {
		                    fBracketInserter.setCloseStringsEnabled(getPreferenceStore().getBoolean(p));
		                    return;
		                }
		
		                if (JavaCore.COMPILER_SOURCE.equals(p)) {
		                    boolean closeAngularBrackets= JavaCore.VERSION_1_5.compareTo(getPreferenceStore().getString(p)) <= 0;
		                    fBracketInserter.setCloseAngularBracketsEnabled(closeAngularBrackets);
		                } */
		
		                if (SPACES_FOR_TABS.equals(p)) {
		                    if (isTabsToSpacesConversionEnabled())
		                        installTabsToSpacesConverter();
		                    else
		                        uninstallTabsToSpacesConverter();
		                    return;
		                }
		
		                if (PreferenceConstants.EDITOR_SMART_TAB.equals(p)) {
		                    if (getPreferenceStore().getBoolean(PreferenceConstants.EDITOR_SMART_TAB)) {
		                        setActionActivationCode("IndentOnTab", '\t', -1, SWT.NONE); //$NON-NLS-1$
		 } else {
		                        removeActionActivationCode("IndentOnTab"); //$NON-NLS-1$
		 }
		                }
		
		                IContentAssistant c= asv.getContentAssistant();
		                
		                if (c instanceof ContentAssistant)
		                    ContentAssistPreference.changeConfiguration((ContentAssistant) c, getPreferenceStore(), event);
		
		                if (CODE_FORMATTER_TAB_SIZE.equals(p) && isTabsToSpacesConversionEnabled()) {
		                    uninstallTabsToSpacesConverter();
		                    installTabsToSpacesConverter();
		                }
		            }
		
		        } finally {
		        	try{
		        		super.handlePreferenceStoreChanged(event);
		        	} catch (Exception e){
		        		System.out.println("moving on after error");
		        	}
		        }
		    }



}
