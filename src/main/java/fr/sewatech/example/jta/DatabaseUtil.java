package fr.sewatech.example.jta;

import java.sql.SQLException;
import javax.naming.NamingException;
import org.h2.jdbcx.JdbcDataSource;

public class DatabaseUtil {
    static JdbcDataSource createDatasource() throws NamingException, SQLException {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setUser("sa");
        dataSource.setPassword("");
        dataSource.setURL("jdbc:h2:~/example-db3");
        return dataSource;
    }

}
