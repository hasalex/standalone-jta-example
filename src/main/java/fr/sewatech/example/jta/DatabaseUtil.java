package fr.sewatech.example.jta;

import java.sql.SQLException;
import javax.naming.NamingException;
import org.h2.jdbcx.JdbcDataSource;

public class DatabaseUtil {
    static JdbcDataSource createDatasource(String dbName) throws SQLException {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setUser("sa");
        dataSource.setPassword("");
        dataSource.setURL("jdbc:h2:./database/" + dbName);
        return dataSource;
    }

}
