package hellojpa;

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

        try {
            tx.begin();
            logic(em);
            tx.commit();
        } catch(Exception e) {
            e.printStackTrace();
            tx.rollback();
        } finally {
            em.close();
        }
        emf.close();
    }

    private static void logic(EntityManager em) {
        Book book = new Book("땃쥐의 Spring", 30000); // 비영속 상태
        em.persist(book); // 영속화
        em.flush(); // 쓰기지연 SQL 저장소에 누적되어 있는 SQL을 DB에 반영

        em.detach(book); // 준영속 상태 (영속성 컨텍스트에서 분리)

        Book findBook = em.find(Book.class, book.getId());// 영속 상태
        em.remove(findBook); // 삭제 (DB, 영속성 컨텍스트로부터)
    }
}
