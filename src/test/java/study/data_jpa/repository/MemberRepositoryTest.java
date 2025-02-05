package study.data_jpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.data_jpa.entity.Member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback(false)
class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;

    @Test
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

}