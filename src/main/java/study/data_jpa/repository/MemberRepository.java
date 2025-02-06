package study.data_jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
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
     * 장점 : 직접 JPQL을 짜지 않아도 돼서 편리하다. / 애플리케이션 로딩 시점에 오류를 인지할 수 있다.
     * 단점 : 조회 조건이 세 개 이상이 되면 메서드명이 너무 길어진다.
     */
    List<Member> findByUsernameAndAgeGreaterThan(String username, int age);

}
