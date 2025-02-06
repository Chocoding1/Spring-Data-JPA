package study.data_jpa.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.data_jpa.entity.Member;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
//@Rollback(false) // 테스트 후 롤백하지 않도록. -> DB 쿼리 확인 가능
class MemberJpaRepositoryTest {

    @Autowired
    MemberJpaRepository memberJpaRepository;

    @Test
    @DisplayName("회원 저장 테스트")
    void save() {
        //given
        Member member = new Member("memberA");

        //when
        memberJpaRepository.save(member);
        Member findMember = memberJpaRepository.find(member.getId());

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

        memberJpaRepository.save(member1);
        memberJpaRepository.save(member2);

        //when
        Member findMember1 = memberJpaRepository.findById(member1.getId()).orElse(null);
        Member findMember2 = memberJpaRepository.findById(member2.getId()).orElse(null);

        //then
        Assertions.assertThat(findMember1).isEqualTo(member1);
        Assertions.assertThat(findMember2).isEqualTo(member2);
    }

    @Test
    @DisplayName("다중 조회 테스트")
    void findAll() {
        //given
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");

        memberJpaRepository.save(member1);
        memberJpaRepository.save(member2);

        //when
        List<Member> result = memberJpaRepository.findAll();
        long count = memberJpaRepository.count();

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

        memberJpaRepository.save(member1);
        memberJpaRepository.save(member2);

        //when
        long count = memberJpaRepository.count();

        //then
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("회원 삭제 테스트")
    void delete() {
        //given
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");

        memberJpaRepository.save(member1);
        memberJpaRepository.save(member2);

        //when
        memberJpaRepository.delete(member1);
        memberJpaRepository.delete(member2);

        long count = memberJpaRepository.count();

        //then
        assertThat(count).isEqualTo(0);
    }

    @Test
    @DisplayName("특정 이름이고, 특정 나이 이상 회원 조회 테스트")
    void findByUsernameAndAgeGreaterThan() {
        //given
        Member member1 = new Member("member1", 10);
        Member member2 = new Member("member1", 20);

        memberJpaRepository.save(member1);
        memberJpaRepository.save(member2);

        //when
        List<Member> result = memberJpaRepository.findByUsernameAndAgeGreaterThan("member1", 15);

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

        memberJpaRepository.save(member1);
        memberJpaRepository.save(member2);

        //when
        List<Member> result = memberJpaRepository.findByUsername("member1");

        //then
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getAge()).isEqualTo(10);
    }
}