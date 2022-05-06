package hellojpa;

import hellojpa.domain.Address;
import hellojpa.domain.Child;
import hellojpa.domain.Member;
import hellojpa.domain.Parent;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

public class JpaMain {
    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {
            Address address = new Address("city", "street","30303");

            Member memberA = new Member();
            memberA.setName("memberA");
            memberA.setHomeAddress(address);
            em.persist(memberA);

            Member memberB = new Member();
            memberB.setName("memberB");
            memberB.setHomeAddress(address);
            em.persist(memberB);

            memberA.getHomeAddress().setCity("newCity");

            tx.commit();
        } catch(Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }
        emf.close();
    }
}
