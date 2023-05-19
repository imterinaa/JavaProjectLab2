package DBconnection;

import com.example.client.controller.GameController;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
public class DataBase {
    Connection c;
    void connect()
    {
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:JDBC_point.db");
            System.out.println("Opened database successfully");
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(GameController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    public DataBase() {
        connect();
    }
    public void addPlayer() {
        try {
            PreparedStatement pst =
                    c.prepareStatement("INSERT OR IGNORE INTO players(ID,name, wins) VALUES (1,?,?)");
            pst.setString(1, "help");
            pst.setInt(2, 5);
            ((PreparedStatement) pst).executeUpdate();

        } catch (SQLException ex) {
            Logger.getLogger(GameController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}