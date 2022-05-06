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
            Address addressA = new Address("city", "street","30303");

            Member memberA = new Member();
            memberA.setName("memberA");
            memberA.setHomeAddress(addressA);
            em.persist(memberA);

            Address addressB = new Address("newCity", addressA.getStreet(), addressA.getZipcode());

            Member memberB = new Member();
            memberB.setName("memberB");
            memberB.setHomeAddress(addressB);
            em.persist(memberB);

            tx.commit();
        } catch(Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }
        emf.close();
    }
}
