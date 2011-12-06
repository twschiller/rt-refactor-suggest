package edu.washington.cs.rtrefactor.eval.transform;

import edu.washington.cs.rtrefactor.eval.QuickPicture;

/**
 * Functions for modifying an images Alpha channel
 * @author Todd Schiller
 */
public class Alpha{

	
	public static class Ghost50 implements ImageTransform{
		@Override
		public QuickPicture transform(QuickPicture old) {
			QuickPicture result = dupe(old);
			
			for (int r = 0 ; r < old.getHeight(); r++){
				for (int c = 0; c < old.getWidth(); c++){
					result.setAlpha(c, r, (int) (result.getAlpha(c, r) * .5));
				}
			}
			
			return result;
		}
	}
	
	public static class Ghost25 implements ImageTransform{
		@Override
		public QuickPicture transform(QuickPicture old) {
			QuickPicture result = dupe(old);
			
			for (int r = 0 ; r < old.getHeight(); r++){
				for (int c = 0; c < old.getWidth(); c++){
					result.setAlpha(c, r, (int) (result.getAlpha(c, r) * .25));
				}
			}
			return result;
		}
	}
	
	public static QuickPicture dupe(QuickPicture i){
		QuickPicture r = new QuickPicture(i.getWidth(), i.getHeight());
		
		for (int y = 0 ; y < i.getHeight(); y ++){
			for (int x = 0 ; x < i.getWidth(); x ++){
				r.setRed(x, y, i.getRed(x, y));
				r.setGreen(x, y, i.getGreen(x, y));
				r.setBlue(x, y, i.getBlue(x, y));
				r.setAlpha(x, y, i.getAlpha(x, y));
				
			}
		}
		
		return r;
	}
	
	
}
