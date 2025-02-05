package study.data_jpa.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.data_jpa.entity.Member;

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
}