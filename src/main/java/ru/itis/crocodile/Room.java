package ru.itis.crocodile;

import java.util.ArrayList;
import java.util.List;

public class Room {
    private int numberOfRounds;
    private List<Player> players;
    // Другие поля и методы, если необходимо

    public Room(int numberOfRounds) {
        this.numberOfRounds = numberOfRounds;
        this.players = new ArrayList<Player>();
    }

    public int getNumberOfRounds() {
        return numberOfRounds;
    }

    public void addPlayer(Player player) {
        players.add(player);
    }

    // Другие методы для управления комнатой и игроками
}
