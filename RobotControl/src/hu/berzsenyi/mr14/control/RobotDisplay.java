package hu.berzsenyi.mr14.control;

import hu.berzsenyi.mr14.net.msg.MsgQuality;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Label;
import java.awt.TextField;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;

public class RobotDisplay extends Frame implements WindowListener {
	private static final long serialVersionUID = -3451693389015184734L;
	
	public RobotControl control;
	public boolean shouldClose = false;
	
	public Canvas cameraCanvas;
	public Label labelQuality;
	public TextField textFieldQuality;
	
	public void onQualityChange(String text) {
		try {
			int q = Integer.parseInt(text);
			if(q < 0) {
				q = 0;
				this.textFieldQuality.setText("0");
			}
			if(100 < q) {
				q = 100;
				this.textFieldQuality.setText("100");
			}
			this.control.tcp.sendMsg(new MsgQuality((byte)q));
		} catch(Exception e) {
			this.textFieldQuality.setText("50");
		}
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
		
		this.labelQuality = new Label("Quality: ");
		this.add(this.labelQuality);
		
		this.textFieldQuality = new TextField("50");
		this.textFieldQuality.addTextListener(new TextListener() {
			@Override
			public void textValueChanged(TextEvent event) {
				onQualityChange(textFieldQuality.getText());
			}
		});
		this.add(this.textFieldQuality);
		
		this.addWindowListener(this);
		this.resizeObjects(640, 480);
		this.setVisible(true);
	}
	
	public void resizeObjects(int imgWidth, int imgHeight) {
		this.cameraCanvas.setBounds((this.getWidth()-imgWidth)/2, (this.getHeight()-imgHeight)/2, imgWidth, imgHeight);
		this.labelQuality.setBounds(this.cameraCanvas.getX(), this.cameraCanvas.getY()+this.cameraCanvas.getHeight()+5, 45, 20);
		this.textFieldQuality.setBounds(this.labelQuality.getX()+this.labelQuality.getWidth(), this.labelQuality.getY(), 30, 20);
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
