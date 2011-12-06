package edu.washington.cs.rtrefactor.eval.transform;

import edu.washington.cs.rtrefactor.eval.QuickPicture;

public class Blocky implements ImageTransform{

	@Override
	public QuickPicture transform(QuickPicture old) {
		int size = 6;
		
		QuickPicture result = new QuickPicture(old.getWidth(), old.getHeight());
		
	
		
		for (int r = size ; r < old.getHeight() - size; r+= size * 2){
			
			for (int c = size; c < old.getWidth() - size; c += size* 2){
			
				for (int ro = -size; ro < size; ro++){
					for (int co = -size; co < size; co++){
						
						if (r + ro >= 0 && r + ro < old.getHeight() && c + co >=0 && c + co < old.getWidth()){
							result.setRed(c + co, r + ro,old.getRed(c, r));
							result.setGreen(c + co, r + ro, old.getGreen(c, r));
							result.setBlue(c + co, r + ro, old.getBlue(c, r));
							result.setAlpha(c + co, r + ro, old.getAlpha(c, r));
						}
					}
				}
		
				
			}
			
		}
		
		return result;
	}

}
