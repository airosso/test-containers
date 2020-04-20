package app;

import dao.BurseDao;
import model.Stock;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import javax.servlet.http.HttpServletResponse;

@RestController
public class Burse {

    BurseDao burseDao;

    public Burse(BurseDao burseDao) {
        this.burseDao = burseDao;
    }

    @GetMapping("/getStock")
    public Stock getStock(String company) throws Exception {
        Double price = burseDao.priceForStock.get(company);
        if(price == null){
            throw new Exception("Price not found");
        }else{
            Double amount = burseDao.numberOfStocks.get(company);
            return new Stock(price, amount);
        }
    }

    @GetMapping("/")
    public String healthCheck(String company) throws Exception {
        return "ok";
    }

    @PostMapping("/newCompany")
    public void createCompany(String company, Double price, Double number, HttpServletResponse response) throws Exception {
        if(burseDao.priceForStock.containsKey(company))
            response.setStatus(400);
        burseDao.priceForStock.put(company, price);
        burseDao.numberOfStocks.put(company, number);
    }

    private void badRequest(String message) {
        throw HttpClientErrorException.create(HttpStatus.BAD_REQUEST, message, HttpHeaders.EMPTY, new byte[0], null);
    }

    @PostMapping("/buyStock")
    public void buyStock(String company, Double amount) throws Exception {
        Double available = burseDao.numberOfStocks.get(company);
        if(amount.compareTo(available) > 0){
            throw new Exception("There are no so many stocks");
        }else{
            burseDao.numberOfStocks.put(company, available-amount);
        }
    }

    @PostMapping("/sellStock")
    public void sellStock(String company, Double amount) throws Exception{
        Double oldAmount = burseDao.numberOfStocks.get(company);
        burseDao.numberOfStocks.put(company, oldAmount+amount);
    }

    @PostMapping("/changePrice")
    public void changePrice(String company, Double newPrice){
        burseDao.priceForStock.put(company, newPrice);
    }
}
