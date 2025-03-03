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

---

### <반환 타입>
- Spring Data JPA는 유연한 반환 타입을 지원한다.
```java
List<Member> findListByUsername(String username); // 컬렉션 반환
Member findMemberByUsername(String username); // 단건 반환
Optional<Member> findOptionalByUsername(String username); // 단건 Optional 반환
```
#### <주의할 점>
**1. List 조회 시 조건 파라미터를 잘못 입력했을 때 반환 값은 null이 아닌 Empty List이다.**
```java
// MemberRepository
public interface MemberRepository Extends JpaRepository<Member, Long> {
        List<Member> findListByUsername(String username); // 컬렉션 반환
}

// 안 좋은 코드
List<Member> result = memberRepository.findListByUsername("aaa");
if (result != null) {
        '''
}
// 권장 코드
List<Member> result = memberRepository.findListByUsername("aaa");
if (result.size() == 0) {
        '''
}
```
  - List 포함 Collection은 null을 반환하지 않기 때문에 그에 맞는 코드를 짜자.<br>
  
**2. 단건 조회(getSingleResult()) 시 값이 없을 때는 Optional을 사용하자.**
  - 순수 JPA의 경우 단건 조회 시 값이 없을 때는 NoResultExceptions을 터뜨린다.
  - Spring Data JPA는 이 예외를 알아서 처리해서 null로 반환한다.
  - 예외가 터지는 것보다는 null이 넘어오는 것이 훨씬 낫다.
  - Java 8부터 Optional이 생겼으므로, Optional을 사용해서 처리하는 것이 좋다.

---

### <Paging & Sorting>
- 검색 조건 : 나이
- 정렬 조건 : 이름 - 내림차순

**<순수 JPA 활용한 paging>**
```java
// MemberRepository
public List<Member> findByPage(int age, int offset, int limit) {
        return em.createQuery("select m from Member m where m.age = :age order by m.username desc", Member.class)
                .setParameter("age", age)
                .setFirstResult(offset) // 몇 번째부터 가져올 지
                .setMaxResults(limit) // 몇 개를 가져올 지
                .getResultList();
  }
/**
* 보통 페이징을 할 때, 현재 있는 페이지가 몇 번째 페이지인지 계산을 하기 위해 전체 개수도 계산을 해야한다.
* 단순 개수를 계산하는 것이기 때문에 정렬은 할 필요 없다.
*/
public long totalCount(int age) {
        return em.createQuery("select count(m) from Member m where m.age = :age", Long.class)
                .setParameter("age", age)
                .getSingleResult();
    }
```
- 순수 JPA를 활용하여 paging을 할 때는 totalCount를 계산하여 직접 페이지 수를 계산해줘야 한다.
- 해당 페이지가 최초 페이지인지, 현재 페이지가 마지막 페이지인지 등..

**<Spring Data JPA 활용한 paging>**
- Spring Data JPA는 paging과 sorting을 표준화했다.

**<paging & sorting 파라미터>**
```java
org.springframework.data.domain.Sort // 정렬 기능
org.springframework.data.domain.Pageable // 페이징 기능(내부에 Sort 포함)
```
- 위의 두 가지 인터페이스로 정렬과 페이징을 통일시켰다.

**<반환 타입>**
```java
org.springframework.data.domain.Page // 추가 count 쿼리 결과를 포함하는 페이징
org.springframework.data.domain.Slice // 추가 count 쿼리 없이 다음 페이지 여부만 확인 가능(내부적으로 limit + 1 조회)
List(Java Collections) // 추가 count 쿼리 없이 결과만 반환
```

**<활용 예제>**<br>

**Page**
```java
// MemberRepository
Page<Member> findByAge(int age, Pageable pageable);
```
```java
// MemberRepositoryTest
@Test
@DisplayName("Spring Data JPA 활용한 paging")
void paging() {
        //given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 10));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 10));
        memberRepository.save(new Member("member5", 10));
        memberRepository.save(new Member("member6", 10));
        memberRepository.save(new Member("member7", 10));
        
        int age = 10;
        // 0페이지에서 3개 가져와(Data JPA는 페이지를 0번부터 센다)
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));
        
        //when
        Page<Member> page = memberRepository.findByAge(age, pageRequest);
        
        //then
        List<Member> content = page.getContent(); // 가져온 페이지에서 안의 내용들을 꺼내는 함수
        long totalElements = page.getTotalElements(); // totalCount
        
        assertThat(content.size()).isEqualTo(3); // 가져온 데이터의 개수
        assertThat(totalElements).isEqualTo(7); // 전체 개수
        assertThat(page.getNumber()).isEqualTo(1); // 현재 페이지 수
        assertThat(page.getTotalPages()).isEqualTo(3); // 전체 페이지 수
        assertThat(page.isFirst()).isTrue(); // 현재 페이지가 첫 번째 페이지인지
        assertThat(page.hasNext()).isTrue(); // 다음 페이지가 존재하는지
    }
```
1. Pageable 인터페이스의 구현체인 PageRequest 객체를 만든다.
```java
// username으로 내림차순 정렬 후, 0 페이지에서 3개의 데이터 가져와. (페이지는 0부터 시작)
PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));
```
2. paging 메서드를 호출하면서 파라미터로 PageRequest를 같이 넘겨준다. (보통 PageRequest를 많이 쓴다고 한다.)
```java
Page<Member> page = memberRepository.findByAge(age, pageRequest);
```
3. 반환된 Page 객체를 가지고 원하는 작업을 진행한다.
```java
List<Member> content = page.getContent(); // 가져온 페이지에서 안의 내용들을 꺼내는 함수
long totalElements = page.getTotalElements(); // totalCount
```
- Spring Data JPA의 편리한 점은 totalCount를 직접 계산할 필요가 없다는 것이다.
- 반환 타입이 Page이면 totalCount 쿼리까지 같이 날린다. 또한 totalCount 쿼리를 날릴 때는 알아서 정렬을 하지 않는다. (최적화)
- 그래서 Page 객체에 getTotalElements() 메서드만 호출하면 전체 개수를 알 수 있다.
- 그 외에도 다양한 메서드들을 편리하게 사용할 수 있다.

**Slice**
- paging을 하되, totalCount는 계산하지 않는 방법
- 모바일의 경우, 스크롤을 쭉 내리다 보면 페이지 번호가 있는 것이 아니라 **더보기** 버튼이 있는 경우를 본 적이 있을 것이다.
- 이렇게 페이지 번호를 매기지 않아도 될 때 쓰는 것이 Slice이다.
- Slice는 totalCount를 계산하지 않는 대신 다음 페이지가 있는지는 알아야 한다. (그래야 더보기 버튼을 보여줄지 말지 결정할 수 있으니까)
- 그래서 Slice는 limit보다 1개 더 가져오는 쿼리를 날린다.
```java
// MemberRepository
Slice<Member> findByAge(int age, Pageable pageable);
```
```java
// MemberRepositoryTest
@Test
@DisplayName("Spring Data JPA 활용한 slicing")
void paging() {
        //given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 10));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 10));
        memberRepository.save(new Member("member5", 10));
        memberRepository.save(new Member("member6", 10));
        memberRepository.save(new Member("member7", 10));
        
        int age = 10;
        // 0페이지에서 3개 가져와(Data JPA는 페이지를 0번부터 센다)
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));
        
        //when
        Slice<Member> page = memberRepository.findByAge(age, pageRequest);
        
        //then
        List<Member> content = page.getContent(); // 가져온 페이지에서 안의 내용들을 꺼내는 함수
        // long totalElements = page.getTotalElements(); // totalCount (사용 불가)
        
        assertThat(content.size()).isEqualTo(3); // 가져온 데이터의 개수
        // assertThat(totalElements).isEqualTo(7); // 전체 개수 (사용 불가)
        assertThat(page.getNumber()).isEqualTo(1); // 현재 페이지 수
        // assertThat(page.getTotalPages()).isEqualTo(3); // 전체 페이지 수 (사용 불가)
        assertThat(page.isFirst()).isTrue(); // 현재 페이지가 첫 번째 페이지인지
        assertThat(page.hasNext()).isTrue(); // 다음 페이지가 존재하는지
    }
```
- 사용법은 Page와 동일하다.
- 그러나 다른 점은 위의 PageRequest를 초기화할 때, 3개를 가져온다고 했지만, 실제 쿼리에는 4개를 가져오는 쿼리를 날린다.
- 그렇게 다음 페이지의 여부만 판단하는 것이 Slice의 특징이다.
- 또한 totalCount 쿼리를 날리지 않기 때문에 totalCount와 관련된 메서드는 사용할 수 없다.

**List**
- 단순히 몇 개의 데이터만 가져오고 싶고, 그 뒤에 페이지가 더 존재하는지 여부는 알 필요가 없을 때 사용하면 된다.
- 당연히 페이지 관련 메서드는 사용할 수 없다.

**<paging 결과를 DTO로 변환>**
- api의 경우, Entity를 그대로 반환하면 절대 안 된다.
- 이럴 때 paging 결과를 DTO로 변환할 수 있다.
```java
Page<MemberDto> dtoPage = page.map(member -> new MemberDto(member.getId(), member.getName(), ...));
```
- paging의 결과값을 map()을 통해 DTO로 변환해주기만 하면 된다.

---

### <벌크성 수정 쿼리>
**<순수 JPA 활용한 벌크 연산>**
```java
// 특정 나이 이상 사람들의 나이 + 1
public int bulkAgePlus(int age) {
        // 수정된 데이터 수 반환
        return em.createQuery("update Member m set m.age = m.age + 1 where m.age >= :age")
                .setParameter("age", age)
                .executeUpdate();
    }
```
- 다른 쿼리와 달리 executeUpdate() 메서드를 호출하면 벌크성 수정 쿼리를 날린다.
- 반환값은 수정한 데이터의 수이다.

**<Spring Data JPA 활용한 벌크 연산>**
```java
// 특정 나이 이상 사람들의 나이 + 1
@Modifying(clearAutomatically = true)
@Query("update Member m set m.age = m.age + 1 where m.age >= :age")
int bulkAgePlus(@Param("age") int age);
```
- Spring Data JPA에서는 벌크성 수정 쿼리를 날릴 때, @Modifying 어노테이션을 붙여야 한다.
- 이 어노테이션을 붙여야 JPA의 executeUpdate() 메서드를 실행한다.
- 붙이지 않으면 getSingleResult()나 getResultList()를 실행한다.

<주의할 점>
- 벌크 연산은 영속성 컨텍스트에 접근해서 데이터를 변경하는 것이 아니라 직접 DB에 접근해서 데이터를 변경한다.
- 때문에 벌크 연산을 수행한 후에는 영속성 컨텍스트를 초기화(em.clear)시켜 데이터 불일치 문제를 일으키지 않도록 해야 한다.
- 예를 들어 나이가 20인 회원을 save하면 현재 영속성 컨텍스트에 20살의 회원이 올라간다.
- 그 뒤에 벌크 연산으로 나이를 1살씩 더하면 해당 벌크 연산은 DB에 직접 접근하여 회원의 나이를 21살로 변경한다. (물론 변경 전에 em.flush를 통해 DB에 우선 저장을 한다.)
- 그러나 영속성 컨텍스트에는 해당 회원의 나이가 20살 그대로 있는 상태이다. (DB에 바로 접근했기 때문에)
- 이 때 만약 이 회원의 나이를 조회한다면 21살이 아닌 20살로 나올 것이다. (영속성 컨텍스트에 존재하는 회원을 우선적으로 가져오니까)
- 이러한 이유로 벌크 연산 후에는 영속성 컨텍스트를 비워줘야 한다.
- 영속성 컨텍스트를 초기화하는 방법에는 em.clear도 있지만 더 깔끔한 방법은 @Modifying에 설정하는 방법이다.
- @Modifying은 clearAutomatically라는 옵션을 설정할 수 있다. (default = false)

<권장법>
1. 영속성 컨텍스트 안에 엔티티가 없는 상태에서 벌크 연산을 먼저 수행
2. 영속성 컨텍스트에 엔티티가 존재한다면 벌크 연산 직후 영속성 컨텍스트 초기화

---

### <EntityGraph(엔티티그래프)>
- 관련된 Entity들을 하나의 SQL로 모두 조회하는 방법
- 지연 로딩 전략은 많은 경우에 N + 1 문제를 일으킨다.
- 이를 해결하기 위해 JPA에서는 fetch join이라는 기능을 제공한다.
```java
// Member를 조회하면서 Member가 속한 Team도 같이 조회하는 쿼리
// JPQL + fetch join 사용
@Query("select m from Member m join fetch m.team")
List<Member> findMemberFetchJoinTeam();
```
- Spring Data JPA는 JPA가 제공하는 EntityGraph 기능을 편리하게 사용할 수 있도록 한다.
- Spring Data JPA는 @EntityGraph라는 어노테이션을 제공하는데, 이 어노테이션은 fetch join의 간편 버전이라고 생각하면 된다.
**1. @EntityGraph + 공통 메서드 override**
   ```java
   @Override
   @EntityGraph(attributePaths = {"team"})
   List<Member> findAll();
   ```
   - 공통 메서드인 findAll()을 오버라이드한 후, @EntityGraph라는 어노테이션을 추가하고 attributePaths 속성에 함께 조회하고자 하는 객체를 지정하면 된다.
**2. @EntityGraph + JPQL**
   ```java
   @EntityGraph(attributePaths = {"team"})
   @Query("select m from Member m")
   List<Member> findMemberEntityGraph();
   ```
   - JPQL과도 같이 사용할 수 있다.
**3. @EntityGraph + 메서드명**
   ```java
   // 특정 username을 가진 Member를 해당 Member가 속한 Team과 함께 조회
   @EntityGraph(attributePaths = {"team"})
   List<Member> findEntityGraphByUsername(@Param("username") String username);
   ```

**<권장법>**
- 간단한 쿼리의 경우 @EntityGraph 사용
- 복잡한 쿼리의 경우 JPQL의 fetch join 사용

---

### <JPA Hint & Lock>
**JPA Hint**
- JPA 쿼리를 날릴 때, JPA 구현체인 하이버네이트한테 제공하는 힌트
- 데이터베이스한테 날리는 SQL 힌트 X
<사용 예>
```java
// MemberRepository
@QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
Member findReadOnlyByUsername(String username);
```
- 보통 엔티티를 조회하면 조회한 엔티티는 영속성 컨텍스트에 들어간다.
- 그 때 JPA에는 Dirty Checking이라는 변경 감지 기능이 있기 때문에 조회한 엔티티에 대한 스냅샷, 즉 동일한 엔티티 객체를 하나 더 만든다.
- 변경 감지 시에 원본 객체와 비교해야 하니까
- 그러나 문제는 단순히 조회만 하는 쿼리를 날린다고 해도 객체를 두 개씩 만든다는 것이다.
- 이렇게 되면 메모리를 더 먹는데, 이 때 사용할 수 있는 것이 위와 같은 JPA Hint 기능이다.
- 메서드에 @QueryHint를 사용하여 readOnly 속성을 설정하면, 해당 메서드를 호출했을 때 조회용이라는 것을 인식하고 객체를 한 개만 만들어둔다.
- 실무에서 이 정도 상황 외에는 크게 사용할 일 없다고 한다.
<주의점>
- 그렇다고 모든 조회용 쿼리에 readOnly JPA Hint를 사용한다고 해서 큰 성능 개선이 발생하지는 않는다.
- 대부분의 성능 저하 원인은 복잡한 쿼리가 잘못 나가서 생기지, 조회용 쿼리에 객체를 두 개씩 만든다고 큰 성능 저하가 생기지는 않는다.
- 따라서 진짜 중요하고 트래픽이 많은 몇몇의 api에 readOnly를 넣는 거지, 모든 조회용 쿼리에 넣는 것은 별 도움이 되지 않는다.
- 즉 이런 경우는 성능 테스트 후에 결정하는 것이 좋다.
