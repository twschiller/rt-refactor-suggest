package edu.washington.cs.rtrefactor.eval;

import java.util.Arrays;

public class QuickColor {
	private int red;
	private int green;
	private int blue;
	private int alpha;
	
	public QuickColor(int red, int green, int blue, int alpha)
	{
		this.setRed(red);
		this.setGreen(green);
		this.setBlue(blue);
		this.setAlpha(alpha);
	}

	public int getRed() {
		return red;
	}

	public void setRed(int red) {
		this.red = red;
	}

	public int getGreen() {
		return green;
	}

	public void setGreen(int green) {
		this.green = green;
	}

	public int getBlue() {
		return blue;
	}

	public void setBlue(int blue) {
		this.blue = blue;
	}

	public int getAlpha() {
		return alpha;
	}

	public void setAlpha(int alpha) {
		this.alpha = alpha;
	}
	
	public boolean equals(Object other) {
		QuickColor qc = (QuickColor)other;
		return (alpha == qc.alpha && red == qc.red && blue == qc.blue && green == qc.green);
	}
	
	public boolean equalsRGB(Object other) {
		QuickColor qc = (QuickColor)other;
		return (red == qc.red && blue == qc.blue && green == qc.green);
	}
	
	public int hashCode() {
		return Arrays.hashCode(new int[] {red, green, blue, alpha});
		
	}
	
	
}
