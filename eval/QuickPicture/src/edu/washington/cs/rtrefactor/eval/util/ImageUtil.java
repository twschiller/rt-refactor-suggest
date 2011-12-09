package edu.washington.cs.rtrefactor.eval.util;

import edu.washington.cs.rtrefactor.eval.ImageTransform;
import edu.washington.cs.rtrefactor.eval.QuickColor;
import edu.washington.cs.rtrefactor.eval.QuickPicture;

/**
 * Provides some simple, basic image transformations which may prove useful to other classes.
 * @author Travis Mandel
 *
 */
public class ImageUtil {

    public static class Flip implements ImageTransform {
        /**
         * Flips the image vertically.
         */
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


    /** 
     * Shrinks the image
     */
    public static class ShrinkImage implements ImageTransform {

        int shrinkFactor;
        /**
         * Initialize the transform
         * @param factor The power of two by which to shrink the image (for example, 3 would 
         *      create an image 1/8th the size)
         */
        public ShrinkImage(int factor){
            shrinkFactor = (int)Math.pow(2, factor);
        }

        @Override
        public QuickPicture transform(QuickPicture old) {
            return ImageUtil.downsizeImage(old, shrinkFactor);
        }

    }


    //Maintenance task: This method works, but it does not correctly blend the alphas when downsizing the
    //image.  Whenever we merge a block of pixels into the same color, we want to change the alpha to be the 
    //average.  

    /**
     * Shrinks the image by the indicated factor.
     * 
     * @param orig The provided image
     * @param pow2 The factor by which to shrink.  Must be a power of 2 (2,4,8,etc.)
     * @return The shrunken image.
     */
    public static QuickPicture downsizeImage(QuickPicture orig, int pow2) {

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

                        // TODO Eval: delete the following line
                        total.setAlpha(total.getAlpha() + oldColor.getAlpha());
                        samples++;
                    }
                }

                QuickColor newColor = new QuickColor(total.getRed()/samples, 
                        total.getGreen()/samples, total.getBlue()/samples, 
                        // TODO Eval: replace the following line
                        //orig.getColor(c, r).getAlpha());
                        total.getAlpha()/samples);
                res.setColor(c/pow2, r/pow2, newColor);
            }
        }

        return res;
    }

}
