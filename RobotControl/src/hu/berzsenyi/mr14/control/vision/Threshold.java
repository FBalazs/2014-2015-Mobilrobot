package hu.berzsenyi.mr14.control.vision;

public class Threshold {
	public int[] histogram = new int[256];
	
	public int calculate(int[][] input) {
		for(int i = 0; i < this.histogram.length; i++)
			this.histogram[i] = 0;
		for(int x = 0; x < input.length; x++)
			for(int y = 0; y < input[0].length; y++)
				this.histogram[input[x][y]]++;
		
		int ret = 0;
		float maxDist = 0F;
		long sumAll = 0;
		for(int i = 0; i < 256; i++)
			sumAll += this.histogram[i]*i;
		long nBlack = 0;
		long sumBlack = 0;
		for(int t = 0; t < 256; t++) {
			nBlack += this.histogram[t];
			sumBlack += this.histogram[t]*t;
			float dist = (sumAll-sumBlack)/(float)(input.length*input[0].length-nBlack) - sumBlack/(float)nBlack;
			dist = dist*dist*dist*dist*nBlack*(input.length*input[0].length-nBlack);
			if(maxDist < dist) {
				maxDist = dist;
				ret = t;
			}
		}
		
		return ret;
	}
}
