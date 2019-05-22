import java.sql.*;
import java.util.ArrayList;
import java.util.Locale;

public class DatabaseConnection {
    public static String HOST = "jdbc:mysql://localhost/?";
    public static String USERNAME = "root";
    public static String PASSWORD = "M44ZigMa@77b";
    public static String DATABASE = "hicochat_db";

    public Connection conn = null;
    public DatabaseConnection()
    {
        try {
            this.conn = DriverManager.getConnection("jdbc:mysql://localhost/"+DATABASE+"?" +
                    "user=" + USERNAME + "&password=" + PASSWORD);
        } catch (SQLException ex) {
            // handle any errors
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }
    }

    public NewUserResponse newUser(String userName)
    {
        userName = userName.toLowerCase(Locale.ENGLISH);
        CallableStatement stmt = null;
        ResultSet rs = null;
        NewUserResponse newUserResponse = new NewUserResponse();
        try{
            stmt = conn.prepareCall("{call newuser(?)}");
            stmt.setString(1, userName);

            boolean hadResults = stmt.execute();

            rs = stmt.getResultSet();
            while (rs.next()) {
                newUserResponse.token = rs.getString("token");
                newUserResponse.userId = rs.getInt("newUserId");
                newUserResponse.status = 1;
            }
        }catch (Exception e){
            newUserResponse.status = 0;
            System.out.println(e);
        }
        finally {
            // it is a good idea to release
            // resources in a finally{} block
            // in reverse-order of their creation
            // if they are no-longer needed

            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException sqlEx) { } // ignore

                rs = null;
            }

            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException sqlEx) { } // ignore

                stmt = null;
            }
        }


        return newUserResponse;
    }
    public ArrayList<User> getAllUsers()
    {
        ArrayList<User> users = new ArrayList<User>();
        CallableStatement stmt = null;
        ResultSet rs = null;
        NewUserResponse newUserResponse = new NewUserResponse();
        try{
            stmt = conn.prepareCall("{call getallusers()}");

            boolean hadResults = stmt.execute();

            rs = stmt.getResultSet();
            while (rs.next()) {
                User u = new User();
                u.token = rs.getString("token");
                u.userId = rs.getInt("user_id");
                u.userName = rs.getString("user_name");
                u.lastActivity = rs.getString("last_activity");
                users.add(u);
            }
        }catch (Exception e){
            newUserResponse.status = 0;
            System.out.println(e);
        }
        finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException sqlEx) { } // ignore

                rs = null;
            }

            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException sqlEx) { } // ignore

                stmt = null;
            }
        }
        return users;
    }

    public User getUserByToken(String token)
    {
        User user = null;
        CallableStatement stmt = null;
        ResultSet rs = null;
        NewUserResponse newUserResponse = new NewUserResponse();
        try{
            stmt = conn.prepareCall("{call getuserbytoken(?)}");
            stmt.setString(1, token);

            boolean hadResults = stmt.execute();

            rs = stmt.getResultSet();
            while (rs.next()) {
                user = new User();
                user.token = rs.getString("token");
                user.userId = rs.getInt("user_id");
                user.userName = rs.getString("user_name");
                user.lastActivity = rs.getString("last_activity");
            }
        }catch (Exception e){
            newUserResponse.status = 0;
            System.out.println(e);
        }
        finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException sqlEx) { } // ignore

                rs = null;
            }

            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException sqlEx) { } // ignore

                stmt = null;
            }
        }
        return user;
    }


    class NewUserResponse{
        public String token ="";
        public Integer userId = 0;
        public Integer status = 0;
    }

    class User{
        public String token ="";
        public Integer userId = 0;
        public String userName = "";
        public String lastActivity = "";
        public String toJson()
        {
            return "{\"userId\":"+this.userId+"," +
                    "\"userName\":\""+this.userName+"\"," +
                    "\"token\":\""+this.token+"\"," +
                    "\"lastActivity\":\""+this.lastActivity+"\"}";
        }
    }



}

