package hu.berzsenyi.mr14.control.vision;

public class SobelMask {
	public static final int[][] maskX = new int[][]{{-1, 0, 1},
													{-2, 0, 2},
													{-1, 0, 1}},
								maskY = new int[][]{{-1, -2, -1},
													{0, 0, 0},
													{1, 2, 1}};
	
	public void mask(int[][] input, int[][] output) {
		int max = 0;
		for(int x = 0; x < input.length; x++)
			for(int y = 0; y < input[0].length; y++)
				if(x == 0 || y == 0 || x == input.length-1 || y == input[0].length-1)
					output[x][y] = 0;
				else {
					int gx = 0, gy = 0, gxR = 0, gyR = 0, gxG = 0, gyG = 0, gxB = 0, gyB = 0;
					for(int cx = -1; cx <= 1; cx++)
						for(int cy = -1; cy <= 1; cy++) {
//							gx += maskX[cx+1][cy+1]*input[x+cx][y+cy];
//							gy += maskY[cx+1][cy+1]*input[x+cx][y+cy];
							gxR += maskX[cx+1][cy+1]*((input[x+cx][y+cy] >> 16) & 255);
							gyR += maskY[cx+1][cy+1]*((input[x+cx][y+cy] >> 16) & 255);
							gxG += maskX[cx+1][cy+1]*((input[x+cx][y+cy] >> 8) & 255);
							gyG += maskY[cx+1][cy+1]*((input[x+cx][y+cy] >> 8) & 255);
							gxB += maskX[cx+1][cy+1]*(input[x+cx][y+cy] & 255);
							gyB += maskY[cx+1][cy+1]*(input[x+cx][y+cy] & 255);
						}
//					gx = Math.abs(gxR)+Math.abs(gxG)+Math.abs(gxB);
//					gy = Math.abs(gyR)+Math.abs(gyG)+Math.abs(gyB);
//					output[x][y] = (int)Math.sqrt(gx*gx+gy*gy);
					output[x][y] = (int)(Math.sqrt(gxR*gxR + gyR*gyR) + Math.sqrt(gxG*gxG + gyG*gyG) + Math.sqrt(gxB*gxB + gyB*gyB))/3;
					if(max < output[x][y])
						max = output[x][y];
				}
		for(int x = 0; x < input.length; x++)
			for(int y = 0; y < input[0].length; y++)
				output[x][y] = output[x][y]*255/max;
	}
}
