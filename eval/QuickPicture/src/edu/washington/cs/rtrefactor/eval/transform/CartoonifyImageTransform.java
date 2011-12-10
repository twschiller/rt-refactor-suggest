package edu.washington.cs.rtrefactor.eval.transform;

import edu.washington.cs.rtrefactor.eval.ImageTransform;
import edu.washington.cs.rtrefactor.eval.QuickColor;
import edu.washington.cs.rtrefactor.eval.QuickPicture;


/**
 * Apply a transformation to the image to make it looky a little more cartoon-y.
 * 
 * @author Travis Mandel
 *
 */
public strictfp class CartoonifyImageTransform implements ImageTransform {

    public enum CartoonStyle  { RETRO, EXPERIMENTAL}

    public static final int NUM_ITER = 2;

    public CartoonStyle myStyle;

    /**
     * Initialize the transformation.
     * 
     * @param style Which of the two cartoony image tranforms to apply: retro or experimental 
     */
    public CartoonifyImageTransform(CartoonStyle style) {
        myStyle = style;
    }

    
    /**
     * Transforms the image based on the mode field.  
     * 
     * If the mode is RETRO, it makes the image look like a pixelated cartoon.
     * 
     * If the mode is EXPERIMENTAL, it wiggles and tints the picture to make it look more 
     *      radioactive. 
     */
    @Override
    public QuickPicture transform(QuickPicture old) {
        if(!myStyle.equals(CartoonStyle.EXPERIMENTAL))
        {
            old = applyRetroDistortion(old);
            return old;
        } else {
            return applyWiggleDistortion(old);
        }

    }

    private QuickPicture applyRetroDistortion(QuickPicture old) {
        int centerOffHeight = old.getHeight()/4;
        int centerOffWidth = old.getWidth()/4;
        int minRun = 0;
        for (int x = centerOffHeight ; x < old.getHeight()-centerOffHeight; x++){
            for (int y = centerOffWidth; y < old.getWidth()-centerOffWidth; y ++){
                QuickColor curColor = old.getColor(x, y);
                int horizRun = x, vertRun = y;
                while(horizRun+1 < old.getWidth() && old.getColor(horizRun, y).equalsRGB(curColor)) {
                    horizRun++;

                    int horizBRun = x;
                    while(horizBRun >= 0 && old.getColor(horizBRun, y).equalsRGB(curColor))
                    {
                        horizBRun--;
                        horizRun++;
                    }

                }
                while(vertRun+1 < old.getHeight() && old.getColor(x, vertRun).equalsRGB(curColor)) {
                    vertRun++;

                    int vertBRun = y;
                    while(vertBRun >= 0 && old.getColor(x, vertBRun).equalsRGB(curColor))
                    {
                        vertBRun--;
                        vertRun++;
                    }
                }
                int minRunHere = Math.min(horizRun-x, vertRun-y);
                minRun = (minRunHere < minRun || minRun == 0) ? minRunHere : minRun;

            }
        }

        QuickPicture result = new QuickPicture(old.getWidth(), old.getHeight());
        int size = Math.min(minRun+1, old.getHeight()/2);
        for (int r = size ; r < old.getHeight() - size; r+= size * 2){

            for (int c = size; c < old.getWidth() - size; c += size* 2){
                // To do : Deal with how this handles alpha channels
                QuickColor total = new QuickColor(0, 0, 0, 0);
                int samples = 0;
                for (int ro = -size; ro < size; ro++){
                    for (int co = -size; co < size; co++){
                        QuickColor oldColor = old.getColor(c+co, r+ro);
                        total.setRed(total.getRed() + oldColor.getRed());
                        total.setGreen(total.getGreen() + oldColor.getGreen());
                        total.setBlue(total.getBlue() + oldColor.getBlue());

                        // TODO Eval: delete the next line
                        total.setAlpha(total.getAlpha() + oldColor.getAlpha());
                        samples++;
                    }
                }

                for (int roff = -size; roff < size; roff++){
                    for (int coff = -size; coff < size; coff++){
                        if (r + roff >= 0 && r + roff < old.getHeight() && c + coff >=0 && c + coff < old.getWidth()){
                            QuickColor newColor = new QuickColor(total.getRed()/samples, 
                                    total.getGreen()/samples, total.getBlue()/samples, 

                                    // TODO Eval: replace the following line 
                                    // old.getColor(c+coff, r+roff).getAlpha());
                                    total.getAlpha()/samples);
                            result.setColor(c+coff, r+roff, newColor);

                        }
                    }
                }
            }
        }

        return result;
    }

    private QuickPicture applyWiggleDistortion(QuickPicture old) {

        QuickPicture result = new QuickPicture(old.getWidth(), old.getHeight());

        for (int x = 0 ; x < old.getWidth(); x++){
            for (int y = 0; y < old.getHeight(); y ++){
                QuickColor curColor = old.getColor(x, y);

                QuickColor newColor = new QuickColor(0, 0, 0, curColor.getAlpha());
                if(curColor.getRed() > 0  && curColor.getGreen() < 0 && curColor.getBlue() > 0)
                {
                    //horiz only
                    QuickColor left = getSafeColor(old, x-1, y);
                    QuickColor right = getSafeColor(old, x+1, y);
                    newColor.setRed((left.getRed() + right.getRed())/2);
                    newColor.setGreen((left.getGreen() + right.getGreen())/2);
                    newColor.setRed((left.getBlue() + right.getBlue())/2);
                }
                else if(curColor.getRed() > 0  && curColor.getGreen() > 0 && curColor.getBlue() < 0)
                {
                    //diag only
                    QuickColor upperLeft = getSafeColor(old, x-1, y-1);
                    QuickColor upperRight = getSafeColor(old, x+1, y-1);
                    QuickColor lowerLeft = getSafeColor(old, x-1, y+1);
                    QuickColor lowerRight = getSafeColor(old, x+1, y+1);
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
                    QuickColor top = getSafeColor(old, x, y-1);
                    QuickColor bottom = getSafeColor(old, x, y+1);
                    newColor.setRed((top.getRed() + bottom.getRed())/2);
                    newColor.setGreen((top.getGreen() + bottom.getGreen())/2);
                    newColor.setRed((top.getBlue() + bottom.getBlue())/2);

                }



                newColor.setAlpha(curColor.getAlpha());
                result.setColor(x, y, newColor);
            }
        }
        return result;
    }

    public static QuickColor getSafeColor(QuickPicture p, int c, int r) {
        QuickColor result = p.getColor(c, r);
        if(result == null)
            return new QuickColor(0,0,0,0);
        else
            return result;
    }
}
