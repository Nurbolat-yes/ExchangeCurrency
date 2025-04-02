package Servlets;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.exchangeproject.Currency;
import org.example.exchangeproject.Database;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet("/currency/USD")
public class CurrencyUSDServlet extends HttpServlet {
    private String result;
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,IOException{
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            Database db = Database.getInstance();

            String selectAll = "select * from currencies";

            ResultSet resultSet = db.executeQuerySt(selectAll);

            Currency currency = null;

            while (resultSet.next()){
                if (resultSet.getString("Code").equals("USD")){
                    response.setStatus(200);
                    currency = new Currency(resultSet.getInt("ID"),resultSet.getString("Fullname"),
                            resultSet.getString("code"),resultSet.getString("Sign"));
                }
                else {
                    response.setStatus(400);
                }
            }

            if (currency == null){
                response.sendError(HttpServletResponse.SC_NOT_FOUND,"Currency not Found");
            }

            String json = new Gson().toJson(currency);


            PrintWriter pw = response.getWriter();
            pw.println(json);
            pw.flush();


        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
