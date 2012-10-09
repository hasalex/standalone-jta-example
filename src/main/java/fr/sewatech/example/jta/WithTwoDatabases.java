package fr.sewatech.example.jta;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import org.apache.log4j.Logger;

public class WithTwoDatabases {

    private static final Logger logger = Logger.getLogger(WithTwoDatabases.class);

    public static void main(String[] args) {
        UserTransaction userTransaction = null;
        EntityManagerFactory emf1 = null;
        EntityManagerFactory emf2 = null;
        try (JndiServer jndi = JndiServer.startServer()) {
            userTransaction = com.arjuna.ats.jta.UserTransaction.userTransaction();
            jndi.initializeNamingContext()
                    .andBind("java:/TransactionManager", com.arjuna.ats.jta.TransactionManager.transactionManager())
                    .andBind("java:comp/UserTransaction", userTransaction)
                    .andBind("java:/Example1DS", DatabaseUtil.createDatasource("example1-db"))
                    .andBind("java:/Example2DS", DatabaseUtil.createDatasource("example2-db"));

            emf1 = Persistence.createEntityManagerFactory("Example1PU");
            emf2 = Persistence.createEntityManagerFactory("Example2PU");

            userTransaction.begin();

            Course course1 = new Course();
            course1.setCode("C1");
            course1.setName("Course 1");
            
            EntityManager em1 = emf1.createEntityManager();
            em1.persist(course1);
            em1.flush();

            Course course2 = new Course();
            course2.setCode("C2");
            course2.setName("Course 2");
            
            EntityManager em2 = emf2.createEntityManager();
            em2.persist(course2);
            em2.flush();
            
            logger.info("Before rollback : " + em1.createNamedQuery("findAll").getResultList());
            logger.info("Before rollback : " + em2.createNamedQuery("findAll").getResultList());

            userTransaction.rollback();
            em1.close();
            em2.close();
            
            EntityManager em1Read = emf1.createEntityManager();
            logger.info("After  rollback : " + em1Read.createNamedQuery("findAll").getResultList());
            em1Read.close();

            EntityManager em2Read = emf2.createEntityManager();
            logger.info("After  rollback : " + em2Read.createNamedQuery("findAll").getResultList());
            em2Read.close();

        } catch (Exception e) {
            rollback(userTransaction);
            logger.error(e);
        } finally {
            if (emf1 != null) {
                emf1.close();            
            } 
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