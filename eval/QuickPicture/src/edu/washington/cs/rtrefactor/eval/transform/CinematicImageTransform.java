package edu.washington.cs.rtrefactor.eval.transform;

import edu.washington.cs.rtrefactor.eval.QuickColor;
import edu.washington.cs.rtrefactor.eval.QuickPicture;

public class CinematicImageTransform implements ImageTransform {

	@Override
	public QuickPicture transform(QuickPicture old) {
		
		double[]  kernel = {0.3, 0.5, 0.3,
							0.5, 1.0, 0.5,
							0.3, 0.5, 0.3};
		
		double[]  kernel2 = {0, 0.3, 0,
							0, 0.5, 0,
							0, 1.0, 0,
							0, 0.5, 0};
		
		QuickPicture result = new QuickPicture(old.getWidth(), old.getHeight());
		
		int len = (int)Math.sqrt(kernel2.length);
		
		for (int r = 0 ; r < old.getHeight(); r++){
			for (int c = 0; c < old.getWidth() ; c ++){
				double totalColor=0;
				for (int ro = Math.max(r-len, 0); ro < Math.min(r+len, old.getHeight()); ro++){
					for (int co = Math.max(c-len, 0); co < Math.min(c+len, old.getHeight()); co++){
						QuickColor color = old.getColor(ro, co);
						int kernelr = ro -(r-len);
						int kernelc = co - (c-len);
						System.out.println(kernelr + " " + kernelc);
						double kFactor = kernel2[kernelr*len + kernelc];
						totalColor += kFactor *color.getRed();
						totalColor += kFactor *color.getGreen();
						totalColor += kFactor *color.getBlue();
					}
				}
				
				if(totalColor > 0){
					result.setColor(r,c, old.getColor(r, c));
				} else {
					result.setColor(r, c, new QuickColor(0,0,0, old.getColor(r, c).getAlpha()));
				}
					
		
			}
		}
		
		
		for (int r = 0 ; r < result.getHeight(); r++){
			for (int c = 0; c < result.getWidth() ; c ++){
				double totalRed =0, totalGreen=0, totalBlue=0;
				for (int ro = Math.max(r-len, 0); ro < Math.min(r+len, result.getHeight()); ro++){
					for (int co = Math.max(c-len, 0); co < Math.min(c+len, result.getHeight()); co++){
						QuickColor color = result.getColor(ro, co);
						int kernelr = ro -(r-len);
						int kernelc = co - (c-len);
						double kFactor = kernel[kernelr*len + kernelc];
						totalRed += kFactor *color.getRed();
						totalGreen += kFactor *color.getGreen();
						totalBlue += kFactor *color.getBlue();
					}
				}
				result.setColor(r, c, new QuickColor((int)totalRed, (int)totalGreen, (int)totalBlue, 
						result.getColor(r, c).getAlpha()));
		
			}
		}
		return result;
	}

	
	
}
