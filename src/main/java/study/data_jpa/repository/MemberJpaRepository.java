package study.data_jpa.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import study.data_jpa.entity.Member;

import java.util.List;
import java.util.Optional;

@Repository
public class MemberJpaRepository {

    /**
     * PersistenceContext라는 어노테이션을 사용하면 스프링 컨테이너가 영속성 컨텍스트라고 불리는 EntityManager라는 애를 가져다 준다.
     * 이 EntityManager는 DB와 관련된 CRUD 기능을 편리하게 사용할 수 있도록 지원한다.
     */
    @PersistenceContext
    private EntityManager em;

    public Member save(Member member) {
        em.persist(member); // 저장
        return member;
    }

    public void delete(Member member) {
        em.remove(member);
    }

    public List<Member> findAll() {
        return em.createQuery("select m from Member m", Member.class)
                .getResultList();
    }

    public Optional<Member> findById(Long id) {
        Member member = em.find(Member.class, id);

        return Optional.ofNullable(member);
        /**
         * Optional.ofNullable
         * 해당 값이 null X -> 해당 값을 가지는 Optional 객체 반환
         * 해당 값이 null O -> 빈 Optional 객체 반환
         */
    }

    public long count() {
        return em.createQuery("select count(m) from Member m", Long.class) // count 결과값은 Long 타입
                .getSingleResult();
    }

    public Member find(Long id) {
        return em.find(Member.class, id); // 조회
    }
}
