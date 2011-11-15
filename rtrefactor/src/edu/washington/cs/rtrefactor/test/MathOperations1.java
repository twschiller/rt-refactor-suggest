package edu.washington.cs.rtrefactor.test;

/**
 * TestFileOne from http://jccd.sourceforge.net/getting_started.html;
 * Clones appear in {@link MathOperations2}
 * @author Todd Schiller
 * @see MathOperations2
 */
public class MathOperations1 {
	public int factorial(int n){
		if(n == 0){
			return 1;
		}else{
			return n * factorial(n-1);
		}
	}

	public int gcdOne(int a, int b) {
		while (b != 0) {
			if (a > b) {
				a = a - b;
			} else {
				b = b - a;
			}
		}
		return a;
	}

	public int mul(int a, int b){
		int n = 0;
		for(int i = 0; i < b; i++){
			n += a;
		}
		return n;
	}
}
