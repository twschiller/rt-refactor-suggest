package edu.washington.cs.rtrefactor.eval;

import java.util.Arrays;

/**
 * A no-frills color implementation which allows for nonstandard RGB values 
 * 
 * @author Travis Mandel
 *
 */
public class QuickColor {
        private int red;
        private int green;
        private int blue;
        private int alpha;
        
        /**
         * Initialize the color with RGBA values.
         * 
         * Color values roughly center at 0, but have no minimum or maximum.
         * 
         * @param red The red channel
         * @param green The green channel
         * @param blue The blue channel
         * @param alpha The alpha channel
         */
        public QuickColor(int red, int green, int blue, int alpha)
        {
                this.setRed(red);
                this.setGreen(green);
                this.setBlue(blue);
                this.setAlpha(alpha);
        }

        /**
         * Get the current value of the red channel
         * @return the current value of the red channel
         */
        public int getRed() {
                return red;
        }
        
        /**
         * Set the red channel value
         * @param red the new channel value
         */
        public void setRed(int red) {
                this.red = red;
        }
        
        /**
         * Get the current value of the green channel
         * @return the current value of the green channel
         */
        public int getGreen() {
                return green;
        }
        
        /**
         * Set the green channel value
         * @param green the new channel value
         */
        public void setGreen(int green) {
                this.green = green;
        }

        /**
         * Get the current value of the blue channel
         * @return the current value of the blue channel
         */
        public int getBlue() {
                return blue;
        }
        
        /**
         * Set the blue channel value
         * @param blue the new channel value
         */
        public void setBlue(int blue) {
                this.blue = blue;
        }

        /**
         * Get the current value of the alpha channel
         * @return the current value of the alpha channel
         */
        public int getAlpha() {
                return alpha;
        }
        
        /**
         * Set the alpha channel value
         * @param alpha the new channel value
         */
        public void setAlpha(int alpha) {
                this.alpha = alpha;
        }
        
        public boolean equals(Object other) {
                QuickColor qc = (QuickColor)other;
                return (alpha == qc.alpha && red == qc.red && blue == qc.blue && green == qc.green);
        }
        
        /**
         * Same as equals, but ignores the RGB values
         * @param other The other QuickColor
         * @return true iff the colors have identical RGB values
         */
        public boolean equalsRGB(Object other) {
                QuickColor qc = (QuickColor)other;
                return (red == qc.red && blue == qc.blue && green == qc.green);
        }
        
        public int hashCode() {
                return Arrays.hashCode(new int[] {red, green, blue, alpha});
                
        }
        
        public String toString() {
                return getRed() + " " + getGreen() + " " + getBlue() + 
                                " " + getAlpha();
        }
        
        
}
