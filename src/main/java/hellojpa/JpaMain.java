package hellojpa;

import hellojpa.domain.Member;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.time.LocalDateTime;

public class JpaMain {
    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {
            Member member = new Member();
            member.setName("user1");
            member.setCreatedBy("kim");
            member.setCreatedDate(LocalDateTime.now());
            em.persist(member);
            em.flush();
            em.clear();


            Member refMember = em.getReference(Member.class, member.getId());
            System.out.println("refMember.class = " + refMember.getClass());
            em.clear();
            Member findMember = em.find(Member.class, member.getId());
            System.out.println("findMember.classs = " + findMember.getClass());

            System.out.println("(refMember == findMember) : " + (refMember == findMember));

            tx.commit();
        } catch(Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }
        emf.close();
    }
}
