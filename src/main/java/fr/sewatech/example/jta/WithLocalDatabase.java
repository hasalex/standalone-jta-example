package fr.sewatech.example.jta;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import org.apache.log4j.Logger;

public class WithLocalDatabase {

    private static final Logger logger = Logger.getLogger(WithLocalDatabase.class);

    public static void main(String[] args) {
        EntityManagerFactory emf = null;
        EntityTransaction transaction = null;
        try {
            emf = Persistence.createEntityManagerFactory("Example3PU");
            EntityManager em = emf.createEntityManager();

            transaction = em.getTransaction();
            transaction.begin();

            Course course = new Course();
            course.setCode("C0");
            course.setName("Course 0");
            
            em.persist(course);
            em.flush();

            logger.info("Before rollback : " + em.createNamedQuery("findAll").getResultList());
            transaction.rollback();

            em.close();
            
            EntityManager emRead = emf.createEntityManager();
            logger.info("After  rollback : " + emRead.createNamedQuery("findAll").getResultList());
            emRead.close();
        } catch (Exception e) {
            rollback(transaction);
            logger.error(e);
        } finally {
            if (emf != null) {
                emf.close();            
            } 
        }
    }

    private static void rollback(EntityTransaction transaction) {
        if (transaction != null) {
            transaction.rollback();
        }
    }
}