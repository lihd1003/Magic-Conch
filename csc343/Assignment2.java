import java.sql.*;
import java.util.List;

// If you are looking for Java data structures, these are highly useful.
// Remember that an important part of your mark is for doing as much in SQL (not Java) as you can.
// Solutions that use only or mostly Java will not receive a high mark.
import java.util.ArrayList;
//import java.util.Map;
//import java.util.HashMap;
//import java.util.Set;
//import java.util.HashSet;
public class Assignment2 extends JDBCSubmission {

    public Assignment2() throws ClassNotFoundException {
        Class.forName("org.postgresql.Driver");
    }

    @Override
    public boolean connectDB(String url, String username, String password) {
        // Implement this method!
        try{
            connection = DriverManager.getConnection(url, username, password);
        } catch(SQLException se){
            System.err.println("SQL Exception." +
                    "<Message>: " + se.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean disconnectDB() {
        // Implement this method!
        if(connection != null) {
            try {
                connection.close();
            } catch (SQLException se) {
                System.err.println("SQL Exception." +
                        "<Message>: " + se.getMessage());
                return false;
            }
        }
        return true;
    }

    @Override
    public ElectionCabinetResult electionSequence(String countryName) {
        // Implement this method!
        List<Integer> elections = new ArrayList<>();
        List<Integer> cabinets = new ArrayList<>();

        try {
            String queryString1 = "select id from country where name = ?;";
            PreparedStatement ps1 = connection.prepareStatement(queryString1);
            ps1.setString(1, countryName);
            ResultSet rs1 = ps1.executeQuery();
	    	
			int country_id = -1;
			while (rs1.next()){
				country_id = rs1.getInt("id");
			}

			System.out.println("rs2");

            String queryString4 =
                    "select cabinet.id as cabinet_id, time_range.election_id as election_id " +
                    "from cabinet, " +
                        "(select v1.e_date as date_1, v2.id as election_id, v2.e_date as date_2 " +
                        "from (select e_date, election.id, previous_parliament_election_id, previous_ep_election_id, e_type from election where country_id = ?) as v1, (select e_date, election.id, previous_parliament_election_id, previous_ep_election_id, e_type from election where country_id = ?) as v2 " +
                        "where (v1.e_type = 'Parliamentary election' and v1.previous_parliament_election_id = v2.id) or " +
                        "(v2.e_type = 'European Parliament' and v1.previous_ep_election_id = v2.id) " +
                        "order by v2.e_date desc " +
                        ") as time_range " +
                    "where cabinet.start_date >= date_2 and cabinet.start_date < date_1 and cabinet.country_id = ? " +
                    "order by time_range.election_id;";
            PreparedStatement ps4 = connection.prepareStatement(queryString4);
ps4.setInt(1, country_id);
ps4.setInt(2, country_id);
ps4.setInt(3, country_id);
            ResultSet rs = ps4.executeQuery();

            while (rs.next()) {
                elections.add(rs.getInt("election_id"));
                cabinets.add(rs.getInt("cabinet_id"));
            }

        } catch (SQLException se) {
            System.err.println("SQL Exception." +
                    "<Message>: " + se.getMessage());
        }

        return new ElectionCabinetResult(elections, cabinets);
    }

    @Override
    public List<Integer> findSimilarPoliticians(Integer politicianName, Float threshold) {
        // Implement this method!
        List<Integer> ids = new ArrayList<>();

        try{
            String description = new String("");
            String queryString = "SELECT description FROM politician_president WHERE politician_president.id = ?";
            PreparedStatement ps = connection.prepareStatement(queryString);
            ps.setInt(1, politicianName);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                description = rs.getString("description");
            }

            String queryString1 = "SELECT id, description FROM politician_president";
            PreparedStatement ps1 = connection.prepareStatement(queryString1);
            ResultSet rs1 = ps1.executeQuery();
            while (rs1.next()) {
                if(similarity(rs1.getString("description"), description) >= threshold) {
                    int id = rs1.getInt("id");
                    if(id != politicianName){
                        ids.add(id);
                    }
                }
            }
        }catch (SQLException se)
        {
            System.err.println("SQL Exception." +
                    "<Message>: " + se.getMessage());
        }

        return ids;
    }

    public static void main(String[] args) {
        // You can put testing code in here. It will not affect our autotester.
        System.out.println("Hello");
        try{
            Assignment2 test = new Assignment2();
            test.connectDB("jdbc:postgresql://localhost:5432/csc343h-lihaoda?currentSchema=parlgov","lihaoda", "");
            ElectionCabinetResult result = test.electionSequence("Japan");
			System.out.println(result);
        } catch (ClassNotFoundException e) {
            System.err.println(e);
        }

    }

}
