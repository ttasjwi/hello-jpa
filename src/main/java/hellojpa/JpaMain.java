package hellojpa;

import hellojpa.domain.Address;
import hellojpa.domain.AddressEntity;
import hellojpa.domain.Member;

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
            Member member = new Member();
            member.setName("member");
            member.setHomeAddress(new Address("city", "street", "zipcode"));

            member.getFavoriteFoods().add("피자");
            member.getFavoriteFoods().add("치킨");
            member.getFavoriteFoods().add("삼겹살");

            member.getAddressHistory().add(new AddressEntity("oldCity1", "street", "oldZipcode1"));
            member.getAddressHistory().add(new AddressEntity("oldCity2", "street", "oldZipcode2"));

            em.persist(member);
            em.flush();
            em.clear();

            Member findMember = em.find(Member.class, member.getId());

            tx.commit();
        } catch(Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }
        emf.close();
    }
}
