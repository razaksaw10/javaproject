package yeni;


	
	import java.sql.Connection;
	import java.sql.DriverManager;
	import java.sql.SQLException;

	public class Database {

	    private static final String URL = "jdbc:mysql://localhost:3306/deneme3";  // URL de la base de données
	    private static final String USER = "#";  // Utilisateur MySQL
	    private static final String PASSWORD = "#";  // Mot de passe (vide par défaut pour root)

	    public static Connection getConnection() throws SQLException {
	        try {
	            // Charger le driver JDBC MySQL
	            Class.forName("com.mysql.cj.jdbc.Driver");
	            // Retourner la connexion
	            return DriverManager.getConnection(URL, USER, PASSWORD);
	        } catch (ClassNotFoundException e) {
	            throw new SQLException("JDBC Driver non trouvé", e);
	        }
	    }
	}



