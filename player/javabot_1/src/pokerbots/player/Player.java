package pokerbots.player;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

/**
 * Simple example pokerbot, written in Java.
 * 
 * This is an example of a bare bones, pokerbot. It only sets up the socket
 * necessary to connect with the engine and then always returns the same action.
 * It is meant as an example of how a pokerbot should communicate with the
 * engine.
 * 
 */
public class Player {
	
	private final PrintWriter outStream;
	private final BufferedReader inStream;

	private ArrayList<Card> hand;
	private HashMap<String, String> sets;

	public Player(PrintWriter output, BufferedReader input) {
		this.outStream = output;
		this.inStream = input;
		sets = new HashMap<>();
		try (BufferedReader br = new BufferedReader(new FileReader("results.txt"))) {
			String line;
			while ((line = br.readLine()) != null) {
				String split[] = line.split(":");
				if (split.length > 1) {
					sets.put(split[0], split[1]);
					System.out.println(line);
				}
				if (line.equalsIgnoreCase("REGRETS")) {
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void run() {
		String input;
		try {
			// Block until engine sends us a packet; read it into input.
			while ((input = inStream.readLine()) != null) {

				// Here is where you should implement code to parse the packets
				// from the engine and act on it.
				System.out.println(input);
				String[] list = input.split(" ");
				
				String type = list[0];
				if ("GETACTION".compareToIgnoreCase(type) == 0) {
					for(String action : list) {
						if (action.compareToIgnoreCase("CHECK") == 0) {
							outStream.println("CHECK");
						} else if (action.compareToIgnoreCase("CALL") == 0){
							outStream.println("CALL");
						}
					}
					// When appropriate, reply to the engine with a legal
					// action.
					// The engine will ignore all spurious packets you send.
					// The engine will also check/fold for you if you return an
					// illegal action.
					outStream.println("CHECK");
				} else if ("REQUESTKEYVALUES".compareToIgnoreCase(type) == 0) {
					// At the end, engine will allow bot to send key/value pairs to store.
					// FINISH indicates no more to store.
					outStream.println("FINISH");
				} else if ("NEWHAND".compareToIgnoreCase(type) == 0) {
					hand = new ArrayList<>();
					hand.add(new Card(list[3], 0));
					hand.add(new Card(list[4], 0));
				}
			}
		} catch (IOException e) {
			System.out.println("IOException: " + e.getMessage());
		}

		System.out.println("Gameover, engine disconnected");
		
		// Once the server disconnects from us, close our streams and sockets.
		try {
			outStream.close();
			inStream.close();
		} catch (IOException e) {
			System.out.println("Encounterd problem shutting down connections");
			e.printStackTrace();
		}
	}

	void translate(ArrayList<Card> cards) {
		Collections.sort(cards, new RankComparator());
		Collections.sort(cards, new SuitComparator());
		HashMap<Integer, Integer> suitMapping = new HashMap<Integer, Integer>();
		int openSuit = 0;
		for (Card card : cards) {
			if (!suitMapping.containsKey(card.suit)) {
				suitMapping.put(card.suit, openSuit);
				card.suit = openSuit;
				openSuit++;
			} else {
				card.suit = suitMapping.get(card.suit);
			}
		}
		Collections.sort(cards, new SuitComparator());
		Collections.sort(cards, new RankComparator());
		Collections.sort(cards, new HandComparator());
	}
	
}

class RankComparator implements Comparator<Card> {
	@Override
	public int compare(Card o1, Card o2) {
		if (o1.rank < o2.rank)
			return -1;
		if (o2.rank > o1.rank)
			return 1;
		return 0;
	}
}

class SuitComparator implements Comparator<Card> {
	@Override
	public int compare(Card o1, Card o2) {
		if (o1.suit < o2.suit)
			return -1;
		if (o2.suit > o1.suit)
			return 1;
		return 0;
	}
}

class HandComparator implements Comparator<Card> {
	@Override
	public int compare(Card o1, Card o2) {
		if (o1.hand < o2.hand)
			return -1;
		if (o2.hand > o1.hand)
			return 1;
		return 0;
	}
}