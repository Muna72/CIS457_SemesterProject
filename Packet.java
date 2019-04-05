import java.util.ArrayList;
import java.io.*;
public class Packet implements Serializable  {
	private static final long serialVersionUID =1L;
	public String  message;
	public String uname;
	//uname | ip | 
	public ArrayList<Integer[]> l;
	public  CommandType type;
	public int port;

	public Packet(CommandType type){
	this.type=type;
	}
}
