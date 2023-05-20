package entity;

import com.example.client.entity.Pair;

import java.util.ArrayList;
import java.util.List;

public class PlayersList {
    public List<String> players;
    public List<Integer> wins;

    public PlayersList() {
        players = new ArrayList<>(4);
        wins = new ArrayList<>(4);
    }

    public PlayersList(ArrayList<Pair> pairs) {
        this.players = new ArrayList<>(pairs.size());
        this.wins = new ArrayList<>(pairs.size());

        for (var player : pairs) {
            System.out.println(player.getString()+ player.getInt());
            this.players.add(player.getString());
            this.wins.add(player.getInt());
        }
    }

    public List<String> players() {
        return players;
    }

    public List<Integer> wins() {
        return wins;
    }
}