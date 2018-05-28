package com.sap.espm.model.web;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.eclipse.persistence.config.PersistenceUnitProperties;

import com.sap.espm.model.util.Utility;
import java.util.Map;

/**
 * Handles the singleton {@link EntityManagerFactory} instance.
 * <p>
 * This class is responsible for fetching the details of the {@link DataSource}
 * that has the connection related details (Host, username, password) on how to
 * connect to the data source.
 * <p>
 * <b>Note</b> - This class fetches the {@link DataSource} details via JNDI, so
 * ensure that you have the DataSource configured as a JNDI lookup variable in
 * the respective ServletContainer where the application is deployed.
 */
public class JpaEntityManagerFactory {

	/**
	 * The JNDI name of the DataSource.
	 */
	public static final String DATA_SOURCE_NAME = "java:comp/env/jdbc/DefaultDB";
	public static final String dbName = System.getenv("DATABASE_TYPE");
	
	/**
	 * The package name which contains all the model classes.
	 */
	 private static JsonNode readCredentialsFromEnvironment() throws IOException {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode actualObj = mapper.readTree(System.getenv("VCAP_SERVICES"));
			return actualObj.get(dbName).get(0).get("credentials");
	 }
	public static final String PERSISTENCE_UNIT_NAME = "com.sap.espm.model";

	/**
	 * The static {@link EntityManagerFactory}
	 */
	private static EntityManagerFactory entityManagerFactory = null;

	/**
	 * Returns the singleton EntityManagerFactory instance for accessing the
	 * default database.
	 * 
	 * @return the singleton EntityManagerFactory instance
	 * @throws NamingException
	 *             if a naming exception occurs during initialization
	 * @throws SQLException
	 *             if a database occurs during initialization
	 * @throws IOException 
	 */
	public static synchronized EntityManagerFactory getEntityManagerFactory()
			throws NamingException, SQLException, IOException {
		if (entityManagerFactory == null) {
			InitialContext ctx = new InitialContext();
		    BasicDataSource ds = new BasicDataSource();
		    JsonNode credentials;
		    credentials = readCredentialsFromEnvironment();
			//ds.setDriverClassName(credentials.get("driver").asText());
		     if(dbName.equals("postgresql")){
		    ds.setDriverClassName("org.postgresql.Driver");
		    ds.setUrl(buildJdbcUrl(credentials));
		    ds.setUsername(credentials.get("username").asText());
		    }
		    else{
		    	ds.setDriverClassName(credentials.get("driver").asText());
		    	ds.setUrl(credentials.get("url").asText());
		    	ds.setUsername(credentials.get("user").asText());
		    }
		   
		   
		    ds.setPassword(credentials.get("password").asText());
			//DataSource ds = (DataSource) ctx.lookup(DATA_SOURCE_NAME);
		    System.out.println("ds:"+ds.getUsername());
			Map<String, Object> properties = new HashMap<String, Object>();
			properties.put(PersistenceUnitProperties.NON_JTA_DATASOURCE, ds);
			entityManagerFactory = Persistence.createEntityManagerFactory(
					PERSISTENCE_UNIT_NAME, properties);
			Utility.setEntityManagerFactory(entityManagerFactory);
		}
		return entityManagerFactory;
	}
   

	public static String buildJdbcUrl(JsonNode credentials) {
		return String.format("%s%s://%s:%s/%s?user=%s&password=%s",
	            "jdbc:", "postgresql", credentials.get("hostname").asText(),
				credentials.get("port").asText(), credentials.get("dbname").asText(), credentials.get("username").asText(),credentials.get("password").asText());
	}
	
  
//jdbc:postgresql://10.11.12.232,10.11.12.233/2e8aa57ff2be14b2ed9fc561285b74ce?targetServerType=master

}

