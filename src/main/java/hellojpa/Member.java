package hellojpa;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table
public class  Member {

    @Id
    private Long id;
    private String name;

    // JPA는 내부적으로 일부 로직을 위해 리플렉션을 사용하는데, 기본생성자가 필요하다. (public으로 할 필요는 없고, 다른 레벨로 해도 됨)
    public Member() {}

    public Member(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
