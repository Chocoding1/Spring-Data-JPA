# Spring-Data-JPA
Spring Data JPA 공부 기록


## Spring Data JPA 정의
- JPA에서 Repository를 구현할 때 반복적으로 작성하는 CRUD 코드를 제공하는 라이브러리
- 이 라이브러리 덕에 개발자는 CRUD 코드를 작성할 수고를 덜 수 있다.

## Spring Data JPA 사용법
### <생성>
```java
public interface MemberRepository extends JpaRepository<Member, Long>
```
- Spring Data JPA를 사용하기 위해서는 우선 Repository를 인터페이스로 생성해야 한다.
- 생성한 인터페이스가 JpaRepository<T, ID>를 상속받도록 한다.
- 여기서 T는 해당 Repository가 다루는 Entity class를 의미하고, ID는 해당 Entity의 PK 타입을 의미한다.

### <사용>
- Spring Data JPA의 기능을 사용하는 방법은 3가지가 있다.
1. 메소드명으로 쿼리 생성
2. NamedQuery로 쿼리 생성
3. @Query로 쿼리 생성
- 위 세 가지 방법은 정적 쿼리 생성 시 사용하는 방법이다.
> 동적 쿼리는 Querydsl을 사용한다고 한다.


### 1. 메소드명으로 쿼리 생성
- 메서드명을 관례에 맞춰서 작성하면 Spring Data JPA가 해당 메서드명을 보고 자동으로 JPQL 쿼리를 생성한다.
```java
List<Member> findByUsernameAndAgeGreaterThan(String username, int age);
```
- 위 메서드는 이름이 파라미터로 넘어온 username과 일치하고 나이가 age 이상인 Member를 조회하는 메서드이다.
- JPA에서 em.createQuery()를 사용하여 직접 JPQL을 작성한 것과 달리, 메서드명만 관례에 맞춰 적어주면 JPQL 쿼리가 자동으로 생성된다.
- 대신 Entity의 필드명이 변경되면, 그에 맞춰 메서드명도 변경해줘야 한다. 그러지 않으면 어플리케이션 로딩 시점에 오류가 발생한다.
- 관례는 공식 문서를 통해 알 수 있다.
- https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html

**<장점>**
<br>
- 직접 JPQL을 짜지 않아도 돼서 편리하다.
- 어플리케이션 로딩 시점에 오류를 인지할 수 있다.

**<단점>**
<br>
- 조회 조건이 많아지면 메서드명이 너무 길어진다.


### 2. NamedQuery로 쿼리 생성
- Entity 클래스에 직접 쿼리를 작성하는 방법
- 실무에서는 잘 사용하지 않는 방법
```java
// Entity
@NamedQuery(
        name="Member.findByUsername",
        query="select m from Member m where m.username = :username"
)
public class Member {
}
```
- JPQL을 Repository 안에서 작성하지 않고, Entity까지 끌고 나와 지저분하다.
```java
// Repository (단순 JPA 사용)
public List<Member> findByUsername(String username) {
        return em.createNamedQuery("Member.findByUsername", Member.class)
                .setParameter("username", username)
                .getResultList();
    }
}
```
- em.createNamedQuery() 메서드 사용
- 파라미터로 JPQL 대신 Entity에서 지정한 NamedQuery명 전달
- em.createQuery()와 코드를 치는 양은 비슷하다.
```java
// Repository (Spring Data JPA``사용)
@Query(name = "Member.findByUsername") // 이름 생략 가능
List<Member> findByUsername(@Param("username") String username);
```
- @Query 어노테이션 사용, Entity에 지정한 NamedQuery명 작성
  - 이름을 지정하면 그 이름으로 NamedQuery를 찾고, 그 이름에 매칭되는 JPQL을 실행한다.
- @Param 어노테이션 사용해서 파라미터 전달
  - 1번과 같이 단순 메서드명으로 쿼리를 생성하는 경우에는 그냥 파라미터를 넘겨줘도 되지만,
  - NamedQuery와 같이 직접 작성한 JPQL이 존재하는 경우에는 @Param 어노테이션을 사용해서 파라미터를 넘겨줘야 한다.
- 이름은 생략 가능하다.
  - Spring Data JPA가 알아서 해당 Repository가 다루는 **Entity 클래스명.메서드명**으로 이름을 찾는다.
  - 만약 해당 이름의 NamedQuery가 없으면, 그 때 메서드명으로 쿼리를 생성하는 방식으로 진행한다.

**<장점>**
<br>
- JPQL 쿼리에 오타가 있으면, 어플리케이션 로딩 시점에 오류가 발생한다.
  - NamedQuery는 기본적으로 정적 쿼리이기 때문에 어플리케이션 로딩 시점에 파싱을 한 후, JPQL을 SQL로 만들어놓고 시작한다.
  - 때문에 파싱 과정에서 오타 여부를 확인할 수 있다.
  - em.createQuery()에 작성한 JPQL은 단순 문자열이기 때문에 어플리케이션 로딩 시점에 오타 여부 확인 불가

**<단점>**
<br>
- JPQL이 Repository에 모여있지 않고, Entity에 따로 분리되어 있어 깔끔하지 않다.
- Entity에서 NamedQuery를 작성해도 Repository에서 다시 작성해줘야 한다. (한 번 작성할 일 두 번 작성)

### 3. @Query로 쿼리 생성
- 1번과 2번의 단점을 보완한 방법
- 실무에서 많이 사용하는 방법
```java
// Repository
@Query("select m from Member m where m.username = :username and m.age = :age")
List<Member> findMember(@Param("username") String username, @Param("age")
```
- Repository에서 바로 JPQL 작성
- 이 또한 JPQL에 오타가 발생하면 어플리케이션 로딩 시점에 오류가 발생한다.
- 이름 없는 NamedQuery라고 보면 된다.

**<장점>**
<br>
- 메서드명을 간략하게 작성해도 된다. (1번 단점 보완)
- Entity 클래스에 JPQL을 작성하지 않아도 된다. (2번 단점 보완)
- JPQL에 오타가 있으면 어플리케이션 로딩 시점에 오류가 발생한다.
---
#### 정리
<정적 쿼리>
<br>
- 실무에서는 간단한 쿼리는 메서드명을 이용한 쿼리 생성 방법을 사용하고,
- 조금 복잡한 쿼리는 @query를 사용한다고 한다.
- NamedQuery는 잘 사용하지 않는 편

<동적 쿼리>
<br>
- 동적 쿼리는 Querydsl 사용
---
### <@Query 사용하여 단순 값 조회>
```java
// 회원명 조회
@Query("select m.username from Member m")
List<String> findUsernameList();
```
### <@Query 사용하여 DTO 조회>
```java
@Query("select new study.data_jpa.dto.MemberDto(m.id, m.username, t.name) from Member m join m.team t")
List<MemberDto> findMemberDto();
```
- DTO 조회 시에는 반드시 DTO 생성자로 조회해야 한다. (JPA와 동일)

### <@Query에 Collection 파라미터 사용>
```java
@Query("select m from Member m where m.username in :names")
List<Member> findByNames(@Param("names") List<String> names);
```
- Collection 타입으로 in절에 바인딩할 수 있다.
