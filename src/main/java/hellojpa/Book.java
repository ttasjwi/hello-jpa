package hellojpa;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "book")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(of = {"id", "name", "price"})
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "price")
    private int price;

    @Builder(access = AccessLevel.PUBLIC)
    private Book(Long id, String name, int price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public Book(String name, int price) {
        this(null, name, price);
    }

    public void changeBookInfo(String name, int price) {
        this.name = name;
        this.price = price;
    }

}

