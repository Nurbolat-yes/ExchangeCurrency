package Servlets;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.exchangeproject.Currency;
import org.example.exchangeproject.Database;

import java.io.PrintWriter;
import java.sql.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/currencies/*")
public class CurrenciesServlet extends HttpServlet {
    @Override
    public void doPost(HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String fullName = request.getParameter("name");
        String code = request.getParameter("code");
        String sign = request.getParameter("sign");

        if (fullName=="" || code == "" || sign==""){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,"A required form field is missing");
            return;
        }

        try {
            Database db = Database.getInstance();

            String sqlSelectAll = "select * from currencies";

            ResultSet resultSet = db.executeQuerySt(sqlSelectAll);

            while (resultSet.next()){
                if (resultSet.getString("Code").equals(code)){
                    response.sendError(HttpServletResponse.SC_CONFLICT,"Currency with this code already exist");
                    return;
                }
            }

            String sqlInsert = "insert into currencies (Code,Fullname,Sign) values ('"+code+"','"+fullName+"','"+sign+"')";

            db.executeUpdateSt(sqlInsert);

            Currency currency = new Currency(0,fullName,code,sign);

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

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            Database db = Database.getInstance();

            String selectAll = "select * from currencies";

            ResultSet resultSet = db.executeQuerySt(selectAll);

            List<Currency> currencies = new ArrayList<>();

            while (resultSet.next()){
                currencies.add(new Currency(resultSet.getInt("ID"),resultSet.getString("Code"),
                        resultSet.getString("Fullname"),resultSet.getString("Sign")));
            }

            String json = new Gson().toJson(currencies);

            PrintWriter pw = response.getWriter();
            pw.println(json);
            pw.flush();

            response.setStatus(HttpServletResponse.SC_OK);

        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,e.getMessage());
        } catch (ClassNotFoundException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"Driver not found");
        }
    }
}
