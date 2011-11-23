package edu.washington.cs.rtrefactor;

import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin implements IStartup{

	// The plug-in ID
	public static final String PLUGIN_ID = "edu.washington.cs.rtrefactor"; //$NON-NLS-1$
	
	public static final String[] IMAGE_IDS = {"rtrefactor.fix.image1", 
		"rtrefactor.fix.image2", "rtrefactor.fix.image3",
		"rtrefactor.fix.image4", "rtrefactor.fix.image5", "rtrefactor.fix.image6",
		"rtrefactor.fix.image7", "rtrefactor.fix.image8"};
	// The shared instance
	private static Activator plugin;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/**
	 * {@inheritDoc}
	 */
	public void start(BundleContext context) throws Exception {
		//this method is called when the plugin is loaded on-demand
		
		super.start(context);
		plugin = this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void earlyStartup() {
		//this method is called when Eclipse starts
		//refer to the documentation for the limitations
	
		ICommandService service = (ICommandService) Activator.getDefault().getWorkbench().getService(ICommandService.class);
		
		service.addExecutionListener(new EclipseActionLogger());
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
	
	@Override
	 protected void initializeImageRegistry(ImageRegistry registry) {
         Bundle bundle = Platform.getBundle(PLUGIN_ID);
         for(int i=0; i<IMAGE_IDS.length; i++)
         {
        	 IPath path = new Path("icons/CloneFix"+(i+1)+".png");
        	 URL url = FileLocator.find(bundle, path, null);
        	 ImageDescriptor desc = ImageDescriptor.createFromURL(url);
        	 registry.put(IMAGE_IDS[i], desc);
         }
      }
}
