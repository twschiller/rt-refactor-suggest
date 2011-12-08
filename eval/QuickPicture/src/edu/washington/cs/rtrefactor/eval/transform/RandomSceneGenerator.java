package edu.washington.cs.rtrefactor.eval.transform;

import edu.washington.cs.rtrefactor.eval.QuickColor;
import edu.washington.cs.rtrefactor.eval.QuickPicture;

public class RandomSceneGenerator implements ImageTransform {
	
	public static final int MAX_STEPS = 500;
	
	private QuickColor lineColor;
	
	private int startRow;
	private int startColumn;
	private int maxRed;

	public RandomSceneGenerator(QuickColor qc, int startRow, int startColumn, int maxRed) {
		lineColor = qc;
		this.startRow = startRow;
		this.startColumn = startColumn;
		this.maxRed = maxRed;
		
	}
	@Override
	public QuickPicture transform(QuickPicture old) {
		QuickPicture newPic = old;
		for(int i=0; i< 20; i++)
			newPic = transformHelper(newPic);
		return newPic;
	}
	public QuickPicture transformHelper(QuickPicture old) {
		int currentRow = startRow;
		int currentColumn = startColumn;
		int totalRed = 0;
		
		QuickPicture result = new QuickPicture(old.getWidth(), old.getHeight());
		for(int r=0; r<old.getHeight(); r++)
			for(int c=0; c<old.getWidth(); c++)
				result.setColor(c, r, old.getColor(c, r));

		int steps = 0;
		while(totalRed < maxRed & steps < MAX_STEPS) 
		{
			steps++;
			QuickColor currentColor =  old.getColor(currentColumn, currentRow);
			totalRed += currentColor.getRed();
			
			if(currentColor.getGreen() < currentColor.getBlue()  ) {
				currentRow--;
				if(currentRow < 0)
					currentRow =old.getHeight()-1;
			} else if (currentColor.getBlue() >= currentColor.getRed()) {
				currentRow++;
				if(currentRow >= old.getHeight())
					currentRow =0;
			}else if (currentColor.getRed()<0){
				currentColumn++;
				if(currentColumn >= old.getWidth())
					currentColumn =0;
			}else {
				currentColumn--;
				if(currentColumn < 0)
					currentColumn = old.getWidth()-1;
			}
			
			//hack
			lineColor.setAlpha(currentColor.getAlpha());
			
			result.setColor(currentColumn, currentRow, lineColor);
		}
		System.out.println(totalRed + " " + steps);
		
		return result;
	}
	
}
