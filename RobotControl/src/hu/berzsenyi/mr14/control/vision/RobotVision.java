package hu.berzsenyi.mr14.control.vision;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class RobotVision {
	public int width = -1, height;
	public int[][] buffer1, buffer2;
	
	public FloodFill fill = new FloodFill(0.075F);
	
	public void process1(BufferedImage img, int x, int y) {
		long time = System.currentTimeMillis();
		
		if(this.width == -1) {
			this.width = img.getWidth();
			this.height = img.getHeight();
			this.buffer1 = new int[this.width][this.height];
			this.buffer2 = new int[this.width][this.height];
		} else if(this.width != img.getWidth() || this.height != img.getHeight()) {
			System.err.println("WRONG SIZE!");
			return;
		}
		
		for(int i = 0; i < this.width; i++)
			for(int j = 0; j < this.height; j++) {
				this.buffer1[i][j] = img.getRGB(i, j);
				this.buffer2[i][j] = 0;
			}
		
		this.fill.fill(this.buffer1, this.buffer2, x, y);
		
		System.out.println("Done processing in "+(System.currentTimeMillis()-time)+" ms.");
		
		try {
			ImageIO.write(img, "PNG", new File("leftOriginal.png"));
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		for(int i = 0; i < this.width; i++)
			for(int j = 0; j < this.height; j++)
				img.setRGB(i, j, this.buffer2[i][j]);
		try {
			ImageIO.write(img, "PNG", new File("leftFilled.png"));
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public void process2(BufferedImage imgRight) {
		
	}
}
