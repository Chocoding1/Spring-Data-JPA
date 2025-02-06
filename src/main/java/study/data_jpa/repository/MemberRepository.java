package study.data_jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import study.data_jpa.entity.Member;

/**
 * JpaRepository를 상속받으면, @Repository가 없어도 Spring Data JPA가 알아서 빈으로 등록해준다.
 */
public interface MemberRepository extends JpaRepository<Member, Long> {
}
