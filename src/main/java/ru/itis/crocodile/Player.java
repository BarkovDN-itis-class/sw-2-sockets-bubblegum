package ru.itis.crocodile;

public class Player {
    public enum Role {
        HOST,
        PLAYER
    }

    private String playerName;
    private Role role;

    public Player(String playerName, Role role) {
        this.playerName = playerName;
        this.role = role;
    }

    public String getPlayerName() {
        return playerName;
    }

    public Role getRole() {
        return role;
    }

    // Другие методы для управления игроком и его ролью
}
