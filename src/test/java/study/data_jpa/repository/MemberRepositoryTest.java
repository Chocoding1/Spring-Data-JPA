package study.data_jpa.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.data_jpa.entity.Member;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
//@Rollback(false)
class MemberRepositoryTest {

    /**
     * memberRepository는 단순 인터페이스인데 어떻게 save(), findById() 메서드를 사용할 수 있나?
     * Spring Data JPA가 해당 인터페이스를 보고 JpaRepository 인터페이스를 상속받고 있으면, Proxy 객체를 생성해서 주입해준다.
     * 따라서 개발자는 Spring Data JPA 관련 인터페이스만 만들어 놓으면, 관련 메서드를 사용할 수 있다.
     */
    @Autowired
    MemberRepository memberRepository;

    @Test
    @DisplayName("회원 저장 테스트")
    void save() {
        //given
        Member member = new Member("memberA");

        //when
        memberRepository.save(member);
        Member findMember = memberRepository.findById(member.getId()).orElse(null);
        /**
         * memberRepository.findById(member.getId()).get()으로도 Member 객체를 꺼내올 수 있는데,
         * 이렇게 하면 만약 null일 경우, 오류 발생 -> throw new NoSuchElementException("No value present");
         */
        /**
         * 실무에서 Optional 반환값 처리
         * 1. isPresent() 메서드를 통해 반환된 객체가 null인지 아닌지 판단한 후 사용
         * 2. 보통은 orElseThrow()를 통해 값이 없는 경우 적절한 예외를 던져 예외처리
         */

        //then
        assertThat(findMember.getId()).isEqualTo(member.getId());
        assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
    }

    @Test
    @DisplayName("단건 조회 테스트")
    void findOne() {
        //given
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");

        memberRepository.save(member1);
        memberRepository.save(member2);

        //when
        Member findMember1 = memberRepository.findById(member1.getId()).orElse(null);
        Member findMember2 = memberRepository.findById(member2.getId()).orElse(null);

        //then
        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);
    }

    @Test
    @DisplayName("다중 조회 테스트")
    void findAll() {
        //given
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");

        memberRepository.save(member1);
        memberRepository.save(member2);

        //when
        List<Member> result = memberRepository.findAll();
        long count = memberRepository.count();

        //then
        assertThat(result.size()).isEqualTo(2);
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("count() 테스트")
    void count() {
        //given
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");

        memberRepository.save(member1);
        memberRepository.save(member2);

        //when
        long count = memberRepository.count();

        //then
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("회원 삭제 테스트")
    void delete() {
        //given
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");

        memberRepository.save(member1);
        memberRepository.save(member2);

        //when
        memberRepository.delete(member1);
        memberRepository.delete(member2);

        long count = memberRepository.count();

        //then
        assertThat(count).isEqualTo(0);
    }

    @Test
    @DisplayName("특정 이름이고, 특정 나이 이상 회원 조회 테스트")
    void findByUsernameAndAgeGreaterThan() {
        //given
        Member member1 = new Member("member1", 10);
        Member member2 = new Member("member1", 20);

        memberRepository.save(member1);
        memberRepository.save(member2);

        //when
        List<Member> result = memberRepository.findByUsernameAndAgeGreaterThan("member1", 15);

        //then
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getUsername()).isEqualTo("member1");
        assertThat(result.get(0).getAge()).isEqualTo(20);
    }

    @Test
    @DisplayName("NamedQuery 사용한 회원 조회")
    void findByUsernameWithNamedQuery() {
        //given
        Member member1 = new Member("member1", 10);
        Member member2 = new Member("member2", 20);

        memberRepository.save(member1);
        memberRepository.save(member2);

        //when
        List<Member> result = memberRepository.findByUsername("member1");

        //then
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getAge()).isEqualTo(10);
    }

    @Test
    @DisplayName("@Query 사용한 회원 조회")
    void findByUsernameWithQueryAnnotation() {
        //given
        Member member1 = new Member("member1", 10);
        Member member2 = new Member("member2", 20);

        memberRepository.save(member1);
        memberRepository.save(member2);

        //when
        List<Member> result = memberRepository.findMember("member1", 10);

        //then
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getAge()).isEqualTo(10);
    }
}