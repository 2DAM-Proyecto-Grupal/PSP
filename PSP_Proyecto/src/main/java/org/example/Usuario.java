package org.example;

import lombok.Data;

@Data
public class Usuario {
    private int id;
    private int karma;
    private boolean premium;

    // Constructor
    public Usuario(int id, int karma, boolean premium) {
        this.id = id;
        this.karma = karma;
        this.premium = premium;
    }

    @Override
    public String toString() {
        return "ID: " + id +
                "\nKarma: " + karma +
                "\nPremium: " + premium;
    }
}