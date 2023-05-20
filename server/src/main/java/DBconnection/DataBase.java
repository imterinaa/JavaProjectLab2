package DBconnection;

import com.example.client.controller.GameController;

import java.sql.*;
import entity.Pair;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
public class DataBase {
    Connection c;
    void connect()
    {
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:C:\\Users\\Dedmo\\Downloads\\demo\\JavaProjectLab2\\server\\JDBC_point.db");
            System.out.println("Opened database successfully");
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(GameController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    public DataBase() {
        connect();
    }
    public void addPlayer(String name) {
        try {
            PreparedStatement pst =
                    c.prepareStatement("INSERT OR IGNORE INTO pll(name, wins) VALUES (?,?)",
                            Statement.RETURN_GENERATED_KEYS);
            pst.setString(1, name);
            pst.setInt(2, 0);
            System.out.println(name);
            ((PreparedStatement) pst).executeUpdate();

        } catch (SQLException ex) {
            Logger.getLogger(GameController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public int getPlayerWins(String name) {
        try {
            PreparedStatement pst = c.prepareStatement("select * from players" +
                    "WHERE name = ?");
            pst.setString(2, name);

            ResultSet r= pst.executeQuery();

            r.next();
            return (r.getInt("wins"));


        } catch (SQLException ex) {
            Logger.getLogger(GameController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
    }
    public void setPlayerWins(Pair p) {
        try {
            System.out.println("zahel");
            PreparedStatement pst =
                    c.prepareStatement("UPDATE pll " +
                            "SET wins = ?" +
                            "WHERE name = ?");
            pst.setInt(1, p.getInt());
            pst.setString(2, p.getString());
            pst.executeUpdate();


        } catch (SQLException ex) {
            Logger.getLogger(GameController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public ArrayList<Pair> getAllPlayers() {
        ArrayList<Pair> res = new ArrayList<>();

        try {
            Statement st = c.createStatement();
            ResultSet r= st.executeQuery("select * from pll ORDER BY wins DESC");

            while(r.next())
            {
                var client = new Pair(r.getInt("wins"),r.getString("name"));

                res.add(client);
            }

        } catch (SQLException ex) {
            Logger.getLogger(GameController.class.getName()).log(Level.SEVERE, null, ex);
        }

        return res;
    }


}