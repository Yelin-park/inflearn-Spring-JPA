### 1. 엔티티 설계시 주의점
   1. 엔티티에는 가급적 Setter를 사용하지 말자
      1. Setter가 모두 열려있으면 변경 포인트가 많아서 유지보수가 어려움
   2. 모든 연관관계는 지연로딩으로 설정하자
      1. 즉시로딩은 예측이 어렵고, 어떤 SQL이 실행될지 추적하기 어려움. JPQL에서는 N+1 문제가 자주 발생함
      2. 연관된 엔티티를 함께 DB에서 조회해야 하면 fetch join 또는 엔티티 그래프 기능을 사용하자
      3. XToOne 관계는 기본이 즉시로딩이므로 직접 지연로딩으로 설정해주자
   3. 컬렉션은 필드에서 초기화하자
      1. 컬렉션은 필드에서 바로 초기화하는 것이 안전하다. 
      2. null 문제에서 안전하다.
      3. 하이버네이트는 엔티티를 영속화 할 때, 컬렉션을 감싸서 하이버네이트가 제공하는 내장 컬렉션으로 변경한다. 만약 getOrders() 처럼 임의의 메서드에서 컬렉션을 잘못 생성하면 하이버네이트 내부 메커니즘에 문제가 발생할 수 있다. 따라서 필드레벨에서 생성하는 것이 안전하고 코드도 간결하다.
      ~~~ java
      Member member = new Member();
      System.out.println(member.getOrders().getClass());
      em.persist(member); // 영속화
      System.out.println(member.getOrders().getClass());
      
      //출력 결과
      class java.util.ArrayList
      class org.hibernate.collection.internal.PersistentBag
      ~~~
   4. 테이블, 컬럼명 생성 전략
      - 스프링 부트에서 하이버네이트 기본 매핑 전략을 변경해서 실제 테이블 필드명은 다름
        - https://docs.spring.io/spring-boot/docs/2.1.3.RELEASE/reference/htmlsingle/#howto-configure-hibernate-naming-strategy
        -  http://docs.jboss.org/hibernate/orm/5.4/userguide/html_single/Hibernate_User_Guide.html#naming
      - 하이버네이트 기존 구현 : 엔티티의 필드명을 그대로 테이블의 컬럼명으로 사용 (`SpringPhysicalNamingStrategy`)
      - 스프링 부트 신규 설정 (엔티티(필드) -> 테이블(컬럼))
        - 카멜 케이스 -> (_)언더스코어 ex) memberPoint -> member_point
        - . -> 언더스코어
        - 대문자 -> 소문자
      - **적용 2 단계**
        1. 논리명 생성: 명시적으로 컬럼, 테이블명을 직접 적지 않으면 ImplicitNamingStrategy 사용
           `spring.jpa.hibernate.naming.implicit-strategy` : 테이블이나, 컬럼명을 명시하지 않을 때 논리명 적용
        2. 물리명 적용: `spring.jpa.hibernate.naming.physical-strategy` : 모든 논리명에 적용됨, 실제 테이블에 적용
           (username usernm 등으로 회사 룰로 바꿀 수 있음)
        3. **스프링 부트 기본 설정**
           `spring.jpa.hibernate.naming.implicit-strategy:
           org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy`
           `spring.jpa.hibernate.naming.physical-strategy:
           org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy` 

### 2. 연관관계 편의 메서드 위치
 - 연관관계 편의 메서드는 어디에 위치해도 상관없다. 연관관계 편의 메서드가 어디에 있든 결국 연관관계의 주인 쪽에 값이 설정되기 때문이다.

### 3. 스프링 부트의 테스트
 - 스프링 부트에서 테스트 모드에는 application.yml 등과 같이 설정을 안해줘도 자동으로 메모리 모드로 사용해준다. 
 - 스프링 부트 테스트에서는 ddl-auto: create-drop이 기본이다. 이것은 테스트를 다 돌리고 drop을 날려주는 것

### 4. CASCADE의 사용 범위는?
 - 라이프 사이클에서 동일하게 관련. 1개에 대해서만 참조를 할 떄 사용 가능 ex. 주문과 주문 상품
 - 만약 주문 상품을 다른 곳에서도 사용한다면 CASCADE를 사용하지 않는게 좋음
