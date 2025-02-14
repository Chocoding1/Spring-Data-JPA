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

    /**
     * 회원 조회
     * 이름이 동일하고, 특정 나이 이상
     */
    public List<Member> findByUsernameAndAgeGreaterThan(String username, int age) {
        return em.createQuery("select m from Member m where m.username = :username and m.age > :age", Member.class)
                .setParameter("username", username)
                .setParameter("age", age)
                .getResultList();
    }

    /**
     * 단순 JPA에서 NamedQuery 사용
     * em.createNamedQuery() 메서드 사용
     * 파라미터로 JPQL 문법 대신 Member 엔티티에서 지정해준 NamedQuery명 전달
     *
     * 단순 JPA 사용해서 NamedQuery 사용하면 사용 안 할 때와 동일하게 코드를 쳐줘야 하고,
     * 심지어는 쿼리를 Entity에 작성해야 해서 보기 안 좋다.
     */
    public List<Member> findByUsername(String username) {
        return em.createNamedQuery("Member.findByUsername", Member.class)
                .setParameter("username", username)
                .getResultList();
    }

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

    /**
     * 벌크 연산
     * 특정 나이 이상 사람들의 나이를 + 1
     */
    public int bulkAgePlus(int age) {
        // 수정된 데이터 수 반환
        return em.createQuery("update Member m set m.age = m.age + 1 where m.age >= :age")
                .setParameter("age", age)
                .executeUpdate();
    }
}
