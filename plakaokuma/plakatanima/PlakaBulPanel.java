package com.plakatanima;

import java.awt.Dimension;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;


public class PlakaBulPanel {

	 static JPanel plakaAlaniGosterPanel;
	 JTabbedPane PlakaBulTabPane = new JTabbedPane();
	 static JLabel plakaAlaniMotionAlani;	 
	 
	public static JLabel getPlakaAlaniMotionAlani() {
		return plakaAlaniMotionAlani;
	}

	public static void setPlakaAlaniMotionAlani(JLabel plakaAlaniMotionAlani) {
		PlakaBulPanel.plakaAlaniMotionAlani = plakaAlaniMotionAlani;
	}

	public JPanel getPlakaAlaniGosterPanel() {
		return plakaAlaniGosterPanel;
	}

	public void setPlakaAlaniGosterPanel(JPanel plakaAlaniGosterPanel) {
		PlakaBulPanel.plakaAlaniGosterPanel = plakaAlaniGosterPanel;
	}

	public JTabbedPane getPlakaBulTabPane() {
		return PlakaBulTabPane;
	}

	public void setPlakaBulTabPane(JTabbedPane plakaBulTabPane) {
		PlakaBulTabPane = plakaBulTabPane;
	}

	public PlakaBulPanel() {
	
		plakaAlaniGosterPanel =new JPanel();
		plakaAlaniMotionAlani = new JLabel();
		 //görüntü iþleme ekraný siyah alan 
		plakaAlaniGosterPanel.add(plakaAlaniMotionAlani,"cell 0 1"); 
		PlakaBulTabPane.setPreferredSize(new Dimension(640,360)); 
		PlakaBulTabPane.addTab("Plaka Okuma", plakaAlaniGosterPanel);
		
	}
}
