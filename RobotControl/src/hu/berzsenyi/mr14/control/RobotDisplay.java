package hu.berzsenyi.mr14.control;

import hu.berzsenyi.mr14.net.msg.MsgQuality;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class RobotDisplay extends JFrame implements WindowListener {
	private static final long serialVersionUID = -3451693389015184734L;
	
	public RobotControl control;
	public boolean shouldClose = false;
	
	public Canvas cameraCanvas;
	public JLabel labelQuality;
	public JSlider sliderQuality;
	
	public void onQualityChange(int q) {
		this.control.tcp.sendMsg(new MsgQuality((byte)q));
	}
	
	public RobotDisplay(RobotControl control) {
		this.control = control;
		this.shouldClose = false;
		
		this.setSize(800, 600);
		this.setLocationRelativeTo(null);
		this.setLayout(null);
		this.setBackground(Color.lightGray);
		
		this.cameraCanvas = new Canvas();
		this.add(this.cameraCanvas);
		
		this.labelQuality = new JLabel("Quality:");
		this.add(this.labelQuality);
		
		this.sliderQuality = new JSlider();
		this.sliderQuality.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				onQualityChange(sliderQuality.getValue());
			}
		});
		this.add(this.sliderQuality);
		
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(this);
		this.resizeObjects(640, 480);
		this.setVisible(true);
	}
	
	public void resizeObjects(int imgWidth, int imgHeight) {
		this.cameraCanvas.setBounds((this.getWidth()-imgWidth)/2, (this.getHeight()-imgHeight)/2, imgWidth, imgHeight);
		this.cameraCanvas.setLocation(0, 0);
		this.labelQuality.setBounds(this.cameraCanvas.getX(), this.cameraCanvas.getY()+this.cameraCanvas.getHeight()+5, 45, 20);
		this.sliderQuality.setBounds(this.labelQuality.getX()+this.labelQuality.getWidth(), this.labelQuality.getY(), 100, this.labelQuality.getHeight());
	}
	
	public void update(BufferedImage cameraImage) {
		if(this.cameraCanvas.getWidth() != cameraImage.getWidth() || this.cameraCanvas.getHeight() != cameraImage.getHeight())
			this.resizeObjects(cameraImage.getWidth(), cameraImage.getHeight());
		this.cameraCanvas.getGraphics().drawImage(cameraImage, 0, 0, null);
	}

	@Override
	public void windowActivated(WindowEvent event) {}

	@Override
	public void windowClosed(WindowEvent event) {}

	@Override
	public void windowClosing(WindowEvent event) {
		this.shouldClose = true;
	}

	@Override
	public void windowDeactivated(WindowEvent event) {}

	@Override
	public void windowDeiconified(WindowEvent event) {}

	@Override
	public void windowIconified(WindowEvent event) {}

	@Override
	public void windowOpened(WindowEvent event) {}
}
