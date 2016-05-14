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

        Spark.get(
                "/create-planet",
                (request, response) -> {

                    //hard-coded for example purposes

                    //Moon moon = new Moon("Luna", "white", 1);

                    String json = "[\n" +
                            "{\n" +
                            "name: \"Mars\",\n" +
                            "radius: 500,\n" +
                            "supportsLife: false,\n" +
                            "distanceFromSun: 3,\n" +
                            "id: 0,\n" +
                            "moons: [\n" +
                            "{\n" +
                            "moonName: \"Europa\",\n" +
                            "color: \"blue\",\n" +
                            "planetId: 1\n" +
                            "}\n" +
                            "]\n" +
                            "}\n" +
                            "]";

                    Planet planet = service.parsePlanet(json);

                    //planet.moons.add(moon);

                    return planet;

                }

        );

    }

}