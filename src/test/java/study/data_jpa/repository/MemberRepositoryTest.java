package study.data_jpa.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import study.data_jpa.dto.MemberDto;
import study.data_jpa.entity.Member;
import study.data_jpa.entity.Team;

import java.util.ArrayList;
import java.util.Arrays;
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
    @Autowired
    TeamRepository teamRepository;
    @Autowired
    EntityManager em;

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

    @Test
    @DisplayName("@Query 사용하여 단순 값 조회(회원명 조회)")
    void findUsernameList() {
        //given
        Member member1 = new Member("member1", 10);
        Member member2 = new Member("member2", 20);

        memberRepository.save(member1);
        memberRepository.save(member2);

        //when
        List<String> usernameList = memberRepository.findUsernameList();

        //then
        assertThat(usernameList.size()).isEqualTo(2);
        assertThat(usernameList).contains("member1", "member2");
    }

    @Test
    @DisplayName("@Query 사용하여 DTO 조회")
    void findMemberDto() {
        //given
        Team teamA = new Team("teamA");
        teamRepository.save(teamA);

        Member member1 = new Member("member1", 10, teamA);

        memberRepository.save(member1);

        //when
        List<MemberDto> result = memberRepository.findMemberDto();

        //then
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getTeamName()).isEqualTo("teamA");
    }

    @Test
    @DisplayName("@Query에 in절 파라미터로 Collection 전달")
    void findByNames() {
        //given
        Member member1 = new Member("member1", 10);
        Member member2 = new Member("member2", 20);

        memberRepository.save(member1);
        memberRepository.save(member2);


        //when
        List<Member> result = memberRepository.findByNames(Arrays.asList("member1", "member2"));

        //then
        assertThat(result.size()).isEqualTo(2);
        assertThat(result).contains(member1, member2);
    }

    @Test
    @DisplayName("다양한 반환 타입")
    void returnType() {
        //given
        Member member1 = new Member("member1", 10);
        Member member2 = new Member("member2", 20);

        memberRepository.save(member1);
        memberRepository.save(member2);


        //when
        List<Member> result = memberRepository.findListByUsername("member1");
        Member findMember = memberRepository.findMemberByUsername("member1");
        Member optionalMember = memberRepository.findOptionalByUsername("member2").orElse(null);

        //then
        assertThat(result.size()).isEqualTo(1);
        assertThat(result).contains(member1);
        assertThat(findMember).isEqualTo(member1);
        assertThat(optionalMember.getAge()).isEqualTo(20);
    }

    @Test
    @DisplayName("Spring Data JPA 활용한 paging")
    void paging() {
        //given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 10));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 10));
        memberRepository.save(new Member("member5", 10));
        memberRepository.save(new Member("member6", 10));
        memberRepository.save(new Member("member7", 10));

        int age = 10;
        // 0페이지에서 3개 가져와(Data JPA는 페이지를 0번부터 센다)
        PageRequest pageRequest = PageRequest.of(2, 3, Sort.by(Sort.Direction.DESC, "username"));

        //when
        Page<Member> page = memberRepository.findByAge(age, pageRequest);

        //then
        List<Member> content = page.getContent(); // 가져온 페이지에서 안의 내용들을 꺼내는 함수
        long totalElements = page.getTotalElements(); // totalCount

        assertThat(content.size()).isEqualTo(1);
        assertThat(totalElements).isEqualTo(7);
        assertThat(page.getNumber()).isEqualTo(2);
        assertThat(page.getTotalPages()).isEqualTo(3);
        assertThat(page.isLast()).isTrue();
        assertThat(page.hasNext()).isFalse();
    }

    @Test
    @DisplayName("Spring Data JPA 활용한 slicing")
    void slicing() {
        //given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 10));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 10));
        memberRepository.save(new Member("member5", 10));
        memberRepository.save(new Member("member6", 10));
        memberRepository.save(new Member("member7", 10));

        int age = 10;
        // 0페이지에서 3개 가져와(Data JPA는 페이지를 0번부터 센다)
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

        //when
        Slice<Member> page = memberRepository.findByAge(age, pageRequest);

        //then
        List<Member> content = page.getContent(); // 가져온 페이지에서 안의 내용들을 꺼내는 함수

        assertThat(content.size()).isEqualTo(3);
        assertThat(page.getNumber()).isEqualTo(2);
        assertThat(page.isLast()).isTrue();
        assertThat(page.hasNext()).isFalse();
    }

    @Test
    @DisplayName("Spring Data JPA 활용한 벌크 연산")
    void bulkUpdate() {
        //given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 19));
        memberRepository.save(new Member("member3", 20));
        memberRepository.save(new Member("member4", 21));
        memberRepository.save(new Member("member5", 40));

        //when
        int resultCount = memberRepository.bulkAgePlus(20);

        //then
        assertThat(resultCount).isEqualTo(3);
    }

    @Test
    @DisplayName("EntityGraph 테스트")
    public void findMemberLazy() throws Exception {
        //given
        //member1 -> teamA
        //member2 -> teamB
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        teamRepository.save(teamA);
        teamRepository.save(teamB);
        memberRepository.save(new Member("member1", 10, teamA));
        memberRepository.save(new Member("member2", 20, teamB));
        em.flush();
        em.clear();
        //when
        List<Member> members = memberRepository.findAll();
        //then
        for (Member member : members) {
            System.out.println(member.getTeam().getName());
        }
    }

    @Test
    @DisplayName("custom repository 테스트")
    void callCustom() {
        // custom repository를 상속받은 memberRepository에서 custom 메서드가 잘 호출되는지 테스트
        List<Member> result = memberRepository.findMemberCustom();
    }

    @Test
    @DisplayName("projections")
    void projections() {
        //given
        Team teamA = new Team("teamA");
        em.persist(teamA);

        em.persist(new Member("m1", 0, teamA));
        em.persist(new Member("m2", 0, teamA));

        em.flush();
        em.clear();

        //when
        List<UsernameOnly> result = memberRepository.findProjectionsByUsername("m1");

        for (UsernameOnly usernameOnly : result) {
            System.out.println("usernameOnly = " + usernameOnly);
        }
    }
}