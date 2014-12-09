package hu.berzsenyi.mr14.control.vision;

import java.util.LinkedList;

public class FloodFill {
	public static int getRed(int c) {
		return (c >> 16) & 255;
	}
	
	public static int getGreen(int c) {
		return (c >> 8) & 255;
	}
	
	public static int getBlue(int c) {
		return c & 255;
	}
	
	public static float redDiff(int c) {
		return getRed(c)/(float)(getRed(c)+getGreen(c)+getBlue(c));
	}
	
	public static float greenDiff(int c) {
		return getGreen(c)/(float)(getRed(c)+getGreen(c)+getBlue(c));
	}
	
	public static float blueDiff(int c) {
		return getBlue(c)/(float)(getRed(c)+getGreen(c)+getBlue(c));
	}
	
	public float mdiff, sr, sg, sb;
	
	public FloodFill(float mdiff) {
		this.mdiff = mdiff;
	}
	
	public float diff(int c) {
		return (Math.abs(this.sr-redDiff(c)) + Math.abs(this.sg-greenDiff(c)) + Math.abs(this.sb-blueDiff(c)))/3;
	}
	
	public void fill(int[][] input, int[][] output, int x, int y) {
		LinkedList<Integer> xs = new LinkedList<Integer>(), ys = new LinkedList<Integer>();
		xs.add(x);
		ys.add(y);
		output[x][y] = input[x][y];
		this.sr = redDiff(input[x][y]);
		this.sg = greenDiff(input[x][y]);
		this.sb = blueDiff(input[x][y]);
		while(!xs.isEmpty()) {
			int i = xs.poll();
			int j = ys.poll();
			
			if(0 < i && output[i-1][j] == 0
				&& this.diff(input[i-1][j]) < this.mdiff) {
				output[i-1][j] = input[x][y];
				xs.add(i-1);
				ys.add(j);
			}
			if(i < input.length-1 && output[i+1][j] == 0
				&& this.diff(input[i+1][j]) < this.mdiff) {
				output[i+1][j] = input[x][y];
				xs.add(i+1);
				ys.add(j);
			}
			if(0 < j && output[i][j-1] == 0
				&& this.diff(input[i][j-1]) < this.mdiff) {
				output[i][j-1] = input[x][y];
				xs.add(i);
				ys.add(j-1);
			}
			if(j < input[0].length-1 && output[i][j+1] == 0
				&& this.diff(input[i][j+1]) < this.mdiff) {
				output[i][j+1] = input[x][y];
				xs.add(i);
				ys.add(j+1);
			}
		}
	}
}
