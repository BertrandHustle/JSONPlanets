import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.h2.tools.Server;
import spark.Spark;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Main {

    public static void main(String[] args) throws SQLException{

        //creates server
        Server server = Server.createTcpServer("-baseDir", "./data").start();

        //creates connection
        String jdbcUrl = "jdbc:h2:" + server.getURL() + "/main";
        System.out.println(jdbcUrl);
        Connection connection = DriverManager.getConnection(jdbcUrl, "", null);

        //creates/configures web service
        Service service =  new Service(connection);

        //init database
        service.initDatabase();

        Spark.get(
                //change to "/planets" later?
                "/",
                (request, response) -> {
                    // returns an arraylist of all planets
                    return "test";
                }
        );

        Spark.get(
                "/planet",
                (request, response) -> {
                    // queryParam for id?
                    // or maybe a param like :name and request.params(":name") ?

                    Planet planet = new Planet();
                    planet.name = request.params(":name");
                    planet.distanceFromSun = 1;
                    planet.radius = 6387; // km
                    planet.supportsLife = true;

                    Moon moon = new Moon();
                    moon.name = "Luna";

                    planet.moons.add(moon);

                    Gson gson = new GsonBuilder().create();
                    return gson.toJson(planet);

                }
        );

        Spark.post(
                "/planet",
                (request, response) -> {
                    String planetJson = request.queryParams("planet");
                    Gson gson = new GsonBuilder().create();
                    Planet planet = gson.fromJson(planetJson, Planet.class);

                    return "";
                }

        );

    }

}