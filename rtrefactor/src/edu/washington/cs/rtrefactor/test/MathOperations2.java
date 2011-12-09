package edu.washington.cs.rtrefactor.test;

/**
 * TestFileTwo from http://jccd.sourceforge.net/getting_started.html;
 * Clones appear in {@link MathOperations1}
 * @author Todd Schiller
 * @see MathOperations1
 */
public class MathOperations2 {
	public int factorial(int n){
		if(n == 0){
			return 1;
		}else{
			return n * factorial(n-1);
		}
	}

	public int gcdTwo(int c, int d) {
		while (d != 0) {
			if (c > d) {
				c = c - d;
			} else {
				d = d - c;
			}
		}
		return c;
	}  

	public double mul(double a, long b){
		double n = 0.0;
		for(long i = 0l; i < b; i++)
			n += a;

		return n;
	}
}
