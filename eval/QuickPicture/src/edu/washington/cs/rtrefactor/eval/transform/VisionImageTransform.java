package edu.washington.cs.rtrefactor.eval.transform;

import edu.washington.cs.rtrefactor.eval.ImageTransform;
import edu.washington.cs.rtrefactor.eval.QuickColor;
import edu.washington.cs.rtrefactor.eval.QuickPicture;

/**
 * Applies some computer-vision-esque transformations to the image.
 * 
 * @author Travis Mandel
 *
 */
public strictfp class VisionImageTransform implements ImageTransform {
	private int interestX;
	private int interestY;
	private int changeThreshold;
	
	/**
	 * Initializes the transformation
	 * @param interestX  The x-coordinate of the primary interest point
	 * @param interestY The y-coordinate of the primary interst point
	 * @param changeThreshold If per-pixel difference is less then this, it is OK to merge two areas 
	 * 					together
	 */
	public VisionImageTransform(int interestX, int interestY, int changeThreshold)
	{
		this.interestX = interestX;
		this.interestY = interestY;
		this.changeThreshold = changeThreshold;
	}

	@Override
	/**
	 * Applies two transformations to the image which are reminiscent of computer vision techniques:
	 * 
	 * The first is an edge detector, all the edges will be marked in the image
	 * 
	 * The second adds a rectangle indicating the region of interest (expanded from the provided \
	 * 	primary interest point)
	 * 
	 * All changes are made in color (0,0,0).
	 */
	public QuickPicture transform(QuickPicture old) {
		QuickPicture pic1 = detectEdges(old);
		QuickPicture pic2 = addRectangle(old);


		for (int r = 0 ; r < old.getHeight(); r++){
			for (int c = 0; c < old.getWidth() ; c ++){
				QuickColor p2Color= pic2.getColor(c, r);
				if(p2Color.equalsRGB(new QuickColor(0,0,0,0)))
				{
					pic1.setColor(c, r, p2Color);
				}

			}
		}



		return pic1;
	}

	public QuickPicture detectEdges(QuickPicture old) {
		int[][] totals = new int[old.getWidth()][old.getHeight()];
		int totalColor = 0;
		int maxColor = 0;
		for (int x = 0 ; x < old.getWidth(); x++){
			for (int y = 0; y < old.getHeight() ; y ++){
				QuickColor current = old.getColor(x, y);
				QuickColor left = old.getColor(x-1, y);
				if(left == null)
					left = new QuickColor(0,0,0,0);
				QuickColor right = old.getColor(x+1, y);
				if(right == null)
					right = new QuickColor(0,0,0,0);
				QuickColor newColor =new QuickColor(left.getRed() - right.getRed(),
						left.getGreen() - right.getGreen(), left.getBlue() - right.getBlue(), 
						current.getAlpha()); 
				int colorSum = newColor.getRed() + newColor.getGreen() + newColor.getBlue();
				totals[x][y] = colorSum;
				totalColor += colorSum;
				maxColor = (colorSum > maxColor) ? colorSum : maxColor;


			}
		}

		double avg = totalColor/(double)(old.getWidth() * old.getHeight());
		int threshold = (int)((avg + maxColor) / 2);

		QuickPicture result = new QuickPicture(old.getWidth(), old.getHeight());
		for (int r = 0 ; r < old.getHeight(); r++){
			for (int c = 0; c < old.getWidth() ; c ++){
				QuickColor current = old.getColor(r, c);
				if(totals[c][r]  < threshold)
					result.setColor(r, c, current);
				else
					result.setColor(r, c, new QuickColor(0, 0, 0, current.getAlpha()));
			}
		}

		return result;
	}

	public QuickPicture addRectangle(QuickPicture orig) {
		int leftFrontier = interestX, rightFrontier=interestX;
		int topFrontier = interestY, bottomFrontier = interestY;
		int numPixels = 1;
		QuickColor totalRegion = orig.getColor(interestY, interestX); 
		boolean changed = true;
		while(changed)
		{
			changed = false;

			int regionHeight = (bottomFrontier-topFrontier+1);
			int regionWidth = (bottomFrontier-topFrontier+1);

			QuickColor averageRegion = divideBy(totalRegion, regionWidth * regionHeight); 

			QuickColor totalLeft = new QuickColor(0,0,0,0);
			QuickColor totalRight = new QuickColor(0,0,0,0);
			//expand + add to average
			for(int r=topFrontier; r<=bottomFrontier; r++)
			{
				if(leftFrontier-1 >= 0)
				{	
					QuickColor leftColor = orig.getColor(r, leftFrontier-1);
					totalLeft.setRed(totalLeft.getRed() + leftColor.getRed());
					totalLeft.setGreen(totalLeft.getGreen() + leftColor.getGreen());
					totalLeft.setBlue(totalLeft.getGreen() + leftColor.getBlue());
				}

				if(rightFrontier+1 < orig.getWidth())
				{	
					QuickColor rightColor = orig.getColor(r, rightFrontier+1);
					totalRight.setRed(totalRight.getRed() + rightColor.getRed());
					totalRight.setGreen(totalRight.getGreen() + rightColor.getGreen());
					totalRight.setBlue(totalRight.getGreen() + rightColor.getBlue());
				}
			}


			if(leftFrontier-1 >= 0 &&
					differenceSum(averageRegion, divideBy(totalLeft, regionHeight)) <= changeThreshold) {
				leftFrontier--;
				changed = true;
			}

			if(rightFrontier+1 < orig.getWidth() && 
					differenceSum(averageRegion, divideBy(totalRight, regionHeight)) <= changeThreshold) {
				rightFrontier++;
				changed = true;
			}


			QuickColor totalTop = new QuickColor(0,0,0,0);
			QuickColor totalBottom = new QuickColor(0,0,0,0);
			//expand + add to average
			for(int c=leftFrontier; c<=rightFrontier; c++)
			{
				if(topFrontier-1 >= 0)
				{
					QuickColor topColor = orig.getColor(topFrontier-1, c);
					totalTop.setRed(totalTop.getRed() + topColor.getRed());
					totalTop.setGreen(totalTop.getGreen() + topColor.getGreen());
					totalTop.setBlue(totalTop.getGreen() + topColor.getBlue());
				}

				if(bottomFrontier+1 < orig.getHeight())
				{
					QuickColor bottomColor = orig.getColor(bottomFrontier+1, c);
					totalBottom.setRed(totalBottom.getRed() + bottomColor.getRed());
					totalBottom.setGreen(totalBottom.getGreen() + bottomColor.getGreen());
					totalBottom.setBlue(totalBottom.getGreen() + bottomColor.getBlue());
				}
			}

			if((topFrontier-1 >= 0) &&
					differenceSum(averageRegion, divideBy(totalTop, regionWidth)) <= changeThreshold) {
				topFrontier--;
				changed = true;
			}

			if(bottomFrontier+1 < orig.getHeight() &&
					differenceSum(averageRegion, divideBy(totalBottom, regionWidth)) <= changeThreshold) {
				bottomFrontier++;
				changed = true;
			}

			totalRegion = new QuickColor(0, 0, 0,0);
			for(int r=topFrontier; r<=bottomFrontier; r++)
			{
				for(int c=leftFrontier; c<=rightFrontier; c++)
				{
					QuickColor color = orig.getColor(r, c);
					totalRegion.setRed(totalRegion.getRed() + color.getRed());
					totalRegion.setGreen(totalRegion.getGreen() + color.getGreen());
					totalRegion.setBlue(totalRegion.getGreen() + color.getBlue());
				}
			}
		}


		QuickPicture result = new QuickPicture(orig.getWidth(), orig.getHeight());
		for (int r = 0 ; r < orig.getHeight(); r++){
			for (int c = 0; c < orig.getWidth() ; c ++){
				result.setColor(r, c, orig.getColor(r, c));
			}
		}
		
		//mark border

		for(int r=topFrontier; r<=bottomFrontier; r++)
		{
			result.setColor(r, leftFrontier, new QuickColor(0,0,0,
					orig.getColor(r, leftFrontier).getAlpha()));

			result.setColor(r, rightFrontier, new QuickColor(0,0,0,
					orig.getColor(r,  rightFrontier).getAlpha()));

		}
		
		for(int r=leftFrontier; r<=rightFrontier; r++)
		{
			result.setColor(topFrontier, r, new QuickColor(0,0,0,
					orig.getColor(topFrontier, r).getAlpha()));

			result.setColor(bottomFrontier, r, new QuickColor(0,0,0,
					orig.getColor(bottomFrontier, r).getAlpha()));

		}

		return result;
	}

	public int differenceSum(QuickColor origC, QuickColor newC)
	{
		int totalDiff = 0;
		totalDiff += Math.abs(newC.getRed() - origC.getRed());
		totalDiff += Math.abs(newC.getGreen() - origC.getGreen());
		totalDiff += Math.abs(newC.getBlue() - origC.getBlue());
		return totalDiff;
	}



	public QuickColor divideBy(QuickColor origC, int divisor)
	{
		QuickColor result = new QuickColor(origC.getBlue()/divisor, 
				origC.getGreen()/divisor,
				origC.getRed()/divisor, 0);

		return result;
	}

}
