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
  char ranks[] = {'2','3','4','5','6','7','8','9','T','J','Q','K','A'};
  char suits[] = {'c','d','h','s'};
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
	std::stable_sort(cards.begin(), cards.end(), [](auto &left, auto &right) {
	    return std::get<0>(left) < std::get<0>(right);
	  });
	std::stable_sort(cards.begin(), cards.end(), [](auto &left, auto &right) {
	    return std::get<2>(left) < std::get<2>(right);
	  });
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
	std::stable_sort(cards.begin(), cards.end(), [](auto &left, auto &right) {
	    return std::get<1>(left) < std::get<1>(right);
	  });
	std::stable_sort(cards.begin(), cards.end(), [](auto &left, auto &right) {
	    return std::get<0>(left) < std::get<0>(right);
	  });
	std::stable_sort(cards.begin(), cards.end(), [](auto &left, auto &right) {
	    return std::get<2>(left) < std::get<2>(right);
	  });
      
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

int main(int argc, char* argv[]) {
  std::ifstream file("../data/TurnHands.txt");
  std::string line;
  Results* res = alloc_results();
  std::string dead = "";
  while (std::getline(file, line)) {
    std::vector<std::string> words;
    boost::split(words, line, boost::is_any_of(" "));
    std::string hand = words[0]+words[1];
    std::string board = words[2]+words[3]+words[4]+words[5];
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
