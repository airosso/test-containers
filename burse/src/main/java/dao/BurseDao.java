package dao;

import org.springframework.context.annotation.Bean;

import java.beans.JavaBean;
import java.util.HashMap;

@JavaBean
public class BurseDao {

    public HashMap<String, Double> numberOfStocks = new HashMap<>();

    public HashMap<String, Double> priceForStock = new HashMap<>();

}
