package edu.washington.cs.rtrefactor.eval.transform;

import java.awt.Color;

import edu.washington.cs.rtrefactor.eval.QuickColor;
import edu.washington.cs.rtrefactor.eval.QuickPicture;

public class CartoonifyImageTransform implements ImageTransform {
	
	public enum CartoonStyle  { ORIG, ENHANCED, EXPERIMENTAL}
	
	public static final int NUM_ITER = 2;
	
	public CartoonStyle myStyle;
	
	public CartoonifyImageTransform(CartoonStyle style) {
		myStyle = style;
	}
	
	@Override
	public QuickPicture transform(QuickPicture old) {
		if(!myStyle.equals(CartoonStyle.EXPERIMENTAL))
		{
			for(int i = 0; i<NUM_ITER; i++) {
				old = applyBlkDistortion(old);
			}
			return old;
		} else {
			return applyWiggleDistortion(old);
		}
		
	}
	
	public QuickPicture applyBlkDistortion(QuickPicture old) {
		int maxRun = 0;
		for (int r = 0 ; r < old.getHeight(); r++){
			for (int c = 0; c < old.getWidth(); c ++){
				QuickColor curColor = old.getColor(r, c);
				int horizRun = 0, vertRun = 0;
				while(old.getColor(horizRun, c).equalsRGB(curColor)) {
					horizRun++;
				}
				while(old.getColor(r, vertRun).equalsRGB(curColor)) {
					vertRun++;
				}
				int maxRunHere = Math.max(horizRun, vertRun);
				maxRun = (maxRunHere > maxRun) ? maxRunHere : maxRun;
				
			}
		}
			
		QuickPicture result = new QuickPicture(old.getWidth(), old.getHeight());
		int size = maxRun+1;
		for (int r = size ; r < old.getHeight() - size; r+= size * 2){
			
			for (int c = size; c < old.getWidth() - size; c += size* 2){
			
				QuickColor total = new QuickColor(0, 0, 0, 0);
				int samples = 0;
				for (int ro = -size; ro < size; ro++){
					for (int co = -size; co < size; co++){
						QuickColor oldColor = old.getColor(r+ro, c+co);
						total.setRed(total.getRed() + oldColor.getRed());
						total.setGreen(total.getGreen() + oldColor.getGreen());
						total.setBlue(total.getBlue() + oldColor.getBlue());
						samples++;
					}
				}
				
				for (int roff = -size; roff < size; roff++){
					for (int coff = -size; coff < size; coff++){
						if (r + roff >= 0 && r + roff < old.getHeight() && c + coff >=0 && c + coff < old.getWidth()){
							QuickColor newColor = new QuickColor(total.getRed()/samples, 
									total.getGreen()/samples, total.getBlue()/samples, 
									old.getColor(r+roff, c+coff).getAlpha());
							result.setColor(c+coff, r+roff, newColor);
						}
					}
				}
		
				
			}
			
		}
		
		return result;
	}
	
	public QuickPicture applyWiggleDistortion(QuickPicture old) {
		
		QuickPicture result = new QuickPicture(old.getWidth(), old.getHeight());
		
		for (int r = 0 ; r < old.getHeight(); r++){
			for (int c = 0; c < old.getWidth(); c ++){
				QuickColor curColor = old.getColor(r, c);
				
				QuickColor newColor = new QuickColor(0, 0, 0, curColor.getAlpha());
				if(curColor.getRed() > 0  && curColor.getGreen() < 0 && curColor.getBlue() > 0)
				{
					//horiz only
					QuickColor left = getSafeColor(old, r-1, c);
					QuickColor right = getSafeColor(old, r+1, c);
					newColor.setRed((left.getRed() + right.getRed())/2);
					newColor.setGreen((left.getGreen() + right.getGreen())/2);
					newColor.setRed((left.getBlue() + right.getBlue())/2);
				}
				else if(curColor.getRed() > 0  && curColor.getGreen() > 0 && curColor.getBlue() < 0)
				{
					//diag only
					QuickColor upperLeft = old.getColor(r-1, c-1);
					QuickColor upperRight = old.getColor(r+1, c-1);
					QuickColor lowerLeft = old.getColor(r-1, c+1);
					QuickColor lowerRight = old.getColor(r+1, c+1);
					newColor.setRed((upperLeft.getRed() + upperRight.getRed()
							+ lowerLeft.getRed() + lowerRight.getRed())/4);
					newColor.setGreen((upperLeft.getGreen() + upperRight.getGreen()
							+ lowerLeft.getGreen() + lowerRight.getGreen())/4);
					newColor.setBlue((upperLeft.getBlue() + upperRight.getBlue()
							+ lowerLeft.getBlue() + lowerRight.getBlue())/4);
				}
				else
				{
					//vert only
					QuickColor top = getSafeColor(old, r, c-1);
					QuickColor bottom = getSafeColor(old, r, c+1);
					newColor.setRed((top.getRed() + bottom.getRed())/2);
					newColor.setGreen((top.getGreen() + bottom.getGreen())/2);
					newColor.setRed((top.getBlue() + bottom.getBlue())/2);
					
				}
				
				
				
				newColor.setAlpha(curColor.getAlpha());
				result.setColor(r, c, newColor);
			}
		}
		return result;
	}
		
		public QuickColor getSafeColor(QuickPicture p, int r, int c) {
			QuickColor result = p.getColor(r, c);
			if(result == null)
				return new QuickColor(0,0,0,0);
			else
				return result;
		}
}
