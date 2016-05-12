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
        statement.execute("CREATE TABLE IF NOT EXISTS planet (id IDENTITY, radius INT, supportsLife BOOLEAN, distanceFromSun DOUBLE)");

    }

    //returns arraylist of all planets
    public String getPlanets() throws SQLException{

        PreparedStatement statement = connection.prepareStatement("SELECT * FROM planet");
        ResultSet resultSet = statement.executeQuery();
        ArrayList<Planet> planets = new ArrayList<>();

        while(resultSet.next()){

            Planet planet = new Planet(

                    resultSet.getString("name"),
                    resultSet.getInt("radius"),
                    resultSet.getBoolean("supportsLife"),
                    resultSet.getDouble("distanceFromSun"),

            );

            planets.add(planet);

        }

        return planets;

    }

    }
}
