
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.crypto.Data;

public class TestServe extends HttpServlet {


    public TestServe(){
    }

    private static final long serialVersionUID = -4751096228274971485L;
    @Override
    protected void doGet(HttpServletRequest reqest, HttpServletResponse resp)
            throws ServletException, IOException {
        setAccessControlHeaders(resp);
        resp.getWriter().println("Hello World!");
    }


    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setAccessControlHeaders(resp);
        resp.getWriter().println("{\"s\":23}");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setAccessControlHeaders(resp);
        System.out.println("on requested");
        try{
            Gson gson = new Gson();
            StringBuilder sb = new StringBuilder();
            String s;
            while ((s = req.getReader().readLine()) != null) {
                sb.append(s);
            }
            System.out.println(sb);
            NewUserRequest newUserRequest = gson.fromJson(sb.toString(), NewUserRequest.class);
            if(newUserRequest==null)
                throw new Exception("Fatal erro : error code 1");

            String context = newUserRequest.context;
            if(context!=null && !context.isEmpty())
            {
                if(context.equals("NEWUSER"))
                {
                    String userName = newUserRequest.username;
                    if(userName!=null && userName.length()>0)
                    {
                        //add to database
                        DatabaseConnection databaseConnection = new DatabaseConnection();
                        DatabaseConnection.NewUserResponse newUserResponse = databaseConnection.newUser(userName);
                        String msg = "User added";
                        if(newUserResponse.status==0) msg = "Failure";
                        resp.getWriter().println("{\"status\":1,\"message\":\""+msg+"\",\"userId\":"+newUserResponse.userId
                                +",\"token\":\""+newUserResponse.token+"\"}");
                    }
                    else{
                        throw new Exception("No user name found");
                    }
                }
                else if(context.equals("GETALLUSERS"))
                {
                    String token = newUserRequest.token;
                    if(token!=null && token.length()>0)
                    {
                        //add to database
                        DatabaseConnection databaseConnection = new DatabaseConnection();
                        DatabaseConnection.User user = databaseConnection.getUserByToken(token);
                        if(user!=null && user.userId>0)
                        {
                            ArrayList<DatabaseConnection.User> users = databaseConnection.getAllUsers();

                            String data = "[";
                            for(int i=0,j=users.size();i<j;i++)
                            {
                                data += ((i>0)?",":"")+users.get(i).toJson();
                            }
                            data += "]";

                            String msg = users.size()+" users found";
                            resp.getWriter().println("{\"status\":1,\"message\":\""+msg+"\",\"users\":"+data+"}");
                        }
                        else{
                            throw new Exception("You have no access to this resource");
                        }

                    }
                    else{
                        throw new Exception("No user name found");
                    }
                }
            }
            else{
                throw new Exception("No context found");
            }


        }catch (Exception e)
        {
            System.out.println(e);
            resp.getWriter().println("{\"status\":0,\"message\":\""+e.getLocalizedMessage()+"\"}");
        }


    }
    private void setAccessControlHeaders(HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin", "http://localhost:8080");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS, PUT, PATCH, DELETE");
        resp.setHeader("Access-Control-Allow-Headers", "X-Requested-With,content-type");
        resp.setHeader("Access-Control-Allow-Credentials", "true");
    }


    @Override
    public void init() throws ServletException {
        System.out.println("Servlet " + this.getServletName() + " has started");
    }
    @Override
    public void destroy() {
        System.out.println("Servlet " + this.getServletName() + " has stopped");
    }
}

class NewUserRequest{
    public String username = "";
    public String context = "";
    public String token = "";
}
