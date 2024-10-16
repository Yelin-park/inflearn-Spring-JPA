# 스프링 데이터 JPA 강의 정리

## 1. JPA 공통 인터페이스
### 1. 공통 인터페이스 설정
- **JavaConfig 설정 - 스프링 부트 사용시 생략 가능**
  - 스프링 부트 사용시`@SpringBootApplication` 위치를 지정(해당 패키지와 하위 패키지 인식)
  - 만약, 위치가 달라지면 `@EnableJpaRepositories` 필요
```java
@Configuration
@EnableJpaRepositories(basePackages = "jpabook.jpashop.repository")
public class AppConfig {}
```

- **스프링 데이터 JPA가 구현 클래스 대신 생성**

![spring_data_jpa.png](image/spring_data_jpa.png)
  - `org.springframework.data.repository.Repository`를 구현한 클래스는 스캔 대상
    - 스프링 데이터 JPA가 알아서 구현체를 만들고 주입을 해준다.
    - MemberRepository 인터페이스가 동작한 이유이다.
    - 실제 출력해보면 memberRepository.getClass() -> class com.sun.proxy.$ProxyXXX
      - 자바의 기술을 가지고 가짜 클래스를 만들어서 주입
  - `@Repository` 어노테이션 생략 가능하다.
    - 컴포넌트 스캔을 스프링 데이터 JPA가 자동으로 처리
    - JPA 예외를 스프링 예외로 변환하는 과정도 자동으로 처리

### 2. 공통 인터페이스 분석
- JpaRepository 인터페이스 : 공통 CRUD 제공
- 제네릭은 <엔티티 타입, 식별자 타입> 설정
- `JpaRepository` 공통 기능 인터페이스
~~~ java
public interface JpaRepository<T, ID extends Serializable> extends PagingAndSortingRepository<T, ID> {
  ...
}
~~~

- `JpaRepository` 를 사용하는 인터페이스
~~~ java
public interface MemberRepository extends JpaRepository<Member, Long> {}
~~~

- **공통 인터페이스 구성**

![spring_data_and_jpa_interface.png](image/spring_data_and_jpa_interface.png)

- **제네릭 타입**
  - `T` : 엔티티
  - `ID` : 엔티티의 식별자 타입
  - `S` : 엔티티와 그 자식 타입
- **주요 메서드**
  - `save(S)` : 새로운 엔티티는 저장하고 이미 있는 엔티티는 병합한다.
  - `delete(T)` : 엔티티 하나를 삭제한다. 내부에서 `EntityManager.remove()` 호출
  - `findById(ID)` : 엔티티 하나를 조회한다. 내부에서 `EntityManager.find()` 호출
  - `getOne(ID)` : 엔티티를 프록시로 조회한다. 내부에서 `EntityManager.getReference()` 호출
  - `findAll(…)` : 모든 엔티티를 조회한다. 정렬(`Sort`)이나 페이징(`Pageable`) 조건을 파라미터로 제공할 수 있다.
  
- 참고: `JpaRepository` 는 대부분의 공통 메서드를 제공한다.

## 3. 쿼리 메서드 기능
* **쿼리 메서드 기능 3가지**
  * 메서드 이름으로 쿼리 생성
  * 메서드 이름으로 JPA NamedQuery 호출
  * `@Query` 어노테이션을 사용해서 리파지토리 인터페이스에 쿼리 직접 정의

### 1. 메서드 이름으로 쿼리 생성
~~~ java
public interface MemberRepository extends JpaRepository<Member, Long> {
    List<Member> findByUsernameAndAgeGreaterThan(String username, int age);
}
~~~
* 스프링 데이터 JPA는 메서드 이름을 분석해서 JPQL을 생성하고 실행한다.
  * 엔티티의 필드명이 변경되면 인터페이스에 정의한 메서드 이름도 꼭 함께 변경해야한다.
  * 변경하지 않으면 애플리케이션 시작하는 시점에 No Property라는 오류가 발생한다.
* 쿼리 메서드 필터 조건은 스프링 데이터 JPA 공식 문서를 참고하자!
  * https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html

* **스프링 데이터 JPA가 제공하는 쿼리 메서드 기능**
  * 조회: find…By ,read…By ,query…By get…By
    * 예:) findHelloBy 처럼 ...에 식별하기 위한 내용(설명)이 들어가도 된다. 
  * COUNT: count…By 반환타입 `long`
  * EXISTS: exists…By 반환타입 `boolean`
  * 삭제: delete…By, remove…By 반환타입 `long`
  * DISTINCT: findDistinct, findMemberDistinctBy
  * LIMIT: findFirst3, findFirst, findTop, findTop3

### 2. JPA NamedQuery
* 쿼리에 이름을 부여하고 호출하는 기능이다.
* 장점 : 애플리케이션 로딩 시점에 문법 오류가 있으면 오류를 보여준다.
* 사용 방법
  * 엔티티에 @NamedQuery 어노테이션으로 Named 쿼리 정의
    ``` java
    @Entity
    @NamedQuery(
        name="Member.findByUsername",
        query="select m from Member m where m.username = :username")
    public class Member {
      ...
    }
    ```
    
  * **JPA를 직접 사용해서 Named 쿼리 호출**
    ```java
    public class MemberRepository {
        public List<Member> findByUsername(String username) {
            List<Member> resultList =
            em.createNamedQuery("Member.findByUsername", Member.class)
                .setParameter("username", username)
                .getResultList();
        }
    }
    ```

* **스프링 데이터 JPA로 Named 쿼리 호출**
    * @Query를 생략하고 메서드 이름만으로 Named 쿼리를 호출할 수 있다.
    * 스프링 데이터 JPA는 선언한 도메인 클래스 + . + 메서드 이름으로 Named 쿼리를 찾아서 실행한다.
    * 만약 실행할 Named 쿼리가 없으면 메서드 이름으로 쿼리 생성 전략을 사용한다.
    * 필요하면 전략을 변경할 수 있지만 권장하는 방법은 아니다.
  ```java
    public interface MemberRepository
        extends JpaRepository<Member, Long> { //** 여기 선언한 Member 도메인 클래스
  
        @Query(name = "Member.findByUsername") // 아래와 같이 해당 어노테이션이 없어도됨
        List<Member> findByUsername(@Param("username") String username);
  
        List<Member> findByUsername(@Param("username") String username);
    }
  ```

