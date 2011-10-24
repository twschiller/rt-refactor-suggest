package edu.washington.cs.rtrefactor.detect;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.ResourcesPlugin;

import com.harukizaemon.simian.SimianCheck;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;

import edu.washington.cs.rtrefactor.preferences.PreferenceUtil.Preference;

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

	private SourceRegion mkRegion(AuditEvent e, Matcher m){
		int endLine = Integer.parseInt(m.group(2).replace(",",""));
		
		return new SourceRegion(
				new SourceLocation(new File(e.getFileName()), e.getLine(), 0),
				new SourceLocation(new File(e.getFileName()), endLine + 1, 0));
	}
	
	private SourceRegion mkOtherRegion(AuditEvent e, Matcher m){
		File src = new File(m.group(3));
		int otherStart = Integer.parseInt(m.group(4).replace(",",""));
		int otherEnd = Integer.parseInt(m.group(5).replace(",",""));
		
		return new SourceRegion(
				new SourceLocation(src, otherStart, 0),
				new SourceLocation(src, otherEnd + 1, 0));
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
	protected ClonePair makeClonePair(AuditEvent e) {
		Matcher m = EN_REGEX.matcher(e.getMessage());
		
		if (m.matches()){
			return new ClonePair(mkRegion(e,m), mkOtherRegion(e,m), quality(e,m));
		}else{
			throw new RuntimeException("Internal error parsing Simian message: " + e.getMessage());	
		}
	}
}
