package edu.washington.cs.rtrefactor.eval.transform;

import edu.washington.cs.rtrefactor.eval.QuickPicture;

public class Blur implements ImageTransform {

	@Override
	public QuickPicture transform(QuickPicture old) {
		int size = 4;
		
		QuickPicture result = new QuickPicture(old.getWidth(), old.getHeight());
		
		for (int r = 0 ; r < old.getHeight(); r++){
			
			for (int c = 0; c < old.getWidth(); c++){
			
				double sumr = 0.0;
				double sumg = 0.0;
				double sumb = 0.0;
				int cnt = 0;
				
				for (int ro = -size; ro < size; ro++){
					for (int co = -size; co < size; co++){
						
						if (r + ro >= 0 && r + ro < old.getHeight() && c + co >=0 && c + co < old.getWidth()){
							sumr += old.getRed(c + co, r + ro);
							sumg += old.getGreen(c + co, r + ro);
							sumb += old.getBlue(c + co, r + ro);
						
							cnt++;
						}
					}
				}
			
				setColor(result, c, r, (int) (sumr/cnt), (int) (sumg/cnt), (int) (sumb/cnt));
				
			}
			
		}
		
		return result;
	}
	
	private void setColor(QuickPicture p, int x, int y, int r, int g, int b){
		p.setRed(x, y, r);
		p.setGreen(x, y, g);
		p.setBlue(x, y, b);
	}


}
