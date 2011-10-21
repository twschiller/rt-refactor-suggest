package edu.washington.cs.rtrefactor.detect;

import org.eclipse.core.resources.ResourcesPlugin;

import com.harukizaemon.simian.SimianCheck;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;

/**
 * The Simian code clone detection tool. For more information see 
 * <a href="http://www.harukizaemon.com/simian/">http://www.harukizaemon.com/simian/</a>.
 * @author Todd Schiller
 */
public class SimianDetector extends BaseCheckStyleDetector<SimianCheck> {
	
	public static final String NAME = "Simian";
	public static final String DEFAULT_CONFIG_NAME = "default";
	
	public SimianDetector(){
		super(new SimianCheck(), ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile());
		
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
}
