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