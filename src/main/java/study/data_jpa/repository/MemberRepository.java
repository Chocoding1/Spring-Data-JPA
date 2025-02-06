package study.data_jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import study.data_jpa.entity.Member;

import java.util.List;

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
     *     직접 JPQL을 짜지 않아도 돼서 편리하다.
     *     애플리케이션 로딩 시점에 오류를 인지할 수 있다.
     * <단점>
     *     조회 조건이 세 개 이상이 되면 메서드명이 너무 길어진다.
     */
    List<Member> findByUsernameAndAgeGreaterThan(String username, int age);

    /**
     * Spring Data JPA에서 NamedQuery 사용
     * @Query 어노테이션을 사용, Member 엔티티에 지정한 NamedQuery명 지정
     * @Param 어노테이션 사용, 파라미터 전달
     * 위와 같이 단순 메서드명으로 쿼리를 생성하는 경우에는 그냥 파라미터를 넘겨줘도 되지만,
     * NamedQuery같이 직접 작성한 JPQL 쿼리가 존재하는 경우에는 @Param 어노테이션을 사용해서 파라미터를 넘겨줘야 한다.
     *
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
     *     메서드명을 간략하게 작성해도 된다.
     *     Entity 클래스에 JPQL을 작성하지 않아도 된다.
     *     JPQL에 오타가 있으면 어플리케이션 로딩 시점에 오류 발생
     */
    @Query("select m from Member m where m.username = :username and m.age = :age")
    List<Member> findMember(@Param("username") String username, @Param("age") int age);

    /**
     * <정적 쿼리>
     * 실무에서는 간단한 쿼리는 메서드명을 이용한 쿼리 생성 방법을 사용하고,
     * 조금 복잡한 쿼리는 @Query를 사용한다고 한다.
     * NamedQuery는 잘 사용하지 않는 편
     *
     * 동적 쿼리는 Querydsl 사용
     */

}
