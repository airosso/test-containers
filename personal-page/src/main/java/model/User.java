package model;

import java.util.HashMap;

public class User {
    public Double availableMoney;
    public HashMap<String, Double> stocks;

    public User() {
        availableMoney = 0.0;
        stocks = new HashMap<>();
    }

    public User(Double availableMoney, HashMap<String, Double> stocks) {
        this.availableMoney = availableMoney;
        this.stocks = stocks;
    }
}
