import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.h2.tools.Server;
import spark.Spark;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

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
                    ArrayList<Planet> planets = new ArrayList<>();
                    planets = service.getPlanets();

                    Gson gson = new GsonBuilder().create();
                    return gson.toJson(planets);
                }
        );

        Spark.get(
                "/planet",
                (request, response) -> {

                    // queryParam for id
                    int id = Integer.parseInt(request.queryParams("id"));
                    Planet planet = service.getPlanet(id);

                    Gson gson = new GsonBuilder().create();
                    return gson.toJson(planet);

                }
        );

        Spark.post(
                "/create-planet",
                (request, response) -> {

                    //hard-coded for example purposes

                    Planet planet = new Planet("Venus", 800, false, 2.3);
                    Moon moon = new Moon("Luna", "white", 1);
                    Moon moon2 = new Moon("Casiopia", "black", 1);

                    planet.moons.add(moon);
                    planet.moons.add(moon2);

                    Gson gson = new GsonBuilder().create();

                    String json = gson.toJson(planet);

                    System.out.println(json);
                    int i = 0;

                    Planet newPlanet = service.parsePlanet(json);

                    return newPlanet;

                }

        );

    }

}