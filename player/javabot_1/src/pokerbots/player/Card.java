package pokerbots.player;

/**
 * Created by kundanc on 1/16/17.
 */
public class Card {
    Card(String s, int inHand) {
        char ranks[] = {'2','3','4','5','6','7','8','9','T','J','Q','K','A'};
        char suits[] = {'c','d','h','s'};
        char r = s.charAt(0);
        for (int i = 0; i < ranks.length; ++i) {
            if (r == ranks[i]) {
                rank = i;
            }
        }
        char c = s.charAt(1);
        for (int i = 0; i < suits.length; ++i) {
            if (c == suits[i]) {
                suit = i;
            }
        }
        hand = inHand;
    }

    @Override
    public String toString() {
        char ranks[] = {'2','3','4','5','6','7','8','9','T','J','Q','K','A'};
        char suits[] = {'c','d','h','s'};
        String s = "";
        s += ranks[rank];
        s += suits[suit];
        return s;
    }

    public int rank;
    public int suit;
    public int hand;
}

