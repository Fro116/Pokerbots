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
  auto boards = abstractBoards(genRivers(), genHands());
  std::cout << boards.size() << std::endl;
}
