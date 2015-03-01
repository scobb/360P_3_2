import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {
	// constants
	private static final String TCP = "T";
	private static final String UDP = "U";
	private static final int len = 1024;

	// members
	private String id;
	private InetAddress add;
	private byte[] rbuffer = new byte[len];

	public Client() {
		this.id = null;
		this.add = null;
	}

	public void parseConfig(String conf) {
		String[] confSplit = conf.split(" ");
		this.id = "c" + confSplit[0];
		try {
			this.add = InetAddress.getByName(confSplit[1]);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	public void processCommand(String cmd) {
		String[] cmdSplit = cmd.split(" ");
		String book = cmdSplit[0];
		String directive = cmdSplit[1];
		int port = Integer.parseInt(cmdSplit[2]);
		String protocol = cmdSplit[3];

		if (protocol.equals(UDP)) {
			processUdp(book, directive, port);
		} else {
			processTcp(book, directive, port);
		}

	}

	public void processTcp(String book, String directive, int port) {
		String send = id + " " + book + " " + directive;
		// System.out.println("send: " + send);
		Socket s = null;
		try {
			// System.out.println("adding socket on " + port);
			s = new Socket(this.add, port);
			Scanner in = new Scanner(s.getInputStream());
			PrintWriter out = new PrintWriter(s.getOutputStream(), true);
			out.println(send);
			System.out.println(in.nextLine());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (s != null) {
				try {
					s.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	public void processUdp(String book, String directive, int port) {
		String send = id + " " + book + " " + directive;
		// System.out.println("send: " + send);
		DatagramPacket sPacket, rPacket;
		DatagramSocket datasocket;
		try {
			datasocket = new DatagramSocket();
			byte[] buffer = send.getBytes();
			sPacket = new DatagramPacket(buffer, buffer.length, add, port);
			datasocket.send(sPacket);
			rPacket = new DatagramPacket(rbuffer, rbuffer.length);
			datasocket.receive(rPacket);
			String retstring = new String(rPacket.getData(), 0,
					rPacket.getLength());
			System.out.println(retstring);
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Client c = new Client();
		Scanner sc = new Scanner(System.in);
		c.parseConfig(sc.nextLine());
		while (true) {
			c.processCommand(sc.nextLine());
		}
	}
}
