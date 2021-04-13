package com.plakatanima;

import static org.bytedeco.javacpp.opencv_core.IPL_DEPTH_8U;
//import static org.bytedeco.javacpp.opencv_core.cvCountNonZero;
import static org.bytedeco.javacpp.opencv_core.cvGetSize;
import static org.bytedeco.javacpp.opencv_core.cvRect;
import static org.bytedeco.javacpp.opencv_core.cvResetImageROI;
import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.CV_SHAPE_ELLIPSE;
import static org.bytedeco.javacpp.opencv_imgproc.cvResize;
import static org.bytedeco.javacpp.opencv_video.createBackgroundSubtractorMOG2;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import org.apache.commons.lang.StringUtils;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_imgproc;
import org.bytedeco.javacpp.opencv_core.IplConvKernel;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_video.BackgroundSubtractorMOG2;
import org.bytedeco.javacpp.opencv_videoio.VideoCapture;
import org.bytedeco.javacv.Frame;
import com.openalpr.jni.Alpr;
import com.openalpr.jni.AlprException;
import com.openalpr.jni.AlprPlateResult;
import com.openalpr.jni.AlprResults;

import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

public class HareketAlgilama extends Thread {
	
	static BackgroundSubtractorMOG2 mog;   			
	static OpenCVFrameConverter.ToIplImage converterIPL = new OpenCVFrameConverter.ToIplImage();
	static OpenCVFrameConverter.ToMat converterMat = new OpenCVFrameConverter.ToMat();
	static Java2DFrameConverter convertTobuffer = new Java2DFrameConverter();
	//tarihleri bu nesnelerden alıyorumm..
	static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	static java.util.Date bugun = new java.util.Date();
	static String strDate = sdf.format(bugun);
	static String DosyaIsmi = strDate.replaceAll(":", "-");							
	static IplImage resultImg = new IplImage(); 
	static String KarakterEski = "", KarakterYeni = "";
	
	ImageIcon myImageIcon = null;

	private VideoCapture capture;
	private Mat image1 ;
	
	private static Timestamp lastProcessTime = new Timestamp(System.currentTimeMillis());
	
	private static boolean screenClear = false;

	public static String getKarakterEski() {
		return KarakterEski;
	}

	public static void setKarakterEski(String karakterEski) {
		KarakterEski = karakterEski;
	}

	public static String getKarakterYeni() {
		return KarakterYeni;
	}

	public static void setKarakterYeni(String karakterYeni) {
		KarakterYeni = karakterYeni;
	}


	@SuppressWarnings("resource")
	public void run() {
		
		capture = new VideoCapture(0);
		image1 = new Mat();
		//Dikdörtgen bir alan oluşturup  belirlenen değerler 
		IplConvKernel.create(3, 3, 1, 1, CV_SHAPE_ELLIPSE, null);
		mog = createBackgroundSubtractorMOG2(15, 300, false);
		Mat foreground = new Mat();
		Mat background = new Mat();

		while (true) {

			capture.read(image1);
			Frame frame =  converterMat.convert(image1);
			IplImage img = converterIPL.convert(frame);
			resultImg = img.clone();
			IplImage resize2 = IplImage.create(640, 360, resultImg.depth(), resultImg.nChannels());
			cvResize(resultImg, resize2);

			IplImage gray = IplImage.create(opencv_core.cvGetSize(resultImg), IPL_DEPTH_8U, 1);
			opencv_imgproc.cvCvtColor(resultImg, gray, CV_BGR2GRAY); // renkli görüntüyü gri tonlamaya çeviriyor.
			myImageIcon = new ImageIcon(convertTobuffer.convert(converterIPL.convert(resize2)));  

			AnaPencere.videoGirisLabel
					.setIcon(myImageIcon);
			Timestamp currentTime = new Timestamp(System.currentTimeMillis());
			int zamanFarki = (int) (currentTime.getTime() - lastProcessTime.getTime());
			if(zamanFarki > 5000) {
				if (screenClear) {
					setScreenValues("", "Kapalı","");
				}
				
				int x = (resultImg.width()) / 8;
				int y = resultImg.width() / 2;
				int with =resultImg.width();
				int hight =resultImg.height();
				opencv_core.cvSetImageROI(gray, cvRect(x, y, with, hight));// (x,y,width,height)
				IplImage roiCikis = IplImage.create(cvGetSize(gray), gray.depth(), gray.nChannels());
				opencv_core.cvCopy(gray, roiCikis);
				cvResetImageROI(gray);
				Mat crrntFrame = converterIPL.convertToMat(converterIPL.convert(roiCikis));
				foreground = crrntFrame.clone();
				background = crrntFrame.clone();
				mog.setNMixtures(3);
				mog.apply(crrntFrame, foreground, 0.09); 	
				mog.getBackgroundImage(background);    
				PlakaBulPanel.plakaAlaniMotionAlani  
						.setIcon(new ImageIcon(convertTobuffer.convert(converterIPL.convert(foreground))));
	
					plakaBul();
			}

		}

	}

	public static void plakaBul() {

		try {
			BufferedImage img = convertTobuffer.convert(converterIPL.convert(resultImg));
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(img, "png", baos);
			baos.flush();  //arabelleği temizler
			byte[] imageInByte = baos.toByteArray();
			baos.close();
			Alpr alpr = new Alpr("eu", "openalpr.conf", "runtime_data");
			alpr.setTopN(10);
			alpr.setDefaultRegion("tr");
			AlprResults results = alpr.recognize(imageInByte);
			for (AlprPlateResult result : results.getPlates()) {
				if (result.getBestPlate().getCharacters() != null) {
					setKarakterEski(result.getBestPlate().getCharacters());
					int uzunluk = getKarakterEski().length();
					System.out.println("Uzunluk : " + uzunluk + " Plaka : " + result.getBestPlate().getCharacters());
					if (!getKarakterYeni().equals(getKarakterEski()) & (uzunluk > 6)) {  
						
						String okunanPlaka = result.getBestPlate().getCharacters();
						boolean hataDurumu = false;
						hataDurumu = PlakaValidation(okunanPlaka);
						System.out.println("plaka :"+ okunanPlaka + " Hata :" + hataDurumu);
						bugun = new java.util.Date();
						strDate = sdf.format(bugun);
						DosyaIsmi = strDate.replaceAll(":", "-");
						DosyaIsmi = DosyaIsmi.replaceAll(" ", "_");
						
						if(!hataDurumu) {
							if (AracGiriyorMu(okunanPlaka)) {
								System.out.println("Araç giriş: " + okunanPlaka);
								AracGiris( okunanPlaka ,getKarakterEski()+"_"+ DosyaIsmi +".png" );
								setScreenValues(okunanPlaka, "Açılıyor","Araç Giriyor");
							} else {
								System.out.println("Araç çıkış: " + okunanPlaka);
								AracCikis( okunanPlaka ,getKarakterEski()+"_"+ DosyaIsmi +".png" );
								setScreenValues(okunanPlaka, "Açılıyor","Araç Çıkıyor");
							}
							lastProcessTime = new Timestamp(System.currentTimeMillis());
						}else {
							System.out.println("Plaka Uygun Değil");
						}
						try {
							if (!hataDurumu) {
								File outputfile = new File(new URI("file:///C:/PlakaOtomasyon/web/imgPlaka/"+getKarakterEski()+"_"+DosyaIsmi+".png"));
								System.out.println("Plaka Resim : " + outputfile.getPath());
							    ImageIO.write(img, "png", outputfile);
							}else {
								System.out.println("Böyle bir plaka olamayacağı için fotoğraf dosyaya eklenemiyor. ");
							}

						    
						} catch (IOException | URISyntaxException e) {
						    System.out.println("Resim Hata : " + e.getMessage());
						}
						
					}

				}
				setKarakterYeni(result.getBestPlate().getCharacters());
			}

			alpr.unload(); //temizliyor

		} catch (IOException | AlprException e1) {
			e1.printStackTrace();
		}

	}
	
	
	
	static DAO dao =new DAO();
	
	 public static void AracGiris(String plaka , String filePath) {
		 
	        try {
	        	
	        	 PreparedStatement pst = dao.getConn().prepareStatement("INSERT INTO aracgiriscikis(id, plaka , imgfilepath ,giriszamani) VALUES (default, ? , ? ,current_timestamp)", Statement.RETURN_GENERATED_KEYS);
	             pst.setString(1, plaka);
	             pst.setString(2, filePath);
	             pst.executeUpdate();

	             ResultSet gk = pst.getGeneratedKeys();
	             Long kayit_id = null;
	             if (gk.next()) {
	                 kayit_id = gk.getLong(1);
	                 System.out.println("kayıt başarılı ile atıldı id:"+ kayit_id);
	             }
	        } catch (SQLException e) {
	            System.out.println(e.getMessage());
	        }
	    }
	
	 public static void AracCikis(String plaka ,String filepathout) {		
		try {
			/*String QueryStr = "UPDATE aracgiriscikis SET cikisZamani = current_timestamp," + 
					"  parkSuresi = ((DATE_PART('day', current_timestamp::timestamp - girisZamani::timestamp) * 24 +" + 
					"                DATE_PART('hour', current_timestamp::timestamp - girisZamani::timestamp)) * 60 +" + 
					"                DATE_PART('minute', current_timestamp::timestamp - girisZamani::timestamp)) * 60 +" + 
					"                DATE_PART('second', current_timestamp::timestamp - girisZamani::timestamp) :: int " + 
					" WHERE id IN (SELECT MAX(id) FROM aracgiriscikis WHERE plaka=?)";*/
			String QueryStr = "SELECT set_araccikis(? , ?)";
		    PreparedStatement pst = dao.getConn().prepareStatement(QueryStr);
		    pst.setString(1, plaka);
		    pst.setString(2, filepathout);
		    pst.executeUpdate();
		} catch (SQLException e) {
		   // System.out.println("Exception : " + e.getMessage());
		}
	     
	 }
	 
	 public static boolean AracGiriyorMu(String plaka) {	       
	        try {
	        	String QueryStr = "select count(id) as sayi from aracgiriscikis where id IN " +
	        					  " (SELECT MAX(id) FROM aracgiriscikis where aracgiriscikis.plaka='" + plaka + "') "  + 
	        					  " and  aracgiriscikis.cikiszamani IS NULL";
	            PreparedStatement pst = dao.getConn().prepareStatement(QueryStr);
	            ResultSet rs = pst.executeQuery();
	            rs.next();
	            int count = rs.getInt("sayi");
	            if (count == 0) {
					return true;
				} else {
					return false;
				}
	        } catch (SQLException e) {
	            System.out.println(e.getMessage());
	        }	         
		 
		 return true;
	 }
	 
	 public static void setScreenValues(String Plaka, String Bariyer, String Durum) {
		 AnaPencere.Plaka.setText("Plaka: " + Plaka);
		 AnaPencere.Bariyer.setText("Bariyer: " + Bariyer);
		 AnaPencere.Durum.setText("Durum: " + Durum);
		 screenClear=true;
	 }
	 
	 public static boolean PlakaValidation(String plaka) {
			
			if(StringUtils.isNotEmpty(plaka)) {
				int Kackarakter = plaka.length();
				char sekizkarakter ='a';
				if(Kackarakter == 8 || Kackarakter ==7) 
				{
					char ilkkarakter = plaka.charAt(0);
					char ikincikarakter = plaka.charAt(1);
					char ucuncukarakter = plaka.charAt(2);
					char dortkarakter = plaka.charAt(3);
					char beskarakter = plaka.charAt(4);
					char altikarakter = plaka.charAt(5);
					char yedikarakter = plaka.charAt(6);
					if(Kackarakter == 8)
					{
						sekizkarakter = plaka.charAt(7);
					}
					
					if(ilkkarakter>='0' && ilkkarakter <= '8')
					{
						if(ikincikarakter>='0' && ikincikarakter <= '9')
						{
							if(ucuncukarakter>='A' && ucuncukarakter <= 'Z')
							{
								if((dortkarakter>='A' && dortkarakter <= 'Z') || (dortkarakter>='0' && dortkarakter <= '9'))
								{
									if((beskarakter>='A' && beskarakter <= 'Z') || (beskarakter>='0' && beskarakter <= '9'))
									{
										if(altikarakter>='0' && altikarakter <= '9') {
											
											if(yedikarakter>='0' && yedikarakter <= '9') {
												
												if(Kackarakter == 8)
												{
													if(sekizkarakter>='0' && sekizkarakter <= '9' ) {
														
														return false ;
													}
													else {
														return true;
													}
													
												}
												
												return false ;
											}
											
											else {
												System.out.println("Plaka tanımsız...");
												return true;
											}
										}
										
										else {
											System.out.println("Plaka tanımsız...");
											return true;
										}
									}
									else {
										System.out.println("Plaka tanımsız...");
										return true;
									}
								}
								else {
									System.out.println("Plaka tanımsız...");
									return true;
								}
							}
							else {
								System.out.println("Plaka tanımsız...");
								return true;
							}
						}
						else {
							System.out.println("Plaka sayi ile baslamalidir...");
							return true;
						}
					}
					else {
						System.out.println("Plaka sayi ile baslamalidir...");
						return true;
					}
				}
			}
			else {
				System.out.println("Plaka Tanımsız....");
				return true;
			}
			return true;
		}

}
