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
                    return id;

                }
        );

        Spark.post(
                "/planet",
                (request, response) -> {

                    Moon moon = new Moon();
                    moon.name = "Luna";

                    String json = "{\n" +
                            "  \"name\": \"Earth\",\n" +
                            "  \"radius\": \"6387\",\n" +
                            "  \"distanceFromSun\": \"1\",\n" +
                            "  \"supportsLife\": \"true\"\n" +
                            "}";

                    Planet planet = service.parsePlanet(json);

                    planet.moons.add(moon);

                    return planet;
                }

        );

    }

}