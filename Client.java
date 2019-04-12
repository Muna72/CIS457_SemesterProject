import java.net.*;
import java.io.*;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.io.Serializable;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;



public class Client{
	/** listener for client **/
	private Socket socket = null;
	
	/** used for reading data **/
	private BufferedReader br;
	
	/** insert users name **/
	private String uname;

	private String serverIp;
	
	/** flag to stop audio reading **/ 
	private boolean stopCapture = false;
	
	/** data written into byte array**/
	private ByteArrayOutputStream byteArrayOutputStream;
	
	/** interpret bits into binary sound data**/
	private AudioFormat aF;
	
	/** A way to read the audion**/
	private TargetDataLine targetDataLine;
	
	/** frames of sounds put into stream**/
	private AudioInputStream audioInputStream;
	
	/** write out the bits to stream**/
	private BufferedOutputStream out = null;
	
	/** write in the bits to stream**/
	private BufferedInputStream in = null;
	
	/** source to the mixer delivers the data **/
	private SourceDataLine sDL;

	//private ObjectInputStream ois;

	VoipGUI myGUI = new VoipGUI();
	
	/**
	 * Constructor taking in the user's ip and the username
	 * @param ip
	 * @param uname
	 */
	public Client(String ip, String uname){
		try{
			socket = new Socket(ip, 9090);
			OutputStream os = socket.getOutputStream();			
			ObjectOutputStream oos = new ObjectOutputStream(os);

			InputStream is= socket.getInputStream();
			ObjectInputStream ois = new ObjectInputStream(is);

			br = new BufferedReader(new InputStreamReader(System.in));
			String line="";
			
			//gets name
			new Thread(()->{
				Packet p = new Packet(CommandType.MESSAGE);
				try{
					
					while(p != null ||p.type==CommandType.MESSAGE){
						p = (Packet)ois.readObject();
						if(p.type==CommandType.MESSAGE){
						//System.out.println(p.uname+": "+p.message);
							myGUI.chat.append(p.uname + ": " + p.message);
					}}
				}catch(Exception e){
					System.err.println(e);}
			}).start();

			while(!line.equals("q")){
				try{
					line=br.readLine();
					Packet p = new Packet(CommandType.MESSAGE);
					p.uname=uname;

					p.message=line;
					oos.writeObject(p);

				}
				catch(IOException i){
					System.out.println(i);
				}
			}
			socket.close();
		}
		catch(Exception i){
			System.out.println(i);
		}

	}
	/*public static void main(String args[]){
		Scanner sc = new Scanner(System.in);
		System.out.println("Enter ip port username");
		//take away tokens \t\r\n\f
		StringTokenizer s = new StringTokenizer(sc.nextLine());
		Client client = new Client(s.nextToken(), s.nextToken());
		
		client.captureAudio();

	} */
	private void captureAudio() {
		// TODO Auto-generated method stub
		try {
			//give port and private ip address
	        socket = new Socket("192.168.0.11", 9090);
	        out = new BufferedOutputStream(socket.getOutputStream());
	        in = new BufferedInputStream(socket.getInputStream());

	        Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
	        System.out.println("Available Hardware devices:");
	        for (int cnt = 0; cnt < mixerInfo.length; cnt++) {
	            System.out.println(cnt+":"+mixerInfo[cnt].getName());
	        }
	        aF = getAudioFormat();

	        DataLine.Info dataLineInfo = new DataLine.Info(
	                TargetDataLine.class, aF);

	        Mixer mixer = AudioSystem.getMixer(mixerInfo[3]);    //Select Available Hardware Devices for the micro, for my Notebook it is number 3.

	        targetDataLine = (TargetDataLine) mixer.getLine(dataLineInfo);

	        targetDataLine.open(aF);
	        targetDataLine.start();

	        Thread captureThread = new CaptureThread();
	        captureThread.start();

	        DataLine.Info dataLineInfo1 = new DataLine.Info(
	                SourceDataLine.class, aF);
	        sDL = (SourceDataLine) AudioSystem
	                .getLine(dataLineInfo1);
	        sDL.open(aF);
	        sDL.start();

	        Thread playThread = new PlayThread();
	        playThread.start();

	    } catch (Exception e) {
	        System.out.println(e);
	        System.exit(0);
	    }
	}
	private AudioFormat getAudioFormat(){
		float sampleRate = 8000.0F;
		
		int sampleSizeInBits = 16;
		
		int channels = 2;
		
		boolean signed = true;
		
		//indicates whether the data for a single sample is stored in big-endian byte order (false means little-endian)
		boolean bigEndian = false;
		
		return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
	}
	
	class CaptureThread extends Thread {

	    byte tempBuffer[] = new byte[10000];

	    @Override
	    public void run() {
	        byteArrayOutputStream = new ByteArrayOutputStream();
	        stopCapture = false;
	        try {
	            while (!stopCapture) {

	                int cnt = targetDataLine.read(tempBuffer, 0,
	                        tempBuffer.length);

	                out.write(tempBuffer);

	                if (cnt > 0) {

	                    byteArrayOutputStream.write(tempBuffer, 0, cnt);

	                }
	            }
	            byteArrayOutputStream.close();
	        } catch (Exception e) {
	            System.out.println(e);
	            System.exit(0);
	        }
	    }
	}
	
	class PlayThread extends Thread {
		
	byte tempBuffer[] = new byte [10000];
	
	@Override
	public void run() {
		
		try {
			
			while (in.read(tempBuffer) != -1){
				sDL.write(tempBuffer, 0, 10000);
			}
			sDL.drain();
			sDL.close();
		}
		
		catch (IOException i){
			i.printStackTrace();
		}
		
	}
	}
}
