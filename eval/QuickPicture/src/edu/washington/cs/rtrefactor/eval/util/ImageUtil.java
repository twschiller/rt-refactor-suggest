package edu.washington.cs.rtrefactor.eval.util;

import edu.washington.cs.rtrefactor.eval.QuickColor;
import edu.washington.cs.rtrefactor.eval.QuickPicture;
import edu.washington.cs.rtrefactor.eval.transform.ImageTransform;

public class ImageUtil {
	
	public static class Flip implements ImageTransform {
		public QuickPicture transform(QuickPicture orig) {
			QuickPicture res = new QuickPicture(orig.getWidth(), orig.getHeight());

			for (int r = 0; r < orig.getHeight(); r++){
				for (int c = 0; c < orig.getWidth(); c++){
					res.setColor(c, orig.getHeight() - r - 1, orig.getColor(c, r));
				}
			}
			return res;
		}
	}



	public static class ShrinkImage implements ImageTransform {
		
		int shrinkFactor;
		public ShrinkImage(int factor){
			shrinkFactor = (int)Math.pow(2, factor);
		}
		
		@Override
		public QuickPicture transform(QuickPicture old) {
			return ImageUtil.halfImage(old, shrinkFactor);
		}
		
	}
	public static QuickPicture halfImage(QuickPicture orig, int pow2) {

		QuickPicture res = new QuickPicture(orig.getWidth() / pow2, orig.getHeight() / pow2);

		int halfsize = pow2/2;
		for (int r = halfsize; r < orig.getHeight() - halfsize; r+= halfsize * 2){

			for (int c = halfsize; c < orig.getWidth() - halfsize; c += halfsize* 2){

				QuickColor total = new QuickColor(0, 0, 0, 0);
				int samples = 0;
				for (int ro = -halfsize; ro < halfsize; ro++){
					for (int co = -halfsize; co < halfsize; co++){
						QuickColor oldColor = orig.getColor(c+co, r+ro);
						total.setRed(total.getRed() + oldColor.getRed());
						total.setGreen(total.getGreen() + oldColor.getGreen());
						total.setBlue(total.getBlue() + oldColor.getBlue());
						samples++;
					}
				}

				QuickColor newColor = new QuickColor(total.getRed()/samples, 
						total.getGreen()/samples, total.getBlue()/samples, 
						orig.getColor(c, r).getAlpha());
				res.setColor(c/2, r/2, newColor);
			}
		}

		return res;
	}
	

}
