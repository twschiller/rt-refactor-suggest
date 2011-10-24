package edu.washington.cs.rtrefactor.detect;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.ResourcesPlugin;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.checks.duplicates.StrictDuplicateCodeCheck;

import edu.washington.cs.rtrefactor.preferences.PreferenceUtil.Preference;

/**
 * The CheckStyle "strict" code clone detection tool. 
 * See <a href="http://checkstyle.sourceforge.net/config_duplicates.html">http://checkstyle.sourceforge.net/config_duplicates.html</a>
 * for more information.
 * @author Todd Schiller
 */
public class CheckStyleDetector extends BaseCheckStyleDetector<StrictDuplicateCodeCheck> {
	
	public static final String DEFAULT_CONFIG_NAME = "default";
	public static final String NAME = "CheckStyle";
	
	private static final Pattern EN_REGEX = Pattern.compile("Found duplicate of ([\\d,]+) lines in (.*?), starting from line ([\\d,]+)");
	
	public static final Preference<?> PREFERENCES[] = new Preference[]{
		new Preference<Integer>(NAME, "Min", "Minimum # of lines to match", 5)
	};
		
	public CheckStyleDetector(){
		super(new StrictDuplicateCodeCheck(), ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile());
		
		for (Preference<?> x : PREFERENCES){
			setPreference(x);
		}
		
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
	public String getName() {
		return NAME;
	}
	
	private static int numLines(AuditEvent e, Matcher m){
		return Integer.parseInt(m.group(1).replace(",",""));
	}
	
	private SourceRegion mkRegion(AuditEvent e, Matcher m){
		return new SourceRegion(
				new SourceLocation(new File(e.getFileName()), e.getLine(), 0),
				new SourceLocation(new File(e.getFileName()), e.getLine() + numLines(e,m), 0));
	}
	
	private SourceRegion mkOtherRegion(AuditEvent e, Matcher m){
		int numLines = Integer.parseInt(m.group(1).replace(",",""));
		File src = new File(m.group(2));
		int otherLine = Integer.parseInt(m.group(3).replace(",",""));
		
		return new SourceRegion(
				new SourceLocation(src, otherLine, 0),
				new SourceLocation(src, otherLine + numLines, 0));
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	protected ClonePair makeClonePair(AuditEvent e) {
		Matcher m = EN_REGEX.matcher(e.getMessage());
		
		if (m.matches()){
			return new ClonePair(mkRegion(e,m), mkOtherRegion(e,m), numLines(e,m));
		}else{
			throw new RuntimeException("Internal error parsing CheckStyle message: " + e.getMessage());	
		}
	}
}
