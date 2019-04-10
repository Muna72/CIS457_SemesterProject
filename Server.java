//import java.net.*;
import java.io.*;
//import java.util.Scanner;
import java.util.ArrayList;
import java.io.ObjectOutputStream;
//import java.util.stream.Stream;

//import java.awt.BorderLayout;
import java.awt.HeadlessException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

public class Server{
	/** class variable to access the client socket**/
	private Socket socket;
	/** serversocket for host to listen for a client **/
	private ServerSocket server;
	
	/** global variable for client input **/
	private InputStream input;
	

	private TargetDataLine targetDataLine;
	
	/** global variable for client ouput **/
	private OutputStream out;
	
	/** global variable to interpret the bit in binary sound data **/
	private AudioFormat aF;
	
	/** source to the mixer, a way to write the data**/
	private SourceDataLine sDL;
	
	/** allocate amount of bytes I want to use **/ 
	int byteSize = 10000;
	
	/** read in the bytes **/ 
	byte tempBuffer[] = new byte[byteSize];
	
	/** info from the mixer system **/ 
	static Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
	
	/** handle the client for incoming threads **/
	public static ArrayList<ClientHandler> handler = new ArrayList<ClientHandler>();
	/**
	 * 
	 * 
	 * @throws LineUnavailableException
	 * @throws HeadlessException
	 * @throws UnknownHostException
	 */
	public Server() throws LineUnavailableException, HeadlessException, UnknownHostException{
		
		try{
			//system default mixer
			Mixer mixer = AudioSystem.getMixer(mixerInfo[1]);
			aF = getAudioFormat();
			DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, aF);
			sDL = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
			sDL.open(aF);
			sDL.start();
			server = new ServerSocket(9090);
			System.out.println("Running the Central Server on  9090");
			socket = server.accept();
			captureAudio();
			
	        input = new BufferedInputStream(socket.getInputStream());
	        out = new BufferedOutputStream(socket.getOutputStream());
	        while (input.read(tempBuffer) != -1) {
	            sDL.write(tempBuffer, 0, byteSize);

	        }
		}catch(IOException i){
			System.out.println(i);
		}
		while(true){	
			try{
				socket=server.accept();
				handler.add(new ClientHandler(socket));				
			}	catch(IOException i){
				System.out.println(i);
			}
		}		
	}
	
	/**
	 * 
	 * @return
	 */
	private AudioFormat getAudioFormat() {
		//https://docs.oracle.com/javase/7/docs/api/javax/sound/sampled/AudioFormat.html
	    float sampleRate = 8000.0F;
	    int sampleSizeInBits = 16;
	    int channels = 2;
	    boolean signed = true;
	    boolean bigEndian = false;
	    
	    return new AudioFormat(sampleRate,sampleSizeInBits,channels,signed,bigEndian);
	}
	
	/**
	 * 
	 */
	private void captureAudio() {
	    try {

	        aF = getAudioFormat();
	        DataLine.Info dataLineInfo = new DataLine.Info(
	                TargetDataLine.class, aF);
	        Mixer mixer = null;
	        System.out.println("Server Ip Address "+InetAddress.getLocalHost().getHostAddress());
	        System.out.println("Available Hardware Devices:");
	        //print out system sound/listening devices
	        for (int cnt = 0; cnt < mixerInfo.length; cnt++) {
	            mixer = AudioSystem.getMixer(mixerInfo[3]);      // Select Available Hardware Devices for the micro, for my Notebook it is number 3
	            if (mixer.isLineSupported(dataLineInfo)) {
	                System.out.println(cnt+":"+mixerInfo[cnt].getName());
	                targetDataLine = (TargetDataLine) mixer.getLine(dataLineInfo);
	            }
	        }
	        targetDataLine.open(aF);
	        targetDataLine.start();

	        //Thread captureThread = new CaptureThread();
	        
	        Thread captureThread = new CaptureThread();
	        captureThread.start();
	    } catch (Exception e) {
	        System.out.println(e);
	        System.exit(0);
	    }
	}
	
	/**
	 * 
	 * @author ajj_a
	 *
	 */
//	class ClientHandler extends Thread {
//		private Socket socket;
//		byte tempBuffer[] = new byte[byteSize];
//		public String uname;
//		private Packet packet;
//		private ObjectOutputStream oos;	
//		
//		
//	    @Override
//	    public void run() {
//	        try {
//	            while (true) {
//	                int cnt = targetDataLine.read(tempBuffer, 0, tempBuffer.length);
//	                out.write(tempBuffer);
//	                out.flush();
//
//	            }
//
//	        } catch (Exception e) {
//	            System.out.println(e);
//	            System.exit(0);
//	        }
//	    }
//	    
//	    /**
//	     * 
//	     * @param socket
//	     */
//		public ClientHandler(Socket socket){
//			//Response word
//			new Thread(()->{	
//				try {
//					InputStream is=socket.getInputStream();
//					ObjectInputStream ois= new ObjectInputStream(is);
//					
//					OutputStream os = socket.getOutputStream();
//					oos = new ObjectOutputStream(os);
//					
//					String line ="";
//					while(!line.equals("q")){
//						Packet p =  (Packet)ois.readObject();
//						if(p.type==CommandType.MESSAGE){
//							System.out.println(p.uname+": "+p.message);
//							Server.handler.forEach(t->{
//								try{t.oos.writeObject(p);}catch(IOException e){System.err.println(e);};
//							});		
//						}	
//
//					}
//
//				}catch(Exception i){System.out.println(i);};
//			}).start();}
//		
//		
//
//	}
	
	
	public static void main(String args[]){
		try {
			Server server = new Server();
		} catch (HeadlessException | UnknownHostException | LineUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	
	class CaptureThread extends Thread {

	    byte tempBuffer[] = new byte[byteSize];

	    @Override
	    public void run() {
	        try {
	            while (true) {
	                int cnt = targetDataLine.read(tempBuffer, 0, tempBuffer.length);
	                out.write(tempBuffer);
	                out.flush();

	            }

	        } catch (Exception e) {
	            System.out.println(e);
	            System.exit(0);
	        }
	    }
	}
	
}



class ClientHandler{
	private Socket socket;
	public String uname;
	private Packet packet;
	private ObjectOutputStream oos;	
	public ClientHandler(Socket socket){
		//Response word
		new Thread(()->{	
			try {
				InputStream is=socket.getInputStream();
				ObjectInputStream ois= new ObjectInputStream(is);
				
				OutputStream os = socket.getOutputStream();
				oos = new ObjectOutputStream(os);
				
				String line ="";
				while(!line.equals("q")){
					Packet p =  (Packet)ois.readObject();
					if(p.type==CommandType.MESSAGE){
						System.out.println(p.uname+": "+p.message);
						Server.handler.forEach(t->{
							try{t.oos.writeObject(p);}catch(IOException e){System.err.println(e);};
						});		
					}	

				}

			}catch(Exception i){System.out.println(i);};
		}).start();}
	
}



