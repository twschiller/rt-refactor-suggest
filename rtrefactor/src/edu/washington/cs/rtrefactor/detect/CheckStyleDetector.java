package edu.washington.cs.rtrefactor.detect;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;

import com.google.common.collect.BiMap;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.checks.duplicates.StrictDuplicateCodeCheck;

import edu.washington.cs.rtrefactor.preferences.PreferenceUtil.Preference;
import edu.washington.cs.rtrefactor.util.FileUtil;

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
	public String getName() {
		return NAME;
	}
	
	private static int numLines(AuditEvent e, Matcher m){
		return Integer.parseInt(m.group(1).replace(",",""));
	}
	
	private SourceRegion mkRegion(BiMap<File, File> files, AuditEvent e, Matcher m) throws BadLocationException, IOException{
		File surface= super.absolutePath(new File(e.getFileName()));
		File underlier = files.get(surface);
		
		Document underlierDoc = new Document(FileUtil.read(surface));
		return new SourceRegion(
				new SourceLocation(underlier, e.getLine(), 0, underlierDoc),
				new SourceLocation(underlier, e.getLine() + numLines(e,m), 0, underlierDoc));
	}
	
	private SourceRegion mkOtherRegion(BiMap<File, File> files, AuditEvent e, Matcher m) throws BadLocationException, IOException{
		int numLines = Integer.parseInt(m.group(1).replace(",",""));
		File src = new File(m.group(2));
		int otherLine = Integer.parseInt(m.group(3).replace(",",""));
		
		File surface= super.absolutePath(src);
		File underlier = files.get(surface);
		Document underlierDoc = new Document(FileUtil.read(surface));
		
		return new SourceRegion(
				new SourceLocation(underlier, otherLine, 0, underlierDoc),
				new SourceLocation(underlier, otherLine + numLines, 0, underlierDoc));
	}

	
	@Override
	protected ClonePair makeClonePair(BiMap<File, File> files, AuditEvent e) {
		Matcher m = EN_REGEX.matcher(e.getMessage());
		
		if (m.matches()){
			try {
				return new ClonePair(mkRegion(files, e, m), mkOtherRegion(files, e, m), numLines(e,m));
			} catch (Exception ex) {
				throw new RuntimeException("Bad location parsed from CheckStyle message. Message: " + e.getMessage());
			}
		}else{
			throw new RuntimeException("Internal error parsing CheckStyle message: " + e.getMessage());	
		}
	}

}
