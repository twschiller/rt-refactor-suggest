package edu.washington.cs.rtrefactor.eval.transform;

import edu.washington.cs.rtrefactor.eval.ImageTransform;
import edu.washington.cs.rtrefactor.eval.QuickColor;
import edu.washington.cs.rtrefactor.eval.QuickPicture;

/**
 * Applys some transformations to the image to give it more of a cinematic feel
 * 
 * @author Travis
 *
 */
public class CinematicImageTransform implements ImageTransform {

	@Override
	/**
	 * Transforms the image to make it more cinematic:
	 * 
	 * This is a two-phase process, with the first phase changing the focus,
	 * and the second phase swapping out colors to give it that old-timey video feel.
	 */
	public QuickPicture transform(QuickPicture old) {
		//odd, square same size kernels only
		double[]  kernel = {0.05, 0.1, 0.05,
							0.1, 0.4, 0.1,
							0.05, 0.1, 0.05};
		
		double[]  kernel2 = {0, 0.5, 0,
							0, -1, 0,
							0, 0.5, 0};
		
		QuickPicture result = new QuickPicture(old.getWidth(), old.getHeight());
		
		int len = (int)Math.sqrt(kernel2.length);
		int sideoff =  (len-1)/2;
		
		for (int x = 0 ; x < old.getHeight(); x++){
			for (int y = 0; y < old.getWidth() ; y ++){
				double totalColor=0;
				for (int xo = Math.max(x-sideoff, 0); xo <= Math.min(x+sideoff, old.getHeight()-1); xo++){
					for (int yo = Math.max(y-sideoff, 0); yo <= Math.min(y+sideoff, old.getHeight()-1); yo++){
						QuickColor color = old.getColor(xo, yo);
						int kernelr = xo -(x-sideoff);
						int kernelc = yo - (y-sideoff);
						double kFactor = kernel2[kernelr*len + kernelc];
						totalColor += kFactor *color.getRed();
						totalColor += kFactor *color.getGreen();
						totalColor += kFactor *color.getBlue();
					}
				}
				
				if(totalColor > 0){
					result.setColor(x,y, old.getColor(x, y));
				} else {
					result.setColor(x, y, new QuickColor(0,0,0, old.getColor(x, y).getAlpha()));
				}
					
		
			}
		}
		
		
		for (int x = 0 ; x < result.getHeight(); x++){
			for (int y = 0; y < result.getWidth() ; y ++){
				double totalRed =0, totalGreen=0, totalBlue=0;
				for (int xo = Math.max(x-sideoff, 0); xo <= Math.min(x+sideoff, result.getHeight()-1); xo++){
					for (int yo = Math.max(y-sideoff, 0); yo <= Math.min(y+sideoff, result.getHeight()-1); yo++){
						QuickColor color = result.getColor(xo, yo);
						int kernelr = xo -(x-sideoff);
						int kernelc = yo - (y-sideoff);
						double kFactor = kernel[kernelr*len + kernelc];
						totalRed += kFactor *color.getRed();
						totalGreen += kFactor *color.getGreen();
						totalBlue += kFactor *color.getBlue();
					}
				}
				result.setColor(x, y, new QuickColor((int)totalRed, (int)totalGreen, (int)totalBlue, 
						result.getColor(x, y).getAlpha()));
		
			}
		}
		return result;
	}

	
	
}
