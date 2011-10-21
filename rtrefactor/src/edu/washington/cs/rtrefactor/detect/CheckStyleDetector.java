package edu.washington.cs.rtrefactor.detect;

import org.eclipse.core.resources.ResourcesPlugin;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.checks.duplicates.StrictDuplicateCodeCheck;

/**
 * The CheckStyle "strict" code clone detection tool. 
 * See <a href="http://checkstyle.sourceforge.net/config_duplicates.html">http://checkstyle.sourceforge.net/config_duplicates.html</a>
 * for more information.
 * @author Todd Schiller
 */
public class CheckStyleDetector extends BaseCheckStyleDetector<StrictDuplicateCodeCheck> {

	// TODO use configuration for minimum number of lines
	public static final int MINIMUM_LINES = 5;
	
	public static final String DEFAULT_CONFIG_NAME = "default";
	public static final String NAME = "CheckStyle";
	
	public CheckStyleDetector(){
		super(new StrictDuplicateCodeCheck(), ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile());
		getCheck().setMin(MINIMUM_LINES);
		
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
}
