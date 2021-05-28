package com.plakatanima;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;

public class AnaPencere extends javax.swing.JFrame {

	private static final long serialVersionUID = 1L; 
	static JPanel
	anaPanel,GuncelGecislerPanel,videoPanel;
	static String PlakaMetin = "";
	static JLabel
	videoGirisLabel,
	Plaka,Bariyer,Durum;
	static PlakaBulPanel plakabulPanel = new PlakaBulPanel();
	HareketAlgilama hareketAlgila = new HareketAlgilama();
	Thread HareketThread;
	public AnaPencere() {
		

		
		videoGirisLabel = new JLabel();
		videoGirisLabel.setPreferredSize(new Dimension(640,360)); 
		videoGirisLabel.setIcon(new ImageIcon("icons/cam.jpg"));
		
				GuncelGecislerPanel = new JPanel();
				GuncelGecislerPanel.setLayout(new MigLayout()); 
				Plaka = new JLabel("Plaka: ");
				Plaka.setFont(new Font("Serif", Font.BOLD, 24));
				Bariyer = new JLabel("Bariyer: Kapalý");     
				Bariyer.setFont(new Font("Serif", Font.BOLD, 24));
				Durum = new JLabel("Durum: ");
				Durum.setFont(new Font("Serif", Font.BOLD, 24)); 
				GuncelGecislerPanel.add(Plaka,"cell 0 2,spanx 3"); 
				GuncelGecislerPanel.add(Bariyer,"cell 0 3,spanx 3");
				GuncelGecislerPanel.add(Durum,"cell 0 4,spanx 3");
					

		videoPanel = new JPanel();
		videoPanel.setLayout(new MigLayout()); 
		videoPanel.add(videoGirisLabel,"cell 0 0");  	
		videoPanel.add(plakabulPanel.getPlakaBulTabPane(),"cell 0 1"); 					
		anaPanel = new JPanel();
		anaPanel.setLayout(new CardLayout());
		anaPanel.add(videoPanel);  
		JFrame pencere = new JFrame();
		pencere.setVisible(true);
		pencere.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pencere.setSize(900, 720);
		pencere.add(anaPanel,BorderLayout.CENTER);
		pencere.add(GuncelGecislerPanel, BorderLayout.EAST);
		
		try {
				HareketThread = new Thread(hareketAlgila);
				HareketThread.start();
	
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}

