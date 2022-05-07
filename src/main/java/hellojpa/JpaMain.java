package hellojpa;

import hellojpa.domain.Address;
import hellojpa.domain.AddressEntity;
import hellojpa.domain.Member;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.util.List;

public class JpaMain {
    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {
            Member member = new Member();
            member.setName("ttasjwi");
            member.setHomeAddress(new Address("서울특별시", "강남구", "11111"));
            em.persist(member);

            em.flush();
            em.clear();

            String jpql = "SELECT m FROM Member as m WHERE m.name = 'ttasjwi'";
            List<Member> resultList = em.createQuery(jpql, Member.class).getResultList();

            for (Member findMember : resultList) {
                System.out.println("findMember.name = " + findMember.getName());
            }

            tx.commit();
        } catch(Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }
        emf.close();
    }
}
