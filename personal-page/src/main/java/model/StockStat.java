package model;

public class StockStat {
    public String company;
    public Double numberOfStocks;
    public Double priceForOne;

    public StockStat(String company, Double numberOfStocks, Double priceForOne) {
        this.company = company;
        this.numberOfStocks = numberOfStocks;
        this.priceForOne = priceForOne;
    }
}
