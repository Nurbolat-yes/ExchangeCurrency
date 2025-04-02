package Servlets;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.exchangeproject.Currency;
import org.example.exchangeproject.Database;
import org.example.exchangeproject.ExchangeRates;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/exchangeRates/*")
public class ExchangeRatesServlet extends HttpServlet {
    @Override
    public void doPost(HttpServletRequest  request,HttpServletResponse response) throws ServletException,IOException{
        String baseCurrencyCode = request.getParameter("baseCurrencyCode");
        String targetCurrencyCode = request.getParameter("targetCurrencyCode");
        int rate = Integer.parseInt(request.getParameter("rate"));

        int baseId = 0;
        int targetId = 0;

        try {
            Database db = Database.getInstance();

            String queryForBaseId = "select * from currencies";

            ResultSet resultSet = db.executeQuerySt(queryForBaseId);
            while (resultSet.next()){
                if (resultSet.getString("code").equals(baseCurrencyCode)){
                    baseId = resultSet.getInt("Id");
                }
                else if (resultSet.getString("code").equals(targetCurrencyCode)) {
                    targetId = resultSet.getInt("Id");
                }
            }

            String queryToInsert = "insert into exchangerates(basecurrencyid,targetcurrencyid,rate) values("+baseId+","+targetId+","+rate+")";

            db.executeUpdateSt(queryToInsert);



        } catch (SQLException e) {
            response.sendError(500,baseId +" "+baseCurrencyCode + " " + targetId +" " +targetCurrencyCode) ;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }


    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,IOException{
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String url = request.getRequestURI();
        String[] parts = url.split("/");
        String pairCurrency = parts[parts.length-1];
        String base = pairCurrency.substring(0,3);
        String target = pairCurrency.substring(3);

        try {
            Database db = Database.getInstance();

            if (pairCurrency.length() == 6){
                doGetForSpecial(request,response,db,base,target);
                return;
            }

            String sqlSelectAll = "SELECT \n" +
                    "    e.id AS exchange_id,\n" +
                    "    b.id AS base_id, b.fullname AS base_name, b.code AS base_code, b.sign AS base_sign,\n" +
                    "    t.id AS target_id, t.fullname AS target_name, t.code AS target_code, t.sign AS target_sign,\n" +
                    "    e.rate\n" +
                    "FROM exchangerates e\n" +
                    "JOIN currencies b ON e.basecurrencyid = b.id\n" +
                    "JOIN currencies t ON e.targetcurrencyid = t.id;";

            ResultSet resultSet = db.executeQuerySt(sqlSelectAll);


            List<ExchangeRates> exchangeRates = new ArrayList<>();

            while (resultSet.next()){
                exchangeRates.add(
                        new ExchangeRates(resultSet.getInt(1),
                                new Currency(resultSet.getInt(2),resultSet.getString(3), resultSet.getString(4), resultSet.getString(5)),
                                new Currency(resultSet.getInt(6),resultSet.getString(7), resultSet.getString(8), resultSet.getString(9)),
                                resultSet.getDouble(10)
                        )
                );
            }

            String json = new Gson().toJson(exchangeRates);

            PrintWriter pw =  response.getWriter();
            pw.println(json);
            pw.flush();

            response.setStatus(200);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void doGetForSpecial(HttpServletRequest request,HttpServletResponse response,Database db,String base,String target) throws SQLException, IOException {
        String sqlSelectAll = "SELECT \n" +
                "    e.id AS exchange_id,\n" +
                "    b.id AS base_id, b.fullname AS base_name, b.code AS base_code, b.sign AS base_sign,\n" +
                "    t.id AS target_id, t.fullname AS target_name, t.code AS target_code, t.sign AS target_sign,\n" +
                "    e.rate\n" +
                "FROM exchangerates e\n" +
                "JOIN currencies b ON e.basecurrencyid = b.id\n" +
                "JOIN currencies t ON e.targetcurrencyid = t.id;";

        ResultSet resultSet = db.executeQuerySt(sqlSelectAll);
        
        List<ExchangeRates> exchangeRates = new ArrayList<>();

        while (resultSet.next()){

            if (base.equals(resultSet.getString(4)) && target.equals(resultSet.getString(8))){
                exchangeRates.add(
                        new ExchangeRates(resultSet.getInt(1),
                                new Currency(resultSet.getInt(2),resultSet.getString(3), resultSet.getString(4), resultSet.getString(5)),
                                new Currency(resultSet.getInt(6),resultSet.getString(7), resultSet.getString(8), resultSet.getString(9)),
                                resultSet.getDouble(10)
                        )
                );
            }
        }

        if (exchangeRates.size() == 0){
            response.sendError(HttpServletResponse.SC_NOT_FOUND,"Exchange rate not found for currency pair");
            return;
        }

        String json = new Gson().toJson(exchangeRates);

        PrintWriter pw =  response.getWriter();
        pw.println(json);
        pw.flush();

        response.setStatus(200);
    }
}
