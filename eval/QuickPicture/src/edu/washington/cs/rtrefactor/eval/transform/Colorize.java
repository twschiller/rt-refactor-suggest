package edu.washington.cs.rtrefactor.eval.transform;

import java.awt.Color;

import edu.washington.cs.rtrefactor.eval.QuickPicture;

public class Colorize {

	public static class Zebra implements ImageTransform{
		@Override
		public QuickPicture transform(QuickPicture old) {

			QuickPicture result = new QuickPicture(old.getWidth(),old.getHeight());
			
			for (int y = 0 ; y < old.getHeight(); y ++){
				for (int x = 0 ; x < old.getWidth(); x ++){
					result.setRed(x, y,old.getRed(x, y));
					result.setGreen(x, y, old.getGreen(x, y));
					result.setBlue(x, y, old.getBlue(x, y));
					result.setAlpha(x, y, old.getAlpha(x, y));
					
				}
			}
			
			
			for (int r = 0 ; r < old.getHeight(); r++){
				
				for (int c = 0; c < old.getWidth(); c++){
				
					if (r % 50 < 20){
						result.setRed(c, r, Color.BLACK.getRed());
						result.setGreen(c, r, Color.BLACK.getGreen());
						result.setBlue(c, r, Color.BLACK.getBlue());
						result.setAlpha(c, r, Color.BLACK.getAlpha());
					}	
				}
			}
			return result;
		}
	}


	
	
}
