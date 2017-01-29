package pokerbots.player;

import java.io.*;
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
	private ArrayList<Card> dead;
	private HashMap<String, String> sets;
	private HashMap<String, Integer> flopColex;
	private ArrayList<Integer> flopBuckets;
	private ArrayList<Integer> flopDiscards;
	private ArrayList<String> turnData;
	private ArrayList<ArrayList<Double>> riverCenters;
	private ArrayList<String> handHistory;
	private ArrayList<String> roundHistory;

	private int pot;
	private int turn;
	private boolean discardState;
	private String ourName;
	private String oppName;
	private int ourTotalContribution;
	private int oppTotalContribution;
	private int ourRoundContribution;
	private int oppRoundContribution;
	private boolean fakeCheck = false;
	private boolean fakeCall = false;

	private int startingStack = 200;

	public Player(PrintWriter output, BufferedReader input) {
		this.outStream = output;
		this.inStream = input;
		init();
	}


	public void init() {
		sets = new HashMap<>();
		try (BufferedReader br = new BufferedReader(new FileReader("data/results.txt"))) {
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
		try (BufferedReader br = new BufferedReader(new FileReader("data/FlopHands.txt"))) {
			String line;
			int i = 0;
			while ((line = br.readLine()) != null) {
				flopColex.put(line, i++);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		flopBuckets = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader("data/FlopBucketsEncoded.txt"))) {
			String line;
			while ((line = br.readLine()) != null) {
				flopBuckets.add(Integer.parseInt(line));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		flopDiscards = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader("data/FlopDiscardsEncoded.txt"))) {
			String line;
			while ((line = br.readLine()) != null) {
				flopDiscards.add(Integer.parseInt(line));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		riverCenters = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader("data/RiverCenters.txt"))) {
			String line;
			while ((line = br.readLine()) != null) {
				ArrayList<Double> point = new ArrayList<>();
				String words[] = line.split("\t");
				for (String word : words) {
					point.add(Double.parseDouble(word));
				}
				riverCenters.add(point);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		turnData = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader("data/TurnDataEncoded.txt"))) {
			String line;
			while ((line = br.readLine()) != null) {
				turnData.add(line);
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
					System.out.println(key() + " "+history);
					board = newboard;
					String move = determineMove();
					System.out.println("PREPROCESSED MOVE "+move);
					move = processMove(move);
					System.out.println("PROCESSED MOVE "+move);
					String action = makeMove(move, actions);
					outStream.println(action);
				} else if ("REQUESTKEYVALUES".compareToIgnoreCase(type) == 0) {
					// At the end, engine will allow bot to send key/value pairs to store.
					// FINISH indicates no more to store.
					outStream.println("FINISH");
				} else if ("NEWHAND".compareToIgnoreCase(type) == 0) {
					hand = new ArrayList<>();
					hand.add(new Card(list[3], 0));
					hand.add(new Card(list[4], 0));
					if (list[2] == "true") {
						ourRoundContribution = 1;
						ourTotalContribution = 1;
						oppRoundContribution = 2;
						oppTotalContribution = 2;
					} else {
						ourRoundContribution = 2;
						ourTotalContribution = 2;
						oppRoundContribution = 1;
						oppTotalContribution = 1;
					}
					handHistory = new ArrayList<>();
					roundHistory = new ArrayList<>();
					board = new ArrayList<>();
					dead = new ArrayList<>();
					pot = 3;
					turn = 0;
					discardState = false;
					fakeCheck = false;
					fakeCall = false;
				} else if ("NEWGAME".compareToIgnoreCase(type) == 0) {
					ourName = list[1];
					oppName = list[2];
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
				int amount = Integer.parseInt(words[1]);
				int lastpot = ourTotalContribution + oppTotalContribution;
				double perc = 0;
				if (words[2].equalsIgnoreCase(ourName)) {
					perc = (double) (amount-ourRoundContribution) / (double) lastpot * 100;
				} else {
					perc = (double) (amount-oppRoundContribution) / (double) lastpot * 100;
				}
				if (perc <= 200) {
					double a = 0;
					double b = 200;
					double f = (b-perc)*(1+a) / ((b-a)*(1+perc));
					if (Math.random() < f) {
						fakeCall = true;
						interpretCall();
					} else {
						if (fakeCheck) {
							interpretBet();
							fakeCheck = false;
						} else {
							interpretRaise();
						}
					}
				} else {
					double a = 200;
					double b = ((startingStack-oppTotalContribution)-oppRoundContribution) / (double) lastpot * 100;
					double f = (b-perc)*(1+a) / ((b-a)*(1+perc));
					if (Math.random() < f) {
						if (fakeCheck) {
							interpretBet();
							fakeCheck = false;
						} else {
							interpretRaise();
						}
					} else {
						interpretAllin(false);
					}
				}
				if (words[2].equalsIgnoreCase(ourName)) {
					ourTotalContribution += amount - ourRoundContribution;
					ourRoundContribution = amount;
				} else {
					oppTotalContribution += amount - oppRoundContribution;
					oppRoundContribution = amount;
				}
			} else if (words[0].equalsIgnoreCase("DISCARD")) {
				if (words.length == 4) {
					if (hand.get(0).toString().equalsIgnoreCase(words[1])) {
						dead.add(new Card(hand.get(0)));
						hand.set(0, new Card(words[2], 0));
					} else if (hand.get(1).toString().equalsIgnoreCase(words[1])) {
						dead.add(new Card(hand.get(1)));
						hand.set(1, new Card(words[2], 0));
					}
				}
				interpretDcheck();
			} else if (words[0].equalsIgnoreCase("POST")) {

			} else if (words[0].equalsIgnoreCase("FOLD")) {
				handHistory.add("FOLD");
				roundHistory.add("FOLD");
			} else if (words[0].equalsIgnoreCase("DEAL")) {
				roundHistory.clear();
				ourRoundContribution = 0;
				oppRoundContribution = 0;
				turn++;
				if(words[1].equalsIgnoreCase("FLOP") || words[1].equalsIgnoreCase("TURN")) {
					discardState = true;
				}
			} else if (words[0].equalsIgnoreCase("CHECK")) {
				if (discardState) {
					interpretDcheck();
				} else {
					interpretCheck();
				}
			} else if (words[0].equalsIgnoreCase("CALL")) {
				int amount = Math.max(ourRoundContribution, oppRoundContribution);
				ourTotalContribution += amount - ourRoundContribution;
				oppTotalContribution += amount - oppRoundContribution;
				ourRoundContribution = amount;
				oppRoundContribution = amount;
				if (fakeCheck) {
					interpretCheck();
					fakeCheck = false;
				}
				if (fakeCall) {
					fakeCall = false;
				} else {
					interpretCall();
				}
			} else if (words[0].equalsIgnoreCase("BET")) {
				int amount = Integer.parseInt(words[1]);
				int lastpot = ourTotalContribution + oppTotalContribution;
				double perc = 0;
				if (words[2].equalsIgnoreCase(ourName)) {
					perc = (double) (amount-ourRoundContribution) / (double) lastpot * 100;
				} else {
					perc = (double) (amount-oppRoundContribution) / (double) lastpot * 100;
				}
				if (perc <= 66) {
					double a = 0;
					double b = 66;
					double f = (b-perc)*(1+a) / ((b-a)*(1+perc));
					if (Math.random() < f) {
						fakeCheck = true;
						interpretCheck();
					} else {
						interpretBet();
					}
				} else {
					double a = 66;
					double b = ((startingStack-oppTotalContribution)-oppRoundContribution) / (double) lastpot * 100;
					double f = (b-perc)*(1+a) / ((b-a)*(1+perc));
					if (Math.random() < f) {
						interpretBet();
					} else {
						interpretAllin(true);
					}
				}

				if (words[2].equalsIgnoreCase(ourName)) {
					ourTotalContribution += amount - ourRoundContribution;
					ourRoundContribution = amount;
				} else {
					oppTotalContribution += amount - oppRoundContribution;
					oppRoundContribution = amount;
				}
			}
		}
	}

	void interpretRaise() {
		if (turn == 0) {
			if (roundHistory.size() == 0) {
				handHistory.add("RAISE167");
				roundHistory.add("RAISE167");
			} else if (roundHistory.size() == 1) {
				if (roundHistory.get(0) == "RAISE167") {
					handHistory.add("RAISE238");
					roundHistory.add("RAISE238");
				} else if (roundHistory.get(0) == "CALL") {
					handHistory.add("RAISE167");
					roundHistory.add("RAISE167");
				}
			}
		} else {
			handHistory.add("RAISE200");
			roundHistory.add("RAISE200");
		}
	}

	void interpretBet() {
		handHistory.add("BET066");
		roundHistory.add("BET066");
	}

	void interpretCall() {
		handHistory.add("CALL");
		roundHistory.add("CALL");
	}

	void interpretCheck() {
		handHistory.add("CHECK");
		roundHistory.add("CHECK");
	}

	void interpretDcheck() {
		handHistory.add("DCHECK");
		roundHistory.add("DCHECK");
		if (roundHistory.size() == 2) {
			discardState = false;
		}
	}

	void interpretAllin(boolean bet) {
		if (turn == 0) {
			handHistory.clear();
			roundHistory.clear();
		} else {
			while (!handHistory.get(handHistory.size()-1).equals("DCHECK")) {
				handHistory.remove(handHistory.size() - 1);
				roundHistory.clear();
			}
		}
		if (bet) {
			handHistory.add("RAISE99999");
			roundHistory.add("RAISE99999");
		} else {
			handHistory.add("RAISE99999");
			roundHistory.add("RAISE99999");
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
			if (turnData.get(row).charAt(col) != '0') {
				key += turnData.get(row).charAt(col);
			}
			key += turnData.get(row).charAt(col+1);
		} else if (board.size() == 5) {
			String deadcards = "";
			for (Card c : dead) {
				deadcards += c.toString();
			}
			String handcards = "";
			for (Card c : hand) {
				handcards += c.toString();
			}
			String boardcards = "";
			for (Card c : board) {
				boardcards += c.toString();
			}
			ArrayList<Double> distances = new ArrayList<>();
			double eq;
			eq = Calculator.calc(handcards+":23s,24s,25s,26s,27s,34s,35s,36s,37s,45s,46s,32o,43o,42o,54o,53o,52o,65o,64o,63o,62o,74o,73o,72o,83o,82o"
					, boardcards, deadcards, 1000000).getEv().get(0);
			distances.add(eq);
			eq = Calculator.calc(handcards+":28s,29s,2Ts,38s,39s,47s,48s,49s,75o,85o,84o,95o,94o,93o,92o,T5o,T4o,T3o,T2o,J3o,J2o"
					, boardcards, deadcards, 1000000).getEv().get(0);
			distances.add(eq);
			eq = Calculator.calc(handcards+":3Ts,4Ts,56s,57s,58s,59s,5Ts,67s,68s,69s,6Ts,78s,79s,89s,67o,68o,69o,6To,78o,79o,7To,89o,8To"
					, boardcards, deadcards, 1000000).getEv().get(0);
			distances.add(eq);
			eq = Calculator.calc(handcards+":22,J2s,J3s,J4s,J5s,J6s,Q2s,Q3s,Q4s,Q5s,K2s,J4o,J5o,J6o,J7o,Q2o,Q3o,Q4o,Q5o,Q6o,Q7o,K2o,K3o,K4o"
					, boardcards, deadcards, 1000000).getEv().get(0);
			distances.add(eq);
			eq = Calculator.calc(handcards+":6Qs,7Ts,7Js,7Qs,8Ts,8Js,8Qs,9Ts,9Js,9Qs,TJs,T9o,J8o,J9o,JTo,Q8o,Q9o,QTo,QJo"
					, boardcards, deadcards, 1000000).getEv().get(0);
			distances.add(eq);
			eq = Calculator.calc(handcards+":33,44,55,K3s,K4s,K5s,K6s,K7s,K8s,A2s,A3s,A4s,A5s,A6s,K5o,K6o,K7o,K8o,K9o,A2o,A3o,A4o,A5o,A6o,A7o,A8o"
					, boardcards, deadcards, 1000000).getEv().get(0);
			distances.add(eq);
			eq = Calculator.calc(handcards+":66,77,QTs,QJs,K9s,KTs,KJs,KQs,A7s,A8s,A9s,ATs,AJs,AQs,AKs,KTo,KJo,KQo,A9o,ATo,AJo,AQo,AKo"
					, boardcards, deadcards, 1000000).getEv().get(0);
			distances.add(eq);
			eq = Calculator.calc(handcards+":88,99,TT,JJ,QQ,KK,AA"
					, boardcards, deadcards, 1000000).getEv().get(0);
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
//			//TODO better correction; find closest state
//			if (roundHistory.get(roundHistory.size()-1).startsWith("RAISE")) {
//				roundHistory.remove(roundHistory.size()-1);
//				handHistory.remove(handHistory.size()-1);
//				return determineMove();
//			}
			return "ERROR";
		}
	}

	String processMove(String move) {
		if (fakeCheck) {
			if (move.startsWith("BET")) {
				move = "RAISE"+move.substring(5);
			} else if (move.equalsIgnoreCase("CHECK")) {
				move = "CALL";
			}
		}
		if (fakeCall) {
			move = "CALL";
		}
		if (move.equalsIgnoreCase("CALL")) {
			if (handHistory.size() > 0 && handHistory.get(handHistory.size()-1).equalsIgnoreCase("RAISE99999")) {
				move = "RAISE99999";
			}
		}
		return move;
	}

	String makeMove(String move, ArrayList<String> possibleActions) {
		if (move.startsWith("RAISE")) {
			double perc = Double.parseDouble(move.substring(5));
			int amount = (int) (Math.round((pot * perc) / 100)) + ourRoundContribution;
			for (String action : possibleActions) {
				String words[] = action.split(":");
				if (words[0].equalsIgnoreCase("RAISE")) {
					int min = Integer.parseInt(words[1]);
					int max = Integer.parseInt(words[2]);
					if (amount < min) {
						return "RAISE:" + min;
					} else if (amount > max) {
						return "RAISE:" + max;
					} else {
						return "RAISE:" + amount;
					}
				}
			}
		} else if (move.startsWith("BET")) {
			double perc = Double.parseDouble(move.substring(3));
			int amount = (int) (Math.round((pot * perc) / 100)) + ourRoundContribution;
			for (String action : possibleActions) {
				String words[] = action.split(":");
				if (words[0].equalsIgnoreCase("BET")) {
					int min = Integer.parseInt(words[1]);
					int max = Integer.parseInt(words[2]);
					if (amount < min) {
						return "BET:" + min;
					} else if (amount > max) {
						return "BET:" + max;
					} else {
						return "BET:" + amount;
					}
				}
			}
		} else if (move.equalsIgnoreCase("CALL")) {
			return "CALL";
		} else if (move.equalsIgnoreCase("FOLD")) {
			return "FOLD";
		} else if (move.equalsIgnoreCase("CHECK")) {
			return "CHECK";
		} else if (move.equalsIgnoreCase("DCHECK")) {
			for (String action : possibleActions) {
				if (action.startsWith("DISCARD")) {
					int discard = 0;
					ArrayList<Card> cards = new ArrayList<>();
					for (Card c : hand) {
						cards.add(new Card(c));
					}
					for (Card c : board) {
						cards.add(new Card(c));
					}
					translate(cards);
					if (turn == 1) {
						String cardset = "";
						for (Card c : cards) {
							cardset += c.toString();
						}
						discard = flopDiscards.get(flopColex.get(cardset));
					} else if (turn == 2) {
						Card end = cards.remove(cards.size()-1);
						translate(cards);
						String flopkey = "";
						for (Card c : cards) {
							flopkey += c.toString();
						}
						int row = flopColex.get(flopkey);
						int col = 3*(4*end.rank+end.suit);
						char val = turnData.get(row).charAt(col + 2);
						if (val == 'N') {
							discard = 0;
						} else if (val == 'L') {
							discard = 1;
						} else if (val == 'R') {
							discard = 2;
						}
					}
					if (discard == 0) {
						return "CHECK";
					} else if (discard == 1) {
						ArrayList<Card> copy = new ArrayList<>();
						for (Card c : hand) {
							copy.add(new Card(c));
						}
						Collections.sort(copy, new RankComparator());
						return "DISCARD:" + copy.get(0).toString();
					} else if (discard == 2) {
						ArrayList<Card> copy = new ArrayList<>();
						for (Card c : hand) {
							copy.add(new Card(c));
						}
						Collections.sort(copy, new RankComparator());
						return "DISCARD:" + copy.get(1).toString();
					}
				}
			}
		}
		for (String action : possibleActions) {
			if (action.startsWith("DISCARD")) {
				return makeMove("DCHECK", possibleActions);
			}
		}
		System.out.println("ERROR: ATTEMPING TO DO MOVE " + move);
		for (String action : possibleActions) {
			if (action.compareToIgnoreCase("CHECK") == 0) {
				return "CHECK";
			} else if (action.compareToIgnoreCase("CALL") == 0) {
				return "CALL";
			}
		}
		return "CHECK";
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
