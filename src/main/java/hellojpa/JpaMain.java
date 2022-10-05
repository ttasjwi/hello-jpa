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
        Book book = Book.builder()
                .name("땃쥐의 JPA")
                .price(10000)
                .build();

        // 등록
        em.persist(book);

        // 수정
        book.changeBookInfo("상땃쥐의 JPA", 20000);

        // 단건 조회
        Book findBook = em.find(Book.class, book.getId());
        System.out.println(findBook);

        // 목록 조회
        List<Book> books = em.createQuery("SELECT b FROM Book as b", Book.class)
                .getResultList();
        System.out.println("books.size = "+books.size());

        // 삭제
        em.remove(book);
    }
}
