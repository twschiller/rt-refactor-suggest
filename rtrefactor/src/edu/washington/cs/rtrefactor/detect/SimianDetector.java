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
import edu.washington.cs.rtrefactor.scorer.Scorer;
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
	public String getName(){
		return NAME;
	}

	private SourceRegion mkRegion(BiMap<File, File> files, AuditEvent e, Matcher m, StringBuilder content){
		int endLine = Integer.parseInt(m.group(2).replace(",",""));
		
		File abs = super.absolutePath(super.absolutePath(new File(e.getFileName())));
		
		
		if (files.containsKey(abs)){
			File underlier = files.get(abs);
			
			Document underlierDoc;
			String source;
			try {
				source = FileUtil.read(abs);
				underlierDoc = new Document(source);
			} catch (IOException ex) {
				throw new RuntimeException("Problem reading file " + abs.getName() + " when parsing Simian message " + ex.getMessage());
			}
			
			SourceLocation start;
			try {
				start = new SourceLocation(underlier, e.getLine(), 0, underlierDoc);
			} catch (BadLocationException ex) {
				throw new RuntimeException("Bad location in file " + abs.getName() + " (Line: " + e.getLine() + ")", ex);
			}
			SourceLocation end;
			try {
				end = new SourceLocation(underlier, endLine, 0, underlierDoc);
			} catch (BadLocationException ex) {
				throw new RuntimeException("Bad location in file " + abs.getName() + " (Line: " + endLine  + ")", ex);
			}
			
			content.append(source.substring(start.getGlobalOffset(), end.getGlobalOffset()));
			
			return new SourceRegion(start, end);
		}else{
			throw new RuntimeException("Internal error: temporary source file " + abs.getAbsolutePath() + " is not registered");
		}
	}
	
	private SourceRegion mkOtherRegion(BiMap<File, File> files, AuditEvent e, Matcher m, StringBuilder content){
		File src = new File(m.group(3));
		int otherStart = Integer.parseInt(m.group(4).replace(",",""));
		int otherEnd = Integer.parseInt(m.group(5).replace(",",""));
		
		File abs = super.absolutePath(src);
				
		if (files.containsKey(abs)){
			File underlier = files.get(abs);
			
			Document underlierDoc;
			String source;
			try {
				source = FileUtil.read(abs);
				underlierDoc = new Document(source);
			} catch (IOException ex) {
				throw new RuntimeException("Problem reading file " + abs.getName() + " when parsing Simian message " + ex.getMessage());
			}
			
			SourceLocation start;
			try {
				start = new SourceLocation(underlier, otherStart, 0, underlierDoc);
			} catch (BadLocationException ex) {
				throw new RuntimeException("Bad location in file " + abs.getName() + " (Line: " + otherStart + ")", ex);
			}
			SourceLocation end;
			try {
				end = new SourceLocation(underlier, otherEnd , 0, underlierDoc);
			} catch (BadLocationException ex) {
				throw new RuntimeException("Bad location in file " + abs.getName() + " (Line: " + otherEnd + ")", ex);
			}
			
			content.append(source.substring(start.getGlobalOffset(), end.getGlobalOffset()));
			
			return new SourceRegion(start, end);		
		}else{
			throw new RuntimeException("Internal error: temporary source file " + abs.getAbsolutePath() + " is not registered");
		}
	}
	
	private double quality(StringBuilder source, StringBuilder other){
		int sourceLen = source.toString().replaceAll("\\S", "").length();
		int otherLen = source.toString().replaceAll("\\S", "").length();
		
		double avg = (sourceLen + otherLen) / 2.0;
	
		// after 150 non-whitespace chars, we don't care how long the clone section is
		return Scorer.scale(Math.min(avg, 150), 0, 100, 50, 100);
	}
	
	@Override
	/**
	 * {@inheritDoc}
	 */
	protected ClonePair makeClonePair(BiMap<File, File> files, AuditEvent e) {
		Matcher m = EN_REGEX.matcher(e.getMessage());
		
		StringBuilder sourceContent = new StringBuilder();
		StringBuilder otherContent = new StringBuilder();
		
		if (m.matches()){
			SourceRegion source = mkRegion(files, e, m, sourceContent);
			SourceRegion other = mkOtherRegion(files, e, m, otherContent);
			
			return new ClonePair(source, other , quality(sourceContent,otherContent));
		}else{
			throw new RuntimeException("Internal error parsing Simian message: " + e.getMessage());	
		}
	}
}
