package jpabook.jpashop.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Member {

    @Id
    @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    @NotEmpty
    private String username;

    @Embedded
    private Address address;

    @OneToMany(mappedBy = "member") // Order 테이블에 있는 member에 매핑되었다.
    private List<Order> orders = new ArrayList<>();
}
