package pokerbots.player;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

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
	private ArrayList<Card> board;
	private HashMap<String, String> sets;
	private HashMap<String, Integer> flopColex;
	private ArrayList<Integer> flopBuckets;
	private ArrayList<Integer> flopDiscards;
	private ArrayList<String> turnData;
	private ArrayList<ArrayList<Double>> riverCenters;
	private ArrayList<String> handHistory;
	private ArrayList<String> roundHistory;

	private int roundContribution;
	private int pot;
	private int turn = 0;

	public Player(PrintWriter output, BufferedReader input) {
		long time = System.currentTimeMillis();
		this.outStream = output;
		this.inStream = input;
		sets = new HashMap<>();
		try (BufferedReader br = new BufferedReader(new FileReader("results.txt"))) {
			String line;
			while ((line = br.readLine()) != null) {
				int delim = line.indexOf(':');
				if (delim != -1) {
					sets.put(line.substring(0,delim), line.substring(delim+1));
				}
				if (line.equalsIgnoreCase("REGRETS")) {
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		flopColex = new HashMap<>();
		try (BufferedReader br = new BufferedReader(new FileReader("/Users/kundanc/Coding/pokerbots/data/FlopHands.txt"))) {
			String line;
			int i = 0;
			while ((line = br.readLine()) != null) {
				flopColex.put(line, i++);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		flopBuckets = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader("/Users/kundanc/Coding/pokerbots/data/FlopBucketsEncoded.txt"))) {
			String line;
			while ((line = br.readLine()) != null) {
				flopBuckets.add(Integer.parseInt(line));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		flopDiscards = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader("/Users/kundanc/Coding/pokerbots/data/FlopDiscardsEncoded.txt"))) {
			String line;
			while ((line = br.readLine()) != null) {
				flopDiscards.add(Integer.parseInt(line));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		riverCenters = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader("/Users/kundanc/Coding/pokerbots/data/RiverCenters.txt"))) {
			String line;
			while ((line = br.readLine()) != null) {
				ArrayList<Double> point = new ArrayList<>();
				String words[] = line.split("\t");
				for (String word : words) {
					point.add(Double.parseDouble(word));
					riverCenters.add(point);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		turnData = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader("/Users/kundanc/Coding/pokerbots/data/TurnDataEncoded.txt"))) {
			String line;
			while ((line = br.readLine()) != null) {
				turnData.add(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("SETUP TIME " + (System.currentTimeMillis() - time));
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
					int index = 1;
					pot = Integer.parseInt(list[index++]);
					int numBoard = Integer.parseInt(list[index++]);
					ArrayList<Card> newboard = new ArrayList<>();
					for (int i = 0; i < numBoard; ++i) {
						newboard.add(new Card(list[index++], 1));
					}
					int numHistory = Integer.parseInt(list[index++]);
					ArrayList<String> history = new ArrayList<>();
					for (int i = 0; i < numHistory; ++i) {
						history.add(list[index++]);
					}
					int numActions= Integer.parseInt(list[index++]);
					ArrayList<String> actions = new ArrayList<>();
					for (int i = 0; i < numActions; ++i) {
						actions.add(list[index++]);
					}
					updateHistory(history);
					if (numBoard != board.size()) {
						board = newboard;
						roundHistory.clear();
						roundContribution = 0;
						turn++;
					}
					String move = determineMove();
					makeMove(move, actions);
				} else if ("REQUESTKEYVALUES".compareToIgnoreCase(type) == 0) {
					// At the end, engine will allow bot to send key/value pairs to store.
					// FINISH indicates no more to store.
					outStream.println("FINISH");
				} else if ("NEWHAND".compareToIgnoreCase(type) == 0) {
					hand = new ArrayList<>();
					hand.add(new Card(list[3], 0));
					hand.add(new Card(list[4], 0));
					if (list[2] == "true") {
						roundContribution = 1;
					} else {
						roundContribution = 2;
					}
					handHistory = new ArrayList<>();
					roundHistory = new ArrayList<>();
					board = new ArrayList<>();
					pot = 0;
					turn = 0;
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
			System.out.println("Encountered problem shutting down connections");
			e.printStackTrace();
		}
	}

	void translate(ArrayList<Card> cards) {
		Collections.sort(cards, new RankComparator());
		Collections.sort(cards, new HandComparator());
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

	void updateHistory(ArrayList<String> pastActions) {
		for (String action : pastActions) {
			String[] words = action.split(":");
			if (words[0].equalsIgnoreCase("WIN")) {

			} else if (words[0].equalsIgnoreCase("TIE")) {

			} else if (words[0].equalsIgnoreCase("SHOW")) {

			} else if (words[0].equalsIgnoreCase("REFUND")) {

			} else if (words[0].equalsIgnoreCase("RAISE")) {
				if (turn == 0) {
					if (roundHistory.size() == 0) {
						handHistory.add("RAISE167");
						roundHistory.add("RAISE167");
					} else {
						handHistory.add("RAISE100");
						roundHistory.add("RAISE100");
					}
				}
				System.out.println("UPDATING RAISE "+ handHistory);
			} else if (words[0].equalsIgnoreCase("DISCARD")) {
				handHistory.add("CHECK");
				roundHistory.add("CHECK");
			} else if (words[0].equalsIgnoreCase("POST")) {

			} else if (words[0].equalsIgnoreCase("FOLD")) {
				handHistory.add("FOLD");
				roundHistory.add("FOLD");
			} else if (words[0].equalsIgnoreCase("DEAL")) {

			} else if (words[0].equalsIgnoreCase("CHECK")) {
				handHistory.add("CHECK");
				roundHistory.add("CHECK");
			} else if (words[0].equalsIgnoreCase("CALL")) {
				handHistory.add("CALL");
				roundHistory.add("CALL");
			} else if (words[0].equalsIgnoreCase("BET")) {
				handHistory.add("BET");
				roundHistory.add("BET");
			}
		}
	}

	double distance(ArrayList<Double> a, ArrayList<Double> b) {
		double total = 0;
		for (int i = 0; i < a.size(); ++i) {
			total += (a.get(i)-b.get(i))*(a.get(i)-b.get(i));
		}
		return total;
	}

	String key() {
		ArrayList<Card> cards = new ArrayList<>();
		for (Card c : hand) {
			cards.add(new Card(c));
		}
		for (Card c : board) {
			cards.add(new Card(c));
		}
		translate(cards);

		String key = "";
		String cardset = "";
		for (Card c : cards) {
			cardset += c.toString();
		}
		if (board.size() == 0) {
			key += cardset;
		} else if (board.size() == 3) {
			key += flopBuckets.get(flopColex.get(cardset));
		} else if (board.size() == 4) {
			Card end = cards.remove(cards.size()-1);
			translate(cards);
			String flopkey = "";
			for (Card c : cards) {
				flopkey += c.toString();
			}
			int row = flopColex.get(flopkey);
			int col = 3*(4*end.rank+end.suit);
			key += turnData.get(row).charAt(col+1);
			key += turnData.get(row).charAt(col+2);
		} else if (board.size() == 5) {
			//TODO dead
			ArrayList<Double> distances = new ArrayList<>();
			double eq;
			eq = Calculator.calc(cardset.substring(0,4)+"23s,24s,25s,26s,27s,34s,35s,36s,37s,45s,46s,32o,43o,42o,54o,53o,52o,65o,64o,63o,62o,74o,73o,72o,83o,82o"
					, cardset.substring(4), "", 1000000).getEv().get(0);
			distances.add(eq);
			eq = Calculator.calc(cardset.substring(0,4)+"28s,29s,2Ts,38s,39s,47s,48s,49s,75o,85o,84o,95o,94o,93o,92o,T5o,T4o,T3o,T2o,J3o,J2o"
					, cardset.substring(4), "", 1000000).getEv().get(0);
			distances.add(eq);
			eq = Calculator.calc(cardset.substring(0,4)+"3Ts,4Ts,56s,57s,58s,59s,5Ts,67s,68s,69s,6Ts,78s,79s,89s,67o,68o,69o,6To,78o,79o,7To,89o,8To"
					, cardset.substring(4), "", 1000000).getEv().get(0);
			distances.add(eq);
			eq = Calculator.calc(cardset.substring(0,4)+"22,J2s,J3s,J4s,J5s,J6s,Q2s,Q3s,Q4s,Q5s,K2s,J4o,J5o,J6o,J7o,Q2o,Q3o,Q4o,Q5o,Q6o,Q7o,K2o,K3o,K4o"
					, cardset.substring(4), "", 1000000).getEv().get(0);
			distances.add(eq);
			eq = Calculator.calc(cardset.substring(0,4)+"6Qs,7Ts,7Js,7Qs,8Ts,8Js,8Qs,9Ts,9Js,9Qs,TJs,T9o,J8o,J9o,JTo,Q8o,Q9o,QTo,QJo"
					, cardset.substring(4), "", 1000000).getEv().get(0);
			distances.add(eq);
			eq = Calculator.calc(cardset.substring(0,4)+"33,44,55,K3s,K4s,K5s,K6s,K7s,K8s,A2s,A3s,A4s,A5s,A6s,K5o,K6o,K7o,K8o,K9o,A2o,A3o,A4o,A5o,A6o,A7o,A8o"
					, cardset.substring(4), "", 1000000).getEv().get(0);
			distances.add(eq);
			eq = Calculator.calc(cardset.substring(0,4)+"66,77,QTs,QJs,K9s,KTs,KJs,KQs,A7s,A8s,A9s,ATs,AJs,AQs,AKs,KTo,KJo,KQo,A9o,ATo,AJo,AQo,AKo"
					, cardset.substring(4), "", 1000000).getEv().get(0);
			distances.add(eq);
			eq = Calculator.calc(cardset.substring(0,4)+"88,99,TT,JJ,QQ,KK,AA"
					, cardset.substring(4), "", 1000000).getEv().get(0);
			distances.add(eq);
			double minDist = Double.POSITIVE_INFINITY;
			int bucket = 0;
			for (int i = 0; i < riverCenters.size(); ++i) {
				double dist = distance(distances, riverCenters.get(i));
				if (dist < minDist) {
					minDist = dist;
					bucket = i;
				}
			}
			key += Integer.toString(bucket);
		}
		for (String s : handHistory) {
			key += "_" + s;
		}
		return key;
	}

	String determineMove() {
		String key = key();
		String val = sets.get(key);
		if (val != null) {
			String words[] = val.split(" ");
			String actions[] = words[0].split("_");
			ArrayList<Double> probs = new ArrayList<>();
			double total = 0;
			for (int i = 1; i < words.length; ++i) {
				double value = Double.parseDouble(words[i]);
				total += value;
				probs.add(value);
			}
			double rand = Math.random();
			double counter = 0;
			for (int i = 0; i < probs.size(); ++i) {
				counter += probs.get(i) / total;
				if (rand < counter) {
					return actions[i];
				}
			}
			return actions[actions.length - 1];
		} else {
			System.out.println("ERROR: KEY " + key + " NOT FOUND");
			System.out.println("ROUND HISTORY "+roundHistory);
			System.out.println("HAND HISTORY "+roundHistory);
			//TODO better correction; find closest state
			if (roundHistory.get(roundHistory.size()-1).startsWith("RAISE")) {
				roundHistory.remove(roundHistory.size()-1);
				handHistory.remove(handHistory.size()-1);
				return determineMove();
			}
			return "ERROR";
		}
	}

	void makeMove(String move, ArrayList<String> possibleActions) {
		if (move.startsWith("RAISE")) {
			double perc = Double.parseDouble(move.substring(5));
			int amount = (int)(Math.round((pot*perc)/100)) + roundContribution;
			for (String action : possibleActions) {
				String words[] = action.split(":");
				if (words[0].equalsIgnoreCase("RAISE")) {
					int min = Integer.parseInt(words[1]);
					int max = Integer.parseInt(words[2]);
					if (amount < min) {
						roundContribution += min;
						outStream.println("RAISE:"+min);
					} else if (amount > max) {
						roundContribution += max;
						outStream.println("RAISE:"+max);
					} else {
						roundContribution += amount;
						outStream.println("RAISE:"+amount);
					}
				}
			}
		} else if (move.startsWith("BET")) {
			double perc = Double.parseDouble(move.substring(3));
			int amount = (int)(Math.round((pot*perc)/100)) + roundContribution;
			for (String action : possibleActions) {
				String words[] = action.split(":");
				if (words[0].equalsIgnoreCase("BET")) {
					int min = Integer.parseInt(words[1]);
					int max = Integer.parseInt(words[2]);
					if (amount < min) {
						roundContribution += min;
						outStream.println("BET:"+min);
					} else if (amount > max) {
						roundContribution += max;
						outStream.println("BET:"+max);
					} else {
						roundContribution += amount;
						outStream.println("BET:"+amount);
					}
				}
			}
		}
		else if (move.equalsIgnoreCase("CALL")) {
			outStream.println("CALL");
		} else if (move.equalsIgnoreCase("FOLD")) {
			outStream.println("FOLD");
		} else if (move.equalsIgnoreCase("CHECK")) {
			outStream.println("CHECK");
		} else {
			for(String action : possibleActions) {
				if (action.compareToIgnoreCase("CHECK") == 0) {
					outStream.println("CHECK");
				} else if (action.compareToIgnoreCase("CALL") == 0){
					outStream.println("CALL");
				}
			}
			System.out.println("ERROR: ATTEMPING TO DO MOVE "+move);
		}
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