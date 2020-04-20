import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.Stock;
import model.StockStat;
import model.User;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class PersonalPage {
    private HashMap<Integer, User> users = new HashMap<>();


    public int createUser() {
        return createUser(0.0, new HashMap<>());
    }

    public int createUser(Double money, HashMap<String, Double> stocks) {
        int userId = new Random().nextInt();

        while (users.containsKey(userId)) {
            userId = new Random().nextInt();
        }

        users.put(userId, new User(money, stocks));
        return userId;
    }

    public void changeAvailableMoney(int userId, Double money) {
        double newAmount = users.get(userId).availableMoney + money;
        if (newAmount > 0) {
            users.get(userId).availableMoney = newAmount;
        }
    }

    public List<StockStat> getAllStocks(int userId) {
        List<StockStat> stoks = new ArrayList<>();
        users.get(userId).stocks.forEach((company, amount) -> {
            try {
                stoks.add(new StockStat(company, amount, getStock(company).price));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return stoks;
    }

    public Double getAllWorth(int userId) {
        double sum = users.get(userId).availableMoney;
        sum += getAllStocks(userId).stream().mapToDouble(stockStat -> stockStat.priceForOne * stockStat.numberOfStocks).sum();
        return sum;
    }

    public void buyStocks(int userId, String company, Double numberOfStocks) throws Exception {
        Stock stock = getStock(company);
        if (users.get(userId).availableMoney < stock.price * numberOfStocks) {
            throw new Exception("Not enough money");
        } else if (numberOfStocks > stock.amount) {
            throw new Exception("Not enough stocks");
        } else if (numberOfStocks < 0) {
            throw new Exception("The number od stocks < 0");
        } else {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:8080/buyStock?company=" + company + "&amount=" + numberOfStocks))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            assert HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString()).statusCode() == 200;
            users.get(userId).availableMoney -= stock.price * numberOfStocks;
            if (users.get(userId).stocks.containsKey(company)) {
                Double amount = users.get(userId).stocks.get(company);
                users.get(userId).stocks.put(company, amount + numberOfStocks);
            } else {
                users.get(userId).stocks.put(company, numberOfStocks);
            }
        }
    }

    public void sellStocks(int userId, String company, Double numberOfStocks) throws Exception {
        Stock stock = getStock(company);
        if (!users.get(userId).stocks.containsKey(company)) {
            throw new Exception("No stocks of such company");
        } else {
            Double amount = users.get(userId).stocks.get(company);
            if (numberOfStocks > amount) {
                throw new Exception("Not enough stocks");
            } else if (numberOfStocks < 0) {
                throw new Exception("The number od stocks < 0");
            } else {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(new URI("http://localhost:8080/sellStock?company=" + company + "&amount=" + numberOfStocks))
                        .POST(HttpRequest.BodyPublishers.noBody())
                        .build();

                assert HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString()).statusCode() == 200;
                users.get(userId).availableMoney += stock.price * numberOfStocks;
                users.get(userId).stocks.put(company, amount - numberOfStocks);
            }
        }
    }

    public void createCompany(String company, Double priceForStock, Double number) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/newCompany?company=" + company + "&price=" + priceForStock + "&number=" + number))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        assert HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString()).statusCode() == 200;
    }

    public void changePrice(String company, Double priceForStock) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/changePrice?company=" + company + "&newPrice=" + priceForStock))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        assert HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString()).statusCode() == 200;
    }

    private Stock getStock(String company) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/getStock?company=" + company))
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        assert response.statusCode() == 200;
        JsonNode node = new ObjectMapper().readTree(response.body());
        return new Stock(node.get("price").asDouble(), node.get("amount").asDouble());
    }

}
