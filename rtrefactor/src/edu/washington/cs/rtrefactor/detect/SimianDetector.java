package edu.washington.cs.rtrefactor.detect;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;

import com.google.common.collect.BiMap;
import com.harukizaemon.simian.SimianCheck;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;

import edu.washington.cs.rtrefactor.preferences.PreferenceUtil.Preference;
import edu.washington.cs.rtrefactor.util.FileUtil;

/**
 * The Simian code clone detection tool. For more information see 
 * <a href="http://www.harukizaemon.com/simian/">http://www.harukizaemon.com/simian/</a>.
 * @author Todd Schiller
 */
public class SimianDetector extends BaseCheckStyleDetector<SimianCheck> {
	
	public static final String NAME = "Simian";
	public static final String DEFAULT_CONFIG_NAME = "default";
	
	private static final Pattern EN_REGEX = Pattern.compile("Found ([\\d,]+) lines ending at line ([\\d,]+) duplicated in (.*?) between lines ([\\d,]+) and ([\\d,]+).");
	
	public static final Preference<?> PREFERENCES[] = new Preference[]{
		new Preference<Integer>(NAME, "Threshold", "Minimum number of lines in a match", 6),
		new Preference<Boolean>(NAME, "IgnoreVariableNames", "Ignore name differences between variables", true),
		new Preference<Boolean>(NAME, "IgnoreCurlyBraces", "Ignore curly braces", false),
		new Preference<Boolean>(NAME, "IgnoreIdentifiers", "Completely ignore all identfiers", false),
		new Preference<Boolean>(NAME, "IgnoreIdentifierCase", "Match identifiers irrespective of case", false),
		new Preference<Boolean>(NAME, "IgnoreStringCase", "Match string literals irrespective of case", false),
		new Preference<Boolean>(NAME, "IgnoreStrings", "Ignore differences in strings", false),
		new Preference<Boolean>(NAME, "IgnoreNumbers", "Ignore differences in numbers", false),
		new Preference<Boolean>(NAME, "IgnoreCharacters", "Ignore differences in characters", false),
		new Preference<Boolean>(NAME, "IgnoreCharacterCase", "Ignore differences in character case", false),
		new Preference<Boolean>(NAME, "IgnoreLiterals", "Ignore differences in literals", false),
		new Preference<Boolean>(NAME, "IgnoreSubtypeNames", "Ignore name differences between subclasses", false),
		new Preference<Boolean>(NAME, "IgnoreModifiers", "Ignore modifiers", false),
		new Preference<Boolean>(NAME, "BalanceParentheses", "Consider expression inside parenthesis split across lines as one", true),
		new Preference<Boolean>(NAME, "BalanceSquareBrackets", "Consider expression inside square brackets split across lines as one", false),
	};
	
	public SimianDetector(){
		super(new SimianCheck(), ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile());

		for (Preference<?> x : PREFERENCES){
			setPreference(x);
		}
		
		getCheck().setLanguage("java");
		
		try{
			getCheck().configure(new DefaultConfiguration(DEFAULT_CONFIG_NAME));
		}catch(CheckstyleException ex){
			throw new RuntimeException("Error configuring Simian", ex);
		}
	}	
	
	@Override
	/**
	 * {@inheritDoc}
	 */
	public String getName(){
		return NAME;
	}

	private SourceRegion mkRegion(BiMap<File, File> files, AuditEvent e, Matcher m) throws BadLocationException, IOException{
		int endLine = Integer.parseInt(m.group(2).replace(",",""));
		
		File abs = super.absolutePath(super.absolutePath(new File(e.getFileName())));
		
		
		if (files.containsKey(abs)){
			File underlier = files.get(abs);
			
			Document underlierDoc = new Document(FileUtil.read(abs));
			
			return new SourceRegion(
				new SourceLocation(underlier, e.getLine(), 0, underlierDoc),
				new SourceLocation(underlier, endLine + 1, 0, underlierDoc));
		}else{
			throw new RuntimeException("internal error: temporary source file " + abs.getAbsolutePath() + " is not registered");
		}
	}
	
	private SourceRegion mkOtherRegion(BiMap<File, File> files, AuditEvent e, Matcher m) throws BadLocationException, IOException{
		File src = new File(m.group(3));
		int otherStart = Integer.parseInt(m.group(4).replace(",",""));
		int otherEnd = Integer.parseInt(m.group(5).replace(",",""));
		
		File abs = super.absolutePath(src);
				
		if (files.containsKey(abs)){
			File underlier = files.get(abs);
			
			Document underlierDoc = new Document(FileUtil.read(abs));
			
			return new SourceRegion(
					new SourceLocation(underlier, otherStart, 0, underlierDoc),
					new SourceLocation(underlier, otherEnd + 1, 0, underlierDoc));			
		}else{
			throw new RuntimeException("internal error: temporary source file " + abs.getAbsolutePath() + " is not registered");
		}
	}
	
	private double quality(AuditEvent e, Matcher m){
		int numLines = Integer.parseInt(m.group(1).replace(",",""));
		int otherStart = Integer.parseInt(m.group(4).replace(",",""));
		int otherEnd = Integer.parseInt(m.group(5).replace(",",""));
		
		return (numLines + (otherEnd - otherStart + 1)) / 2.0;
	}
	
	@Override
	/**
	 * {@inheritDoc}
	 */
	protected ClonePair makeClonePair(BiMap<File, File> files, AuditEvent e) {
		Matcher m = EN_REGEX.matcher(e.getMessage());
		
		if (m.matches()){
			try {
			return new ClonePair(mkRegion(files, e, m), mkOtherRegion(files, e, m), quality(e,m));
			} catch (BadLocationException ex) {
				throw new RuntimeException("Bad location in parsing Simian message " + ex.getMessage());
			} catch (IOException ex) {
				throw new RuntimeException("Problem reading file when parsing Simian message " + ex.getMessage());
			}
		}else{
			throw new RuntimeException("Internal error parsing Simian message: " + e.getMessage());	
		}
	}
}
