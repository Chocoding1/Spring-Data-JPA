package study.data_jpa.controller;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import study.data_jpa.dto.MemberDto;
import study.data_jpa.entity.Member;
import study.data_jpa.repository.MemberRepository;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberRepository memberRepository;

    @GetMapping("/members/{id}")
    public String findMember(@PathVariable("id") Long id) {
        Member member = memberRepository.findById(id).get();
        return member.getUsername();
    }

    /**
     * 도메인 클래스 컨버터 적용
     * @PathVariable로 받는 id값이 PK이기 때문에 사용할 수 있다.
     * 사실 그렇게 권장하는 방법은 아니다.
     * 간단한 경우에만 사용이 가능하고, 조금 복잡해지면 못 쓴다고 보면 된다.
     * <주의>
     * 도메인 클래스 컨버터로 엔티티를 파라미터로 받으면, 해당 엔티티는 반드시 조회용으로만 사용해야 한다.
     * 트랜잭션이 없는 범위에서 엔티티를 조회했기 때문에 엔티티를 변경해도 DB에 반영되지 않기 때문이다.
     */
    @GetMapping("/members2/{id}")
    public String findMember2(@PathVariable("id") Member member) {
        return member.getUsername();
    }

    /**
     * 요청 파라미터들이 controller에 바인딩될 때, Pageable 인터페이스가 있으면
     * PageRequest라는 객체를 생성해서 값을 주입해준다.
     * ex) http://localhost:8080/members?page=1&size=10&sort=id,desc&sort=username
     * 저런 파라미터들이 다 알아서 적용이 된다.
     *
     * 페이징에 대한 기본값을 변경하고 싶다면,
     * 글로벌 설정은 application.yml 파일에서 설정하면 되고,
     * 메서드 단위로 특별한 설정을 하고 싶다면, Pageable 파라미터 앞에 @PageableDefault를 설정하면 된다.
     */
    @GetMapping("/members")
    public Page<MemberDto> list(@PageableDefault(size = 5, sort = "username") Pageable pageable) {
        Page<Member> page = memberRepository.findAll(pageable);
        Page<MemberDto> map = page.map(member -> new MemberDto(member.getId(), member.getUsername(), null));
        return map;
    }

    @PostConstruct
    public void init() {
        for (int i = 0; i < 100; i++) {
            memberRepository.save(new Member("user" + i, i));
        }
    }
}
