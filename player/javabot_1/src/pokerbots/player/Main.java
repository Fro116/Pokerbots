package pokerbots.player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Main {
	
	private static final String USAGE = "Usage: pokerbot [-h HOST] PORT";

	/**
	 * Parses args and prints out help messages as appropriate.
	 * args: [-h HOST] PORT
	 * @param args
	 * @return Created socket if args are valid, null otherwise.
	 */
	public static Socket parseArgsToSocket(String[] args) {
		String host = "localhost";
		int port;
		// PORT
		if (args.length == 1) {
			try {
				port = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				System.out.println(String.format("Error: Invalid int value: %s", args[0]));
				return null;
			}
		// -h HOST PORT
		} else if (args.length == 3 && "-h".equals(args[0])) {
			host = args[1];
			try {
				port = Integer.parseInt(args[2]);
			} catch (NumberFormatException e) {
				System.out.println(String.format("Error: Invalid int value: %s", args[0]));
				return null;
			}
		} else {
			System.out.println(USAGE);
			return null;
		}
		
		try {
			Socket socket = new Socket(host, port);
			return socket;
		} catch (IOException e) {
			System.out.println("Error connecting to host: " + e.getMessage());
			return null;
		}
	}
	
	public static void main(String[] args) {
		try {
			// Socket which will connect to the engine.
			Socket socket = parseArgsToSocket(args);
			if (socket == null) {
				return;
			}
			// Convenience wrapper to write back to engine
			PrintWriter outStream = new PrintWriter(socket.getOutputStream(), true);
			// Reader to read packets from engine
			BufferedReader inStream = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			
			Player player = new Player(outStream, inStream);
			player.run();
			
			socket.close();
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
	}

}
