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

### 3. @Query, 리포지토리 메소드에 쿼리 정의하기
* `org.springframework.data.jpa.repository.Query` 어노테이션을 사용
* 실행할 메서드에 정적 쿼리를 직접 작성하므로 이름 없는 Named 쿼리라 할 수 있음
* JPA Named 쿼리처럼 애플리케이션 실행 시점에 문법 오류를 반결할 수 있음
```java
public interface MemberRepository extends JpaRepository<Member, Long> {
    @Query("select m from Member m where m.username= :username and m.age = :age")
    List<Member> findUser(@Param("username") String username, @Param("age") int age);
}
```

### 4. @Query를 사용하여 값, DTO 조회하기
* 단순히 값 하나를 조회하는 방법
```markdown
@Query("select m.username from Member m")
List<String> findUsernameList();
```

* DTO로 직접 조회하는 방법
  * new 명령어 사용 필요
```markdown
@Query("select new study.data_jpa.dto.MemberDto(m.id, m.username, t.name) from Member m join m.team t")
List<MemberDto> findMemberDto();
```

### 5. 파라미터 바인딩
* 파라미터 바인딩에는 2가지가 있다.
  * 위치 기반
  * 이름 기반
* 코드 가독성과 유지보수를 위해 이름 기반 파라미터 바인딩을 사용하자
```markdown
select m from Member m where m.username = ?0 //위치 기반
select m from Member m where m.username = :name //이름 기반
```
* 파라미터 바인딩
```java
import org.springframework.data.repository.query.Param
public interface MemberRepository extends JpaRepository<Member, Long> {
    @Query("select m from Member m where m.username = :name")
    Member findMembers(@Param("name") String username);
}
```

* 컬렉션 파라미터 바인딩
  * Collection 타입으로 in절 지원
```java
@Query("select m from Member m where m.username in :names")
List<Member> findByNames(@Param("names") List<String> names);
```

### 6. 반환 타입
스프링 데이터 JPA는 유연한 반환 타입을 지원한다.
```markdown
List<Member> findByUsername(String name); //컬렉션
Member findByUsername(String name); //단건
Optional<Member> findByUsername(String name); //단건 Optional
```
* 조회 결과가 많거나 없으면?
  * 컬렉션은 결과가 없다면 빈 컬렉션을 반환한다
  * 단건 조회시
    * 결과가 없으면 null을 반환
    * 결과가 2건 이상이면 `javax.persistence.NonUniqueResultException`이 발생
* 참고
  * 단건으로 지정한 메서드를 호출하면 스프링 데이터 JPA는 내부에서 JPQL의 `Query.getSingleResult()` 메서드를 호출한다.
  * 이 메서드를 호출했을 때 결과가 없으면 `javax.persistence.NoResultException` 예외가 발생한다.
  * 스프링 데이터 JPA는 단건을 조회할 때 이 예외가 발생하면 예외를 무시하고 대신에 null을 반환한다.
  * Optional을 사용하면 Optional.empty가 넘어온다.

### 7. 순수 JPA 페이징과 정렬
* 조건
  * 나이가 10살
  * 이름으로 내림차순
  * 첫 번째 페이지, 페이지당 보여줄 데이터는 3건
* JPA 페이징 리포지토리 코드
  * setFirstResult로 시작 페이지를 지정
  * setMaxResults로 페이지당 보여줄 데이터 개수를 지정
```java
public List<Member> findByPage(int age, int offset, int limit) {
    return em.createQuery("select m from Member m where m.age = :age order by m.username desc", Member.class)
        .setParameter("age", age)
        .setFirstResult(offset)
        .setMaxResults(limit)
        .getResultList();
    }

public long totalCount(int age) {
    return em.createQuery("select count(m) from Member m where m.age = :age", Long.class)
        .setParameter("age", age)
        .getSingleResult();
    }
```

### 8. 스프링 데이터 JPA 페이징과 정렬
* 페이징과 정렬 파라미터
  * `org.springframework.data.domain.Sort` : 정렬 기능
  * `org.springframework.data.domain.Pageble` : 페이징 기능(내부에 Sort 포함)
* 특별한 반환 타입
  * `org.springframework.data.domain.Page` : 추가 count 쿼리 결과를 포함하는 페이징
  * `org.springframework.data.domain.Slice` : 추가 count 쿼리 없이 다음 페이지만 확인 가능(내부적으로 limit + 1 조회)
    * ex) 다음 페이지가 있는 걸 확인하고 더보기 할 수 있도록 간단하게 처리할 수 있음
  * `List`(자바 컬렉션) : 추가 count 쿼리 없이 결과만 반환 
* 페이징과 정렬 사용 예제
```markdown
Page<Member> findByUsername(String name, Pageable pageable); //count 쿼리 사용
Slice<Member> findByUsername(String name, Pageable pageable); //count 쿼리 사용 안함
List<Member> findByUsername(String name, Pageable pageable); //count 쿼리 사용 안함
List<Member> findByUsername(String name, Sort sort);
```
* 조건
  * 나이가 10살
  * 이름으로 내림차순
  * 첫 번째 페이지, 페이지당 보여줄 데이터는 3건
* Page 사용 예제 정의 코드 
  * 두 번째 파라미터로 받은 `Pageable`은 인터페이스다. 따라서 실제 사용할 때는 해당 인터페이스를 구현한 `org.springframework.data.domain.PageRequest` 객체를 사용한다.
  * `PageRequest` 생성자의 첫 번째 파라미터에는 현재 페이지를, 두 번째 파라미터에는 조회할 데이터 수를 입력한다. 여기에 추가로 정렬 정보도 파라미터로 사용할 수 있다. 참고로 페이지는 0부터 시작한다.
    * ```markdown
      PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));
      ```
    * sort 조건이 복잡해지면 PageRequest보다 직접 쿼리에 넣어서 풀어내기  
  * getContent : 조회된 데이터
  * size : 조회된 데이터 수
  * getTotalElements : 전체 데이터 수
  * getNumber : 페이지 번호
  * getTotalPage : 전체 페이지 번호
```java
public interface MemberRepository extends Repository<Member, Long> {
    Page<Member> findByAge(int age, Pageable pageable);
}
```

* count 쿼리는 조인할 필요가 없기 때문에 조회 쿼리가 복잡하거나 성능이 안나온다면 count 쿼리를 분리하여 사용할 수 있다.
  * ```java
    @Query(value = "select m from Member m",
    countQuery = "select count(m.username) from Member m")
    Page<Member> findMemberAllCountBy(Pageable pageable);
    ```

* Page를 유지하면서 엔티티를 DTO로 변환하기
  * 절대 엔티티 자체를 외부에 노출시키면 안된다!!
  ```java
  Page<Member> page = memberRepository.findByAge(10, pageRequest);
  Page<MemberDto> dtoPage = page.map(m -> new MemberDto());
  ```

* 참고) 스프링 부트 3, 하이버네이트 6 - left join 최적화
  * 하이버네이트 6에서 의미없는 left join을 최적화하고 있다.
  ```java
  @Query(value = "select m from Member m left join m.team t")  
  Page<Member> findByAge(int age, Pageable pageable);
  
  // SQL 실행 결과
  // select m1_0.member_id, m1_0.age, m1_0.team_id, m1_0.username from member m1_0
   ```
  * 실행한 JPQL을 보면 select m from Member m left join m.team t 쿼리로 left join을 사용하고 있다.
  * Member와 Team을 조인을 하지만 이 쿼리에서는 Team을 전혀 사용하지 않는다. 사실상 이 JPQL은 select m from Member m 이다.
  * left join이기 때문에 왼쪽에 있는 Member 자체를 다 조회한다는 뜻이다. 만약, select나 where에 team의 조건이 들어간다면 정상적인 join 문이 보인다.
  * JPA는 이 경우 최적화를 해서 join없이 해당 내용으로만 SQL을 만드는 것이다.

### 9. 벌크성 수정 쿼리
* JPA를 사용한 벌크성 수정 쿼리
* ```java
  public int bulkAgePlus(int age) {
    int resultCount = em.createQuery(
            "update Member m set m.age = m.age + 1" +
            "where m.age >= :age")
            .setParameter("age", age)
            .executeUpdate();
        return resultCount;
    }
  ```

* 스프링 데이터 JPA를 사용한 벌크성 수정 쿼리
  * ```java
    @Modifying(clearAutomatically = true)
    @Query("update Member m set m.age = m.age + 1 where m.age >= :age")
    int bulkAgePlus(@Param("age") int age);
    ```
  * 반환 타입은 int로!
  * 벌크성 수정, 삭제 쿼리는 `@Modifying` 어노테이션을 꼭 넣어줘야한다.
    * 사용하지 않으면 `org.hibernate.hql.internal.QueryExecutionRequestException: Not supported for DML operations` 예외 발생
  * 벌크 연산은 영속성 컨텍스트를 무시하고 실행하기 때문에, 영속성 컨텍스트에 있는 엔티티의 상태와 DB에 엔티티 상태가 달라질 수 있다.
  * 권장 방안)
    * 영속성 컨텍스트에 엔티티가 없는 상태에서 벌크 연산을 먼저 실행한다.
    * 부득이하게 영속성 컨텍스트에 엔티티가 있으면 벌크 연산 직후 영속성 컨텍스트를 초기화한다.
  * 벌크성 쿼리를 실행하고나서 영속성 컨텍스트 초기화 : `@Modifying(clearAutomatically = true)`, 이 옵션의 기본값은 false이다.
    * 이 옵션 없이 회원을 findById로 다시 조회하면 영속성 컨텍스트에 과거 값이 남아서 문제가 될 수 있다. 만약, 다시 조회해야 하면 꼭 영속성 컨텍스트를 초기회해야한다.

### 10. @EntityGraph
* 연관된 엔티티들을 SQL 한번에 조회하는 방법으로 JPQL에서는 fetch join을 사용했었다. 
* 스프링 데이터 JPA에서는 엔티티 그래프 기능을 사용하면 JPQL 없이 페치 조인을 사용할 수 있다.(JPQL + 엔티티 그래프 기능)
* 간단하게 말하면 페치 조인의 간편 버전이며, LEFT OUTER JOIN 사용한다.
* 복잡한 쿼리는 JPQL로 페치 조인을 사용하고 간단하다면 메서드 이름 쿼리로 엔티티 그래프를 사용해보자
* EntityGraph 사용 방법
  * ```java
    //공통 메서드 오버라이드
    @Override
    @EntityGraph(attributePaths = {"team"})
    List<Member> findAll();
    
    //JPQL + 엔티티 그래프
    @EntityGraph(attributePaths = {"team"})
    @Query("select m from Member m")
    List<Member> findMemberEntityGraph();
    
    //메서드 이름으로 쿼리에서 특히 편리하다.
    @EntityGraph(attributePaths = {"team"})
    List<Member> findByUsername(String username)
    ```
* NamedEntityGraph 사용 방법
  * ```java
    // Member Entity
    @NamedEntityGraph(name = "Member.all", attributeNodes =
    @NamedAttributeNode("team"))
    @Entity
    public class Member {}
    
    // MemberRepository
    @EntityGraph("Member.all")
    @Query("select m from Member m")
    List<Member> findMemberEntityGraph();
    ```

### 11. JPA Hint & Lock
* **JPA Hint란?**
  * JPA 쿼리 힌트, SQL 힌트가 아니라 JPA 구현체에게 제공하는 힌트이다.
  * 스냅샷을 안만들고, 읽기 전용이라고 인식하고 데이터 변경을 하지 않는다.
  * Query Hint 예제
    ```java
    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Member findReadOnlyByUsername(String username);
    ```
  * Query Hint Page 예제
    ```java
    @QueryHints(value = { @QueryHint(name = "org.hibernate.readOnly", value = "true")}, forCounting = true)
    Page<Member> findByUsername(String name, Pageable pageable);
    ```
    * `org.springframework.data.jpa.repository.QueryHints` 어노테이션을 사용
    * `forCounting` : 반환 타입으로 `Page` 인터페이스를 적용하면 추가로 호출하는 페이징을 위한 count 쿼리도 쿼리 힌트 적용(기본값 `true`)
    
* **Lock**
  * DB에서 select 할 때 건들지 말라고 Lock을 걸 수 있음
  * `org.springframework.data.jpa.repository.Lock` 어노테이션을 사용
    ```java
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Member findLockByUsername(String username);
    
    // 쿼리 결과
    select
    m1_0.member_id,
    m1_0.age,
    m1_0.team_id,
    m1_0.username
    from
    member m1_0
    where
    m1_0.username=? for update
    ```
  * 구매한 책에서 참고하려면 16.1 트랜잭션과 락 절을 참고

## 4. 확장 기능
### 1. 사용자 정의 리포지토리 구현
* 스프링 데이터 JPA 리포지토리는 인터페이스만 정의하고 구현체는 스프링이 자동 생성한다.
* 스프링 데이터 JPA가 제공하는 인터페이스를 직접 구현하면 구현해야 하는 기능이 너무 많다.
* 다양한 이유로 인터페이스의 메서드를 직접 구현하고 싶다면?
  * JPA 직접 사용(EntityManager)
  * 스프링 JDBC Template 사용
  * MyBaits를 연결해서 사용
  * 데이터 베이스 커넥션을 직접 연결해서 사용
  * Querydsl 사용

* **구현하는 방법**
  * 사용자 정의 인터페이스 생성 -> 사용자 정의 인터페이스 구현 클래스 생성 -> 사용자 인터페이스 상속 -> 호출 
  * 사용자 정의 인터페이스
    * ```java
      public interface MemberRepositoryCustom {
        List<Member> findMemberCustom();
      }
      ```
      
  * 사용자 정의 인터페이스 구현 클래스
    * ```java
      @RequiredArgsConstructor
      public class MemberRepositoryImpl implements MemberRepositoryCustom {

         private final EntityManager em;

         @Override
         public List<Member> findMemberCustom() {
           return em.createQuery("select m from Member m").getResultList();
         }
      }
      ```
      
  * 사용자 정의 인터페이스 상속
    * ```java
      public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom { }
      ```

  * 사용자 정의 메서드 호출
    * ```java
      List<Member> result = memberRepository.findMemberCustom();
      ```

* 사용자 정의 구현 클래스
  * 규칙 : 리포지토리 인터페이스 이름 + `Impl` ex)MemberRepositoryImpl
    * 스프링 데이터 2.x 부터는 사용자 정의 인터페이스 이름 + `Impl` 방식도 지원(해당 방식이 직관적)
  * 규칙을 지켜야 스프링 데이터 JPA가 인식해서 스프링 빈으로 등록
  * Impl 대신 다른 이름으로 변경하고 싶다면 XML 설정 또는 JavaConfig 설정을 하면 된다.
    * XML 설정
    ```xml
    <repositories base-package="study.datajpa.repository" repository-impl-postfix="Impl" />
    ```
    * JavaConfig 설정
    ```java
    @EnableJpaRepositories(basePackages = "study.datajpa.repository", repositoryImplementationPostfix = "Impl")
    ```
