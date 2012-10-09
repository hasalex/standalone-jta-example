package fr.sewatech.example.jta;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import org.junit.Test;

public class CourseTest {
    
    @Test
    public void should_persist_work() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("Example1PU");
        
        EntityManager em = emf.createEntityManager();
        EntityTransaction transaction = null;
        try {
            transaction = em.getTransaction();
            transaction.begin();
            em.persist(newCourse());
            transaction.commit();

            transaction = em.getTransaction();
            transaction.begin();
            final Course found = em.find(Course.class, 1L);
            System.out.println(found.getCode());
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        } finally {
            em.close();
        }
    } 

    private Course newCourse() {
        Course course = new Course();
        course.setCode("TST");
        course.setName("Test");
        return course;
    }

}
