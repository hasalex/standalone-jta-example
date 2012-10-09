package fr.sewatech.example.jta;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import org.apache.log4j.Logger;

public class App {

    private static final Logger logger = Logger.getLogger(App.class);

    public static void main(String[] args) {
        UserTransaction userTransaction = null;
        EntityManagerFactory emf = null;
        try (JndiServer jndi = JndiServer.startServer()) {
            userTransaction = com.arjuna.ats.jta.UserTransaction.userTransaction();
            jndi.initializeNamingContext()
                    .andBind("java:/TransactionManager", com.arjuna.ats.jta.TransactionManager.transactionManager())
                    .andBind("java:comp/UserTransaction", userTransaction)
                    .andBind("java:/MyDatasource", DatabaseUtil.createDatasource());

            emf = Persistence.createEntityManagerFactory("helloworld");

            userTransaction.begin();
            EntityManager em = emf.createEntityManager();

            Course course = new Course();
            course.setName("firstvalue");
            em.persist(course);
            em.flush();

            System.out.println("\nCreated and flushed instance a with id : " + course.getId() + "  a.name set to:" + course.getName());
            userTransaction.commit();

        } catch (Exception e) {
            rollback(userTransaction);
        } finally {
            emf.close();            
        }
    }

    private static void rollback(UserTransaction userTransaction) {
        if (userTransaction != null) {
            try {
                userTransaction.rollback();
            } catch (IllegalStateException | SecurityException | SystemException ex) {
                logger.warn(ex);
            }
        }
    }
}