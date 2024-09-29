package jpabook.jpashop.domain;

import jakarta.persistence.Embeddable;
import lombok.*;

/**
 * 값 타입은 변경 불가능하게 설계해야 한다.
 * Setter를 제거하고 생성자에서 값을 모두 초기화해서 변경 불가능한 클래스로 만들기
 * 생성자는 public보다 protected로 설정하는 것이 안전하다.
 * **/
@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Address {

    private String city;
    private String street;
    private String zipcode;

}
