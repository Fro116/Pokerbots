#include <iostream>
#include <string>
#include <fstream>
#include <vector>
#include <algorithm>
#include <sstream>
#include <memory>
#include <cassert>
#include <limits>
#include <chrono>
#include <set>
#include <unordered_set>
#include <cstdlib>
#include <tuple>
#include <map>
#include <unordered_map>
#include <thread>
#include <mutex>
#include <boost/algorithm/string.hpp>
#include <pbots_calc.h>

#include "Random.h"
#include "Card.h"

std::string ranks[] = {"2","3","4","5","6","7","8","9","T","J","Q","K","A"};
std::string suits[] = {"c","d","h","s"};

std::vector<std::vector<int>> genHands() {
  std::vector<std::vector<int>> ans;
  for (int a = 0; a < 52; ++a) {
    for (int b = 0; b < a; ++b) {
      std::vector<int> board;
      board.push_back(a);
      board.push_back(b);
      ans.push_back(board);	  
    }
  }
  return ans;
}

std::vector<std::vector<int>> genFlops() {
  std::vector<std::vector<int>> ans;
  for (int a = 0; a < 52; ++a) {
    for (int b = 0; b < a; ++b) {
      for (int c = 0; c < b; ++c) {
	  std::vector<int> board;
	  board.push_back(a);
	  board.push_back(b);
	  board.push_back(c);
	  ans.push_back(board);	  
      }
    }
  }
  return ans;
}

std::vector<std::vector<int>> genTurns() {
  std::vector<std::vector<int>> ans;
  for (int a = 0; a < 52; ++a) {
    for (int b = 0; b < a; ++b) {
      for (int c = 0; c < b; ++c) {
	for (int d = 0; d < c; ++d) {	
	  std::vector<int> board;
	  board.push_back(a);
	  board.push_back(b);
	  board.push_back(c);
	  board.push_back(d);	  
	  ans.push_back(board);
	}
      }
    }
  }
  return ans;
}

std::vector<std::vector<int>> genRivers() {
  std::vector<std::vector<int>> ans;
  for (int a = 0; a < 52; ++a) {
    for (int b = 0; b < a; ++b) {
      for (int c = 0; c < b; ++c) {
	for (int d = 0; d < c; ++d) {
	  for (int e = 0; e < d; ++e) {		  
	    std::vector<int> board;
	    board.push_back(a);
	    board.push_back(b);
	    board.push_back(c);
	    board.push_back(d);
	    board.push_back(e);	  	  
	    ans.push_back(board);
	  }
	}
      }
    }
  }
  return ans;
}

std::unordered_set<std::string> abstractBoards(std::vector<std::vector<int>> boardFields, std::vector<std::vector<int>> handFields) {
  int i = 0;
  std::unordered_set<std::string> boards;
  for (auto bfield : boardFields) {
    std::unordered_set<int> dups;
    for (auto card : bfield) {
      dups.insert(card);
    }
    for (auto hfield : handFields) {
      bool areDups = false;
      for (auto card : hfield) {
	if (dups.find(card) != dups.end()) {
	  areDups = true;
	}
      }
      if (!areDups) {
	std::vector<std::tuple<int, int, int>> cards;    
	for (auto card : bfield) {
	  cards.push_back(std::make_tuple(card/4,card%4, 1));
	}
	for (auto card : hfield) {
	  cards.push_back(std::make_tuple(card/4,card%4, 0));
	}      
	std::stable_sort(cards.begin(), cards.end(), [](auto &left, auto &right) {return std::get<0>(left) < std::get<0>(right);});
	std::stable_sort(cards.begin(), cards.end(), [](auto &left, auto &right) {return std::get<2>(left) < std::get<2>(right);});
	std::unordered_map<int, int> suitMapping;
	int openSuit = 0;
	for (auto& card : cards) {
	  if (suitMapping.find(std::get<1>(card)) == suitMapping.end()) {
	    suitMapping[std::get<1>(card)] = openSuit;	      
	    std::get<1>(card) = openSuit;
	    openSuit++;
	  } else {
	    std::get<1>(card) = suitMapping[std::get<1>(card)];
	  }
	}
	std::stable_sort(cards.begin(), cards.end(), [](auto &left, auto &right) {return std::get<1>(left) < std::get<1>(right);});
	std::stable_sort(cards.begin(), cards.end(), [](auto &left, auto &right) {return std::get<0>(left) < std::get<0>(right);});
	std::stable_sort(cards.begin(), cards.end(), [](auto &left, auto &right) {return std::get<2>(left) < std::get<2>(right); });
      
	std::string board = "";
	for (auto card : cards) {
	  if (board != "") {
	    board += " ";
	  }
	  board += ranks[std::get<0>(card)];
	  board += suits[std::get<1>(card)];	    
	}
	if (boards.find(board) == boards.end()) {
	  boards.insert(board);
	  std::cout << board << std::endl;
	}
      }
    }
    std::cerr << i << " " << boards.size() << std::endl;
    i++;          
  }
   return boards;
}

std::string translate(std::vector<Card> bfield, std::vector<Card> hfield) {
  std::vector<std::tuple<int, int, int>> cards;    
  for (auto card : bfield) {
    cards.push_back(std::make_tuple(card.rank(),card.suit(), 1));
  }
  for (auto card : hfield) {
    cards.push_back(std::make_tuple(card.rank(),card.suit(), 0));
  }      
  std::stable_sort(cards.begin(), cards.end(), [](auto &left, auto &right) {return std::get<0>(left) < std::get<0>(right);});
  std::stable_sort(cards.begin(), cards.end(), [](auto &left, auto &right) {return std::get<2>(left) < std::get<2>(right);});
  std::unordered_map<int, int> suitMapping;
  int openSuit = 0;
  for (auto& card : cards) {
    if (suitMapping.find(std::get<1>(card)) == suitMapping.end()) {
      suitMapping[std::get<1>(card)] = openSuit;	      
      std::get<1>(card) = openSuit;
      openSuit++;
    } else {
      std::get<1>(card) = suitMapping[std::get<1>(card)];
    }
  }
  std::stable_sort(cards.begin(), cards.end(), [](auto &left, auto &right) {return std::get<1>(left) < std::get<1>(right);});
  std::stable_sort(cards.begin(), cards.end(), [](auto &left, auto &right) {return std::get<0>(left) < std::get<0>(right);});
  std::stable_sort(cards.begin(), cards.end(), [](auto &left, auto &right) {return std::get<2>(left) < std::get<2>(right); });
      
  std::string board = "";
  for (auto card : cards) {
    board += ranks[std::get<0>(card)];
    board += suits[std::get<1>(card)];	    
  }
  return board;
}

std::vector<Card> toCards(std::string cards) {
  std::vector<Card> col;
  for (std::size_t i = 0; i < cards.size(); i += 2) {
    col.push_back(Card(cards.substr(i,2)));
  }
  return col;
}

double distance(std::vector<double> a, std::vector<double> b) {
  assert(a.size() == b.size());
  double dist = 0;
  for (std::size_t i = 0; i < a.size(); ++i) {
    dist += (a[i] - b[i])*(a[i] - b[i]);
  }
  return dist;
}

void cluster(std::string distancesFile, std::string centersFile) {
  std::vector<std::vector<double>> centers;
  {
    std::string line;
    std::ifstream file(centersFile);
    while (std::getline(file, line)) {
      std::vector<double> center;    
      std::vector<std::string> words;
      boost::split(words, line, boost::is_any_of("\t "));
      for (std::string word : words) {
	center.push_back(std::stod(word));
      }
      centers.push_back(center);
    }
  }
  {
    std::string line;
    std::ifstream file(distancesFile);
    while (std::getline(file, line)) {
      std::string name;          
      std::vector<double> point;    
      std::vector<std::string> words;
      boost::trim(line);      
      boost::split(words, line, boost::is_any_of("\t "));
      for (std::size_t i = 0; i < words.size(); ++i) {
	if (i == 0) {
	  name = words[i];
	} else {
	  point.push_back(std::stod(words[i]));
	}
      }
      double minDist = std::numeric_limits<double>::max();
      int bucket = 0;
      for (std::size_t i = 0; i < centers.size(); ++i) {
	double dist = distance(point, centers[i]);
	if (dist < minDist) {
	  minDist = dist;
	  bucket = i;
	}
      }
      std::cout << name << " " << std::to_string(bucket) << std::endl;
    }
  }  
}

void computeDistances(std::string handsFile) {
  std::ifstream file(handsFile);
  std::string line;
  Results* res = alloc_results();
  std::string dead = "";
  while (std::getline(file, line)) {
    std::string hand = line.substr(0,4);
    std::string board = line.substr(4);
    std::cout << hand << board;
    calc(const_cast<char*>((hand+":"+"23s,24s,25s,26s,27s,34s,35s,36s,37s,45s,46s,32o,43o,42o,54o,53o,52o,65o,64o,63o,62o,74o,73o,72o,83o,82o").c_str()), const_cast<char*>(board.c_str()), const_cast<char*>(dead.c_str()), 1000000, res);
    std::cout << " " << *(res->ev);
    calc(const_cast<char*>((hand+":"+"28s,29s,2Ts,38s,39s,47s,48s,49s,75o,85o,84o,95o,94o,93o,92o,T5o,T4o,T3o,T2o,J3o,J2o").c_str()), const_cast<char*>(board.c_str()), const_cast<char*>(dead.c_str()), 1000000, res);
    std::cout << " " << *(res->ev);
    calc(const_cast<char*>((hand+":"+"3Ts,4Ts,56s,57s,58s,59s,5Ts,67s,68s,69s,6Ts,78s,79s,89s,67o,68o,69o,6To,78o,79o,7To,89o,8To").c_str()), const_cast<char*>(board.c_str()), const_cast<char*>(dead.c_str()), 1000000, res);
    std::cout << " " << *(res->ev);
    calc(const_cast<char*>((hand+":"+"22,J2s,J3s,J4s,J5s,J6s,Q2s,Q3s,Q4s,Q5s,K2s,J4o,J5o,J6o,J7o,Q2o,Q3o,Q4o,Q5o,Q6o,Q7o,K2o,K3o,K4o").c_str()), const_cast<char*>(board.c_str()), const_cast<char*>(dead.c_str()), 1000000, res);
    std::cout << " " << *(res->ev);
    calc(const_cast<char*>((hand+":"+"6Qs,7Ts,7Js,7Qs,8Ts,8Js,8Qs,8Ts,9Ts,9Js,9Qs,TJs,T9o,J8o,J9o,JTo,Q8o,Q9o,QTo,QJo").c_str()), const_cast<char*>(board.c_str()), const_cast<char*>(dead.c_str()), 1000000, res);
    std::cout << " " << *(res->ev);
    calc(const_cast<char*>((hand+":"+"33,44,55,K3s,K4s,K5s,K6s,K7s,K8s,A2s,A3s,A4s,A5s,A6s,K5o,K6o,K7o,K8o,K9o,A2o,A3o,A4o,A5o,A6o,A7o,A8o").c_str()), const_cast<char*>(board.c_str()), const_cast<char*>(dead.c_str()), 1000000, res);
    std::cout << " " << *(res->ev);
    calc(const_cast<char*>((hand+":"+"66,77,QTs,QJs,K9s,KTs,KJs,KQs,A7s,A8s,A9s,ATs,AJs,AQs,AKs,KTo,KJo,KQo,A9o,ATo,AJo,AQo,AKo").c_str()), const_cast<char*>(board.c_str()), const_cast<char*>(dead.c_str()), 1000000, res);
    std::cout << " " << *(res->ev);
    calc(const_cast<char*>((hand+":"+"88,99,TT,JJ,QQ,KK,AA").c_str()), const_cast<char*>(board.c_str()), const_cast<char*>(dead.c_str()), 1000000, res);
    std::cout << " " << *(res->ev);
    std::cout << "\n";
  }
  free_results(res);       
}

void computeEquities(std::string handsFile) {
  std::ifstream file(handsFile);
  std::string line;
  std::string dead = "";
  while (std::getline(file, line)) {
    std::string hand = line.substr(0,4);
    std::string board = line.substr(4);
    std::cout << hand << board;
    Results* res = alloc_results();    
    calc(const_cast<char*>((hand+":"+"xx").c_str()), const_cast<char*>(board.c_str()), const_cast<char*>(dead.c_str()), 10000, res);
    std::cout << " " << *(res->ev);
    free_results(res);           
    std::cout << "\n";
  }
}

int cardNumber(std::string name) {
  int r = 0;
  int s = 0;
  std::string rank = name.substr(0,1);
  std::string suit = name.substr(1,1);
  for (int i = 0; i < 13; ++i) {
    if (rank == ranks[i]) {
      r = i;
      break;
    }
  }
  for (int i = 0; i < 4; ++i) {
    if (suit == suits[i]) {
      s = i;
      break;
    }
  }
  return 4*r+s;
}

void encodeTurnBuckets(std::string flopHandsFile, std::string turnBucketsFile, std::string turnDiscardsFile) {
  std::unordered_map<std::string, int> flops;
  std::unordered_map<std::string, std::string> turnDiscards;  
  std::vector<std::string> turnBuckets;
  std::string empty = "------------------------------------------------------------------------------------------------------------------------------------------------------------";
  {
    std::string line;
    std::ifstream file(flopHandsFile);
    int i = 0;
    while (std::getline(file, line)) {
      flops[line] = i++;
      turnBuckets.push_back(empty);
    }
  }
  std::cerr << "DONE FLOPS" << std::endl;
  {
    std::string line;
    std::ifstream file(turnDiscardsFile);
    while (std::getline(file, line)) {
      auto loc = line.find_first_of(' ');
      std::string key = line.substr(0, loc);
      std::string val = line.substr(loc+1);
      turnDiscards[key] = val;
    }
  }
  std::cerr << "DONE TURNS" << std::endl;  
  {
    int iter = 0;
    std::string line;
    std::ifstream file(turnBucketsFile);
    while (std::getline(file, line)) {
      auto loc = line.find_first_of(' ');
      std::string key = line.substr(0, loc);
      std::string discard = turnDiscards[key];
      std::string hand = key.substr(0,4);
      std::string board = key.substr(4,6);
      std::string flopkey = translate(toCards(board), toCards(hand));
      std::size_t row = static_cast<std::size_t>(flops[flopkey]);
      std::size_t col = static_cast<std::size_t>(3*cardNumber(key.substr(10)));      
      int bucket = std::stoi(line.substr(loc+1));
      std::string val = std::to_string(bucket)+discard;
      if (val.size() < 3) {
	val = "0"+val;
      }
      turnBuckets[row][col] = val[0];
      turnBuckets[row][col+1] = val[1];
      turnBuckets[row][col+2] = val[2];
      std::cerr << iter++ << std::endl;      
    }
  }
  for (std::string& str : turnBuckets) {
    if (str != empty) {
      std::cout << str << std::endl;
    } else {
      std::cout << "-" << std::endl;
    }
  }
}

Card getCard(std::vector<Card>& deck) {
  int index = Random::integer(0, deck.size()-1);
  Card c = deck[static_cast<std::size_t>(index)];
  deck.erase(deck.begin()+index);
  return c;
}

void genRandomTurns(int iterations) {
  std::vector<Card> deck;
  for (int rank = 0; rank < 13; ++rank) {
    for (int suit = 0; suit < 4; ++suit) {
      deck.push_back(Card(rank,suit));
    }
  }
  for (int i = 0; i < iterations; ++i) {
    auto copy = deck;
    for (int j = 0; j < 7; ++j) {
      std::cout << getCard(copy).toString() << " ";
    }
    std::cout << std::endl;
  }
}

std::string createRange(std::string hand) {
  std::string ans = "";
  for (int rank = 0; rank < 13; ++rank) {
    ans += hand.substr(0,1);
    ans += ranks[rank];
    ans += ",";	
  }
  ans.pop_back();
  return ans;
}

void chooseDiscards(std::string distancesFile, std::string equityFile, std::string handsFile) {
  std::unordered_map<std::string, double> equities;
  std::unordered_map<std::string, double> singleEquities;  
  {
    std::string line;
    std::ifstream file(distancesFile);
    while (std::getline(file, line)) {
      boost::trim(line);
      std::vector<std::string> words;
      boost::split(words, line, boost::is_any_of(" "));      
      std::string key = words[0];
      double eq = 0;
      double weights[] = {0.168929, 0.14178, 0.12368, 0.152338, 0.105581, 0.155354, 0.120664, 0.0316742};
      for (std::size_t i = 1; i < words.size(); ++i) {
  	eq += std::stod(words[i])*weights[i-1];
      }
      equities[key] = eq;
      // std::cout << line << std::endl;  
    }
  }
  {
    std::string line;
    std::ifstream file(equityFile);
    while (std::getline(file, line)) {
      boost::trim(line);      
      std::vector<std::string> words;
      boost::split(words, line, boost::is_any_of(" "));      
      std::string key = words[0];
      double eq = std::stod(words[1]);
      singleEquities[key] = eq;
    }
  }
  {
    std::ifstream file(handsFile);
    std::string line;
    // std::string error = "2c4d7c8c9cTc";
    while (std::getline(file, line)) {
      std::string hand = line.substr(0,4);
      std::string board = line.substr(4);
      double maxeq = 0;      
      // double maxeq = equities[line]/100;
      // double maxeq = equities[line] - 0.1; //Punish unpaired hands      
      std::string resp = "N";
      std::vector<Card> handc;
      for (std::size_t i = 0; 2*i < hand.size(); ++i) {
	handc.push_back(Card(hand.substr(2*i, 2)));
      }      
      std::vector<Card> boardc;
      for (std::size_t i = 0; 2*i < board.size(); ++i) {
	boardc.push_back(Card(board.substr(2*i, 2)));
      }
      std::vector<Card> handc1;      
      handc1.push_back(Card(hand.substr(0,2)));
      std::string key1 = translate(boardc,handc1);        
      std::vector<Card> handc2;      
      handc2.push_back(Card(hand.substr(2)));
      std::string key2 = translate(boardc,handc2);
      {
  	double b = singleEquities[key1];
	// if (line == error) {
	//   std::cout << " " << b;
	// }	
  	if (b > maxeq) {
  	  maxeq = b;
  	  resp = "R";
  	}
      }
      {
  	double b = singleEquities[key2];
	// if (line == error) {
	//   std::cout << " " << b;
	// }		
  	if (b > maxeq) {
  	  maxeq = b;
  	  resp = "L";
  	}
      }
      {
  	double b = equities[line]/100;
  	// double b = equities[line];		
	// if (line == error) {
	//   std::cout << " " << b;
	// }		
  	if (b > maxeq) {
	  bool pocketpair = (handc[0].rank() == handc[1].rank());
	  
	  bool lpair = false;
	  for (Card& c : boardc) {
	    if (c.rank() == handc[0].rank()) {
	      lpair = true;
	      break;
	    }
	  }
	  bool rpair = false;
	  for (Card& c : boardc) {
	    if (c.rank() == handc[1].rank()) {
	      rpair = true;
	      break;
	    }
	  }
	  bool twopair = (rpair && lpair);

	  int lflush = 1;
	  if (handc[0].suit() == handc[1].suit()) {
	    lflush++;
	  }
	  for (Card & c : boardc) {
	    if (c.suit() == handc[0].suit()) {
	      lflush++;
	    }
	  }
	  int rflush = 1;
	  if (handc[0].suit() == handc[1].suit()) {
	    rflush++;
	  }
	  for (Card & c : boardc) {
	    if (c.suit() == handc[1].suit()) {
	      rflush++;
	    }
	  }	  
	  bool flush = (lflush >= 5) && (handc[0].suit() == handc[1].suit());
	  bool flushdraw = (lflush >= 4) && (handc[0].suit() == handc[1].suit());	  

	  std::size_t lstraight = 0;
	  std::size_t lstri = 0;	  	  
	  std::vector<Card> lstr = boardc;
	  lstr.push_back(handc[0]);
	  std::stable_sort(lstr.begin(), lstr.end(), [](auto &left, auto &right) {return left.rank() < right.rank();});
	  while (lstraight + 1 < lstr.size() && (lstr[lstraight].rank() >= lstr[lstraight+1].rank() - 1)) {
	    if (lstr[lstraight].rank() == lstr[lstraight+1].rank() - 1) {
	      lstri++;
	    }
	    lstraight++;	    	    
	  }
	  if (lstr.front().rank() == 0 && lstr.back().rank() == 12) {
	    lstri++;
	  }
	  std::size_t rstraight = 0;
	  std::size_t rstri = 0;	  	  
	  std::vector<Card> rstr = boardc;
	  rstr.push_back(handc[1]);
	  std::stable_sort(rstr.begin(), rstr.end(), [](auto &left, auto &right) {return left.rank() < right.rank();});
	  while (rstraight + 1 < rstr.size() && (rstr[rstraight].rank() >= rstr[rstraight+1].rank() - 1)) {
	    if (rstr[rstraight].rank() == rstr[rstraight+1].rank() - 1) {
	      rstri++;
	    }
	    rstraight++;	    	    
	  }
	  if (rstr.front().rank() == 0 && rstr.back().rank() == 12) {
	    rstri++;
	  }	  
	  std::size_t twostraightf = 0;
	  std::size_t twostraightb = 0;
	  std::size_t twostrif = 0;
	  std::size_t twostrib = 0;	  	  
	  std::vector<Card> twostr = boardc;
	  twostr.push_back(handc[0]);
	  twostr.push_back(handc[1]);	  
	  std::stable_sort(twostr.begin(), twostr.end(), [](auto &left, auto &right) {return left.rank() < right.rank();});
	  while (twostraightf + 1 < twostr.size() && (twostr[twostraightf].rank() >= twostr[twostraightf+1].rank() - 1)) {
	    if (twostr[twostraightf].rank() == twostr[twostraightf+1].rank() - 1) {
	      twostrif++;
	    }
	    twostraightf++;	    	    
	  }
	  if (twostr.front().rank() == 0 && twostr.back().rank() == 12) {
	    twostrif++;
	    twostrib++;
	  }	  
	  std::reverse(twostr.begin(), twostr.end());
	  while (twostraightb + 1 < twostr.size() && (twostr[twostraightb].rank() <= twostr[twostraightb+1].rank() + 1)) {
	    if (twostr[twostraightb].rank() == twostr[twostraightb+1].rank() + 1) {
	      twostrib++;
	    }
	    twostraightb++;	    	    
	  }
	  bool straight = (twostrif >= 4 || twostrib >= 4) && (handc[0].rank() != handc[1].rank());
	  bool openendedstraightdraw = (twostrif >= 3 || twostrib >= 3) && (handc[0].rank() != handc[1].rank());	  

	  bool madehand = pocketpair || twopair || flush || straight || flushdraw || openendedstraightdraw;
	  if (madehand) {
	    maxeq = b;
	    resp = "N";
	  }
	  else if (resp == "L") {
	    if ((lflush >= 5 || lstri >=4) && !(rflush >= 5 || rstri >= 4)) {
	      resp = "R";
	    }
	  }
	  else if (resp == "R") {
	    if (!(lflush >= 5 || lstri >=4) && (rflush >= 5 || rstri >= 4)) {
	      resp = "L";
	    }
	  }
	  // if (line == error) {
	  //   std::cout << " " << lflush << " " << rflush << " " << lstri << " " << rstri << std::endl;
	  // }
  	}
      }      
      std::cout << line << " " << resp << std::endl;
      // if (line == error) {
      //   exit(2);
      // }		      
    }
  }
}

void computeSingleEquities(std::string handsFile) {
  std::unordered_map<std::string, double> equities;
  std::ifstream file(handsFile);
  std::string line;
  Results* res = alloc_results();
  std::string dead = "";
  while (std::getline(file, line)) {
    std::string hand = line.substr(0,4);
    std::string board = line.substr(4);
    std::vector<Card> boardc;
    for (std::size_t i = 0; 2*i < board.size(); ++i) {
      boardc.push_back(Card(board.substr(2*i, 2)));
    }
    std::vector<Card> handc1;      
    handc1.push_back(Card(hand.substr(0,2)));
    std::string key1 = translate(boardc,handc1);        
    std::vector<Card> handc2;      
    handc2.push_back(Card(hand.substr(2)));
    std::string key2 = translate(boardc,handc2);        
    if (equities.find(key1) == equities.end()) {
      calc(const_cast<char*>((createRange(hand.substr(0,2))+":"+"xx").c_str()), const_cast<char*>(board.c_str()), const_cast<char*>(dead.c_str()), 100000, res);
      equities[key1] = *(res->ev);
      std::cout << key1 << " " << *(res->ev) << std::endl;
    }
    if (equities.find(key2) == equities.end()) {
      calc(const_cast<char*>((createRange(hand.substr(2))+":"+"xx").c_str()), const_cast<char*>(board.c_str()), const_cast<char*>(dead.c_str()), 100000, res);
      equities[key2] = *(res->ev);
      std::cout << key2 << " " << *(res->ev) << std::endl;      
    }
    // std::cout << line << std::endl;
  }
  free_results(res);
}

void encodeAsInteger(std::string discardFile) {
  std::string line;
  std::ifstream file(discardFile);
  while (std::getline(file, line)) {
    if (line == "N") {
      std::cout << 0 << std::endl;
    } else if (line == "L") {
      std::cout << 1 << std::endl;
    } else {
      std::cout << 2 << std::endl;
    }
  }
}


int main(int argc, char* argv[]) {
  // computeDistances("../data/TurnHands.txt");    
  // computeEquities("../data/TurnHands.txt");  
  // computeSingleEquities("../data/TurnHands.txt");
  // chooseDiscards("../data/TurnDistances.txt","../data/TurnSingleEquities.txt","../data/TurnHands.txt");
  // chooseDiscards("../data/FlopDistances.txt","../data/FlopSingleEquities.txt","../data/FlopHands.txt");    
  // genRandomTurns(1000000);
  // encodeTurnBuckets("../data/FlopHands.txt", "../data/TurnAssignments.txt", "../data/TurnDiscards.txt");
  encodeAsInteger("../data/FlopDiscardsEncoded.txt");
  // for (int i = 0 ; i < 1300000 ; ++i) {
  //   std::cout << "1234567890123456789012345678901234567890123456789012" << std::endl;
  // }
  // computeDistances("../build/tmp");
  // cluster("../data/FlopDistances.txt", "../data/ClusterCenters.txt");
  // cluster("../data/TurnDistances.txt", "../data/ClusterCenters.txt");  
  // cluster("../data/TurnDistances.txt", "../data/TurnCenters.txt");  
}
