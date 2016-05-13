import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.sql.*;
import java.util.ArrayList;

public class Service {

    private final Connection connection;

    public Service(Connection connection) {
        this.connection = connection;
    }

    //init database
    public void initDatabase() throws SQLException {
        Statement statement = connection.createStatement();
        statement.execute("CREATE TABLE IF NOT EXISTS planet (id IDENTITY, name VARCHAR, radius INT, supportsLife BOOLEAN, distanceFromSun DOUBLE)");
        statement.execute("CREATE TABLE IF NOT EXISTS moon (id IDENTITY, name VARCHAR, color VARCHAR, planetId INT)");

    }

    //receives Planet data as JSON, parses into Planet and adds to database (USE JOIN HERE)
    public Planet parsePlanet(String json) throws SQLException{

        Gson gson = new GsonBuilder().create();
        Planet planet = gson.fromJson(json, Planet.class);

        PreparedStatement statement = connection.prepareStatement("INSERT INTO planet VALUES (NULL, ?, ?, ?, ?)");
        statement.setString(1, planet.getName());
        statement.setInt(2, planet.getRadius());
        statement.setBoolean(3, planet.supportsLife);
        statement.setDouble(4, planet.getDistanceFromSun());
        statement.executeUpdate();

        //set id

        ResultSet resultSet = statement.getGeneratedKeys();
        resultSet.next();
        planet.setId(resultSet.getInt(1));

        return planet;

    }

    //returns arraylist of all planets
    public ArrayList<Planet> getPlanets() throws SQLException{

        PreparedStatement statement = connection.prepareStatement("SELECT * FROM planet");
        ResultSet resultSet = statement.executeQuery();
        ArrayList<Planet> planets = new ArrayList<>();

        while(resultSet.next()){

            Planet planet = new Planet(

                    resultSet.getString("name"),
                    resultSet.getInt("radius"),
                    resultSet.getBoolean("supportsLife"),
                    resultSet.getDouble("distanceFromSun")

            );
            planets.add(planet);
        }
        return planets;
    }

    //retrieves single Planet from id in query params

    public Planet getPlanet (int id) throws SQLException {

        PreparedStatement statement = connection.prepareStatement("SELECT * FROM planet WHERE ID = ?");
        statement.setInt(1, id);

        ResultSet resultSet = statement.executeQuery();
        ArrayList<Planet> singlePlanet = new ArrayList<>();

            while (resultSet.next()) {
                Planet planet = new Planet(
                resultSet.getString("name"),
                resultSet.getInt("radius"),
                resultSet.getBoolean("supportsLife"),
                resultSet.getDouble("distanceFromSun")
            );

            singlePlanet.add(planet);

        }

        return singlePlanet.get(0);

    }

}
