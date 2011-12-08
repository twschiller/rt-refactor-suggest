package edu.washington.cs.rtrefactor.eval.transform;

import edu.washington.cs.rtrefactor.eval.QuickColor;
import edu.washington.cs.rtrefactor.eval.QuickPicture;

public class UnderwaterTransform implements ImageTransform {
	
	private boolean setSize;
	public UnderwaterTransform(boolean setSize) {
		this.setSize = setSize;
	}
	public QuickPicture transform(QuickPicture old) {
		
		
		QuickPicture blue = new QuickPicture(old.getWidth(), old.getHeight());
		for (int r = 0; r < old.getHeight(); r++){
			for (int c = 0; c < old.getWidth(); c++){
				QuickColor oldColor =old.getColor(c, r);
				oldColor.setRed(-Math.abs(oldColor.getRed()));
				oldColor.setGreen(-Math.abs(oldColor.getGreen()));
				oldColor.setBlue(Math.abs(oldColor.getBlue()) * 2);
				blue.setColor(c, r, oldColor);
			}
		}
		
		if(!setSize)
			return blue;
		
		
		int pow2 = 2;
		QuickPicture res = new QuickPicture(blue.getWidth() / pow2, blue.getHeight() / pow2);

		int halfsize = pow2/2;
		for (int r = halfsize; r < blue.getHeight() - halfsize; r+= halfsize * 2){

			for (int c = halfsize; c < blue.getWidth() - halfsize; c += halfsize* 2){

				QuickColor total = new QuickColor(0, 0, 0, 0);
				int samples = 0;
				for (int ro = -halfsize; ro < halfsize; ro++){
					for (int co = -halfsize; co < halfsize; co++){
						QuickColor oldColor = blue.getColor(c+co, r+ro);
						total.setRed(total.getRed() + oldColor.getRed());
						total.setGreen(total.getGreen() + oldColor.getGreen());
						total.setBlue(total.getBlue() + oldColor.getBlue());
						samples++;
					}
				}

				QuickColor newColor = new QuickColor(total.getRed()/samples, 
						total.getGreen()/samples, total.getBlue()/samples, 
						blue.getColor(c, r).getAlpha());
				res.setColor(c/2, r/2, newColor);
			}
		}

		return res;
	}
}
