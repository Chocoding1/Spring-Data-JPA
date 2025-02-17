package study.data_jpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import study.data_jpa.dto.MemberDto;
import study.data_jpa.entity.Member;

import java.util.List;
import java.util.Optional;

/**
 * JpaRepository를 상속받으면, @Repository가 없어도 Spring Data JPA가 알아서 빈으로 등록해준다.
 */
public interface MemberRepository extends JpaRepository<Member, Long> {

    /**
     * Spring Data JPA는 메서드 이름만으로도 JPQL 쿼리를 생성할 수 있다.
     * MemberJpaRepository의 findByUsernameAndAgeGreaterThan() 메서드는 아래의 메서드와 동일하다.
     * 대신 메서드명은 관례에 맞춰서 작성해야 한다.
     * 엔티티의 필드명이 변경되면, 메서드명도 그에 맞춰 변경해줘야 한다. 그러지 않으면 애플리케이션 시작 시점에 오류 발생
     * <장점>
     * 직접 JPQL을 짜지 않아도 돼서 편리하다.
     * 애플리케이션 로딩 시점에 오류를 인지할 수 있다.
     * <단점>
     * 조회 조건이 세 개 이상이 되면 메서드명이 너무 길어진다.
     */
    List<Member> findByUsernameAndAgeGreaterThan(String username, int age);

    /**
     * Spring Data JPA에서 NamedQuery 사용
     *
     * @Query 어노테이션을 사용, Member 엔티티에 지정한 NamedQuery명 지정
     * @Param 어노테이션 사용, 파라미터 전달
     * 위와 같이 단순 메서드명으로 쿼리를 생성하는 경우에는 그냥 파라미터를 넘겨줘도 되지만,
     * NamedQuery와 같이 직접 작성한 JPQL 쿼리가 존재하는 경우에는 @Param 어노테이션을 사용해서 파라미터를 넘겨줘야 한다.
     * <p>
     * 이렇게 이름을 지정하면 그 이름으로 NamedQuery를 찾고, 거기에 있는 JPQL을 실행한다.
     * 이름은 생략 가능하다.
     * Spring Data JPA가 알아서 해당 인터페이스의 "class명.메서드명"으로 이름 찾아준다.
     * 만약 해당 이름의 NamedQuery가 없으면, 위에서 했던 메서드명으로 쿼리를 생성하는 방식으로 진행한다.
     */
//    @Query(name = "Member.findByUsername") // 이름 생략 가능
    List<Member> findByUsername(@Param("username") String username);

    /**
     * @Query 어노테이션 사용
     * NamedQuery가 JPQL을 Entity로 분리해야 해서 불편했다면,
     * @Query는 JPQL을 Entity까지 확장할 필요가 없다.
     * 또한 JPQL에 오타가 발생하면 어플리케이션 로딩 시점에 오류가 발생한다. 정적 쿼리이기 때문에 파싱을 하기 때문
     * 이름 없는 NamedQuery라고 보면 된다.
     * 실무에서는 @Query를 많이 사용한다고 한다.
     * <장점>
     * 메서드명을 간략하게 작성해도 된다.
     * Entity 클래스에 JPQL을 작성하지 않아도 된다.
     * JPQL에 오타가 있으면 어플리케이션 로딩 시점에 오류 발생
     */
    @Query("select m from Member m where m.username = :username and m.age = :age")
    List<Member> findMember(@Param("username") String username, @Param("age") int age);

    /**
     * @Query 사용하여 단순 값 조회
     * 회원명 조회
     */
    @Query("select m.username from Member m")
    List<String> findUsernameList();

    /**
     * @Query 사용하여 DTO 조회
     * DTO 조회 시에는 반드시 DTO 생성자로 조회해야 한다.
     */
    @Query("select new study.data_jpa.dto.MemberDto(m.id, m.username, t.name) from Member m join m.team t")
    List<MemberDto> findMemberDto();

    /**
     * @Query에 Collection 파라미터 사용
     */
    @Query("select m from Member m where m.username in :names")
    List<Member> findByNames(@Param("names") List<String> names);

    /**
     * 다양한 반환 타입 가능
     * Spinrg Data JPA는 유연한 반환 타입을 지원한다.
     */
    List<Member> findListByUsername(String username); // 컬렉션 반환
    Member findMemberByUsername(String username); // 단건 반환
    Optional<Member> findOptionalByUsername(String username); // 단건 Optional 반환

    /**
     * 페이징
     */
    Page<Member> findByAge(int age, Pageable pageable);

    /**
     * 벌크 연산
     * 벌크 연산 쿼리는 @Modifying 어노테이션을 붙여야 한다.
     * 이 어노테이션을 붙여야 JPA의 executeUpdate()를 실행한다.
     * 붙이지 않으면 getSingleResult()나 getResultList()를 실행한다.
     */
    /**
     * 주의점
     * 벌크 연산은 영속성 컨텍스트에 접근해서 데이터를 변경하는 것이 아니라 직접 DB에 접근해서 데이터를 변경한다.
     * 때문에 벌크 연산을 수행한 후에는 영속성 컨텍스트를 초기화(em.clear)시켜 데이터 불일치 문제를 일으키지 않도록 해야 한다.
     * 예를 들어 나이가 20인 회원을 save하면 현재 영속성 컨텍스트에 20살의 회원이 저장된다.
     * 그 뒤에 벌크 연산으로 나이를 1살씩 더하면 해당 벌크 연산은 DB에 직접 접근하여 회원의 나이를 21살로 변경한다. (물론 변경 전에 em.flush를 통해 DB에 우선 저장을 한다.)
     * 그러나 영속성 컨텍스트에는 해당 회원의 나이가 20살 그대로 있는 상태이다.
     * 이 때 만약 이 회원의 나이를 조회한다면 21살이 아닌 20살로 나올 것이다. (영속성 컨텍스트에 존재하는 회원을 우선적으로 가져오니까)
     * 이러한 이유로 벌크 연산 후에는 영속성 컨텍스트를 비워줘야 한다.
     * em.clear도 있지만 더 깔끔한 방법은 @Modifying에 설정하는 방법이다.
     * @Modifying은 clearAutomatically라는 옵션을 설정할 수 있다. (default = false)
     */
    /**
     * 권장법
     * 1. 영속성 컨텍스트 안에 엔티티가 없는 상태에서 벌크 연산을 먼저 수행
     * 2. 영속성 컨텍스트에 엔티티가 존재한다면 벌크 연산 직후 영속성 컨텍스트 초기화
     */
    @Modifying(clearAutomatically = true)
    @Query("update Member m set m.age = m.age + 1 where m.age >= :age")
    int bulkAgePlus(@Param("age") int age);

    /**
     * 페치 조인
     */
    // 단순 JPQL 사용
    @Query("select m from Member m join fetch m.team")
    List<Member> findMemberFetchJoinTeam();

    // findAll() 오버라이드 후, @EntityGraph 사용
    @Override
    @EntityGraph(attributePaths = {"team"})
    List<Member> findAll();

    // JPQL + @EntityGraph 사용
    @EntityGraph(attributePaths = {"team"})
    @Query("select m from Member m")
    List<Member> findMemberEntityGraph();

    // 메서드명 + @EntityGraph 사용
    @EntityGraph(attributePaths = {"team"})
    List<Member> findEntityGraphByUsername(@Param("username") String username);
}
