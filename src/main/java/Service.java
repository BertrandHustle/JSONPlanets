import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class Service {

    private final Connection connection;

    public Service(Connection connection) {
        this.connection = connection;
    }

    //init database
    public void initDatabase() throws SQLException {
        Statement statement = connection.createStatement();
        statement.execute("CREATE TABLE IF NOT EXISTS planet (id IDENTITY, name VARCHAR, radius INT, supportsLife BOOLEAN, distanceFromSun DOUBLE)");
        statement.execute("CREATE TABLE IF NOT EXISTS moon (id IDENTITY, moonName VARCHAR, color VARCHAR, planetId INT)");

    }

    //receives Planet data as JSON, parses into Planet and adds to database (USE JOIN HERE)
    public Planet parsePlanet(String json) throws SQLException {

        Gson gson = new GsonBuilder().create();

        //may have to use a ridiculous delimiter here to remove moons and parse separately
        //json.split("\t\"\\\"moons.*]\")

        //parsing arraylist from json
        //Type listType = new TypeToken<Collection<Moon>>(){}.getType();
        //List<Moon>moons = new Gson().fromJson(json, listType);

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

        List<Moon> moons = planet.moons;

        //loops through moons, adds to moon table and planet object
        for (Moon moon : moons) {

            //sets moon id to match planet id
            moon.setPlanetId(planet.id);
            PreparedStatement statementM = connection.prepareStatement("INSERT INTO moon VALUES (NULL, ?, ?, ?)");
            statementM.setString(1, moon.moonName);
            statementM.setString(2, moon.color);
            statementM.setInt(3, moon.planetId);
            statementM.executeUpdate();

        }

        return planet;

    }

    //returns arraylist of all planets
    public ArrayList<Planet> getPlanets() throws SQLException {

        //inner join of planets on moons
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM planet LEFT JOIN moon ON moon.planetId = planet.id");

        ResultSet resultSet = statement.executeQuery();
        ArrayList<Planet> planets = new ArrayList<>();
        ArrayList<String> planetNames = new ArrayList<>();

        while (resultSet.next()) {

            //builds planet
            Planet planet = new Planet(

                    resultSet.getString("name"),
                    resultSet.getInt("radius"),
                    resultSet.getBoolean("supportsLife"),
                    resultSet.getDouble("distanceFromSun")

            );

            planet.setId(resultSet.getInt("id"));


            //builds moon and adds to planet
            //put this in its own loop?

            String moonName = resultSet.getString("moonName");
            String color = resultSet.getString("color");
            int planetId = resultSet.getInt("planetId");

            if (planetId != 0) {
                Moon moon = new Moon(moonName, color, planetId);
                planet.moons.add(moon);
            }

            for (Planet planetMoonChecks : planets){
                if (planetMoonChecks.getId() == planetId){
                    Moon moon = new Moon(moonName, color, planetId);
                    planetMoonChecks.moons.add(moon);
                }
            }

            //checks if planet has already been made
            if (!planetNames.contains(planet.name)) {
                planets.add(planet);
                planetNames.add(planet.getName());
            }


        }

        return planets;
    }


    //retrieves single Planet from id in query params

    public Planet getPlanet(int id) throws SQLException {

        PreparedStatement statement = connection.prepareStatement("SELECT * FROM planet INNER JOIN moon ON moon.planetId = planet.id WHERE planet.id = ?");
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

        ResultSet resultSetM = statement.executeQuery();
        ArrayList<Moon> moons = new ArrayList<>();

        while (resultSetM.next()) {
            Moon moon = new Moon(
                    resultSetM.getString("moonName"),
                    resultSetM.getString("color"),
                    resultSetM.getInt("planetId")
            );

            moons.add(moon);

        }

        for (Moon moon : moons) {
            singlePlanet.get(0).moons.add(moon);
        }

        return singlePlanet.get(0);

    }
}

