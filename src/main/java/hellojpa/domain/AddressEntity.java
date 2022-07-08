package hellojpa.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "address")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter @Setter
public class AddressEntity {

    @Id @GeneratedValue
    @Column(name = "address_id")
    private Long id;

    @Embedded
    private Address address;

    public AddressEntity(Address address) {
        this.address = address;
    }

    public AddressEntity(String city, String street, String zipcode) {
        this(new Address(city, street, zipcode));
    }

}
