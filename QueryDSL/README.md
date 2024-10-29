# QueryDSL 강의

## 1. QueryDSL 설정하기
* SpringBoot 3.x 기준 설정 방법
  * 아래와 같이 build.gradle을 설정하기
    ```java
    plugins {
        id 'java'
        id 'org.springframework.boot' version '3.3.5'
        id 'io.spring.dependency-management' version '1.1.6'
    }
    
    group = 'study'
    version = '0.0.1-SNAPSHOT'
    
    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(17)
        }
    }
    
    configurations {
        compileOnly {
            extendsFrom annotationProcessor
        }
    }
    
    repositories {
        mavenCentral()
    }
    
    dependencies {
        implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
        implementation 'org.springframework.boot:spring-boot-starter-web'
    
        compileOnly 'org.projectlombok:lombok'
        runtimeOnly 'com.h2database:h2'
        annotationProcessor 'org.projectlombok:lombok'
    
        //QueryDSL 추가 시작
        implementation 'com.querydsl:querydsl-jpa:5.0.0:jakarta'
        annotationProcessor "com.querydsl:querydsl-apt:${dependencyManagement.importedProperties['querydsl.version']}:jakarta"
        annotationProcessor "jakarta.annotation:jakarta.annotation-api"
        annotationProcessor "jakarta.persistence:jakarta.persistence-api"
            //QueryDSL 추가 끝
            
        testImplementation 'org.springframework.boot:spring-boot-starter-test'
        testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
        //test 롬복 사용
        testCompileOnly 'org.projectlombok:lombok'
        testAnnotationProcessor 'org.projectlombok:lombok'
    }
    
    tasks.named('test') {
        useJUnitPlatform()
    }
    
    // clean할 때 해당 디렉토리도 비울 수 있도록 설정
    clean {
        delete file('src/main/generated')
    }
    ```
  * Q 타입 생성 확인
    * Gradle -> Tasks -> build -> clean
    * Gradle -> Tasks -> build -> build
    * build -> generated -> sources -> annotaionProcessor -> java -> main 안에 Q파일이 생성된 것을 확인
    * 참고) Q타입은 컴파일 시점에 자동 생성되므로 버전관리에 포함하지 않는 것이 좋다.

## 1. QueryDSL 기본 문법
* 아하 모먼트!
  * JPAQueryFactory를 필드로 제공하면 동시성 문제에 대해서는 어떻게 되는가?
    * JPAQueryFactory를 생성할 때 제공하는 EntityManager에 달려있다.
    * 스프링 프레임워크는 여러 쓰레드에서 동시에 같은 EntityManager에 접근해도, 트랜잭션 마다 별도의 영속성 컨텍스트를 제공하기 때문에 동시성 문제는 걱정하지 않아도 된다.
  * 다음과 같이 설정을 추가하면 실행되는 JPQL을 볼 수 있다.
    * spring.jpa.properties.hibernate.use_sql_comments: true

### 1. 기본 Q-Type 활용
* Q클래스 인스턴스를 사용하는 2가지 방법
  * 별칭 직접 지정 : QMember qMember = new QMember("m");
  * 기본 인스턴스 사용 : QMember qMember = QMember.member;
  * 참고) 같은 테이블을 조인해야 하는 경우가 아니면 기본 인스턴스를 사용하자

* 기본 인스턴스를 static import와 함께 사용
```java
import static study.querydsl.entity.QMember.*;

@Test
public void startQuerydsl3() throws Exception {
        Member findMember = queryFactory
        .select(member)
        .from(member)
        .where(member.username.eq("member1")) // 자동으로 파라미터 바인딩 처리
        .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
        }
```