package study.data_jpa.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
/**
 * JPA 스펙 상 Entity는 기본 생성자가 하나 있어야 한다.
 * JPA는 기본적으로 프록시 객체를 생성하거나 할 때 기본 생성자를 사용하기 떼문에 기본 생성자를 필수로 명시해야 한다.
 * 누군가가 기본 생성자를 사용하지 않도록 protected로 지정
 * private는 불가. JPA 구현체들이 프록시 객체 생성 등에 사용할 수 있어야 한다.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(of = {"id", "username", "age"}) // toString() 어노테이션
/**
 * NamedQuery
 * NamedQuery는 기본적으로 정적 쿼리이기 때문에 어플리케이션 로딩 시점에 파싱을 할 수 있다.
 * 그래서 이 JPQL을 SQL로 다 만들어놓고 시작한다.
 *
 * <장점>
 * * JPQL 쿼리에 오타가 있으면, 어플리케이션 로딩 시점에 오류 발생 (미리 파싱을 하기 때문)
 * Repository 클래스에 있는 단순 JPQL 쿼리는 문자열로 취급되기 때문에 오타가 들어가도 어플리케이션 로딩 시점에 오류가 발생하지 않는다.
 *
 * <단점>
 * * JPQL이 Repository에 모여있지 않고, Entity에 따로 분리되어 있어 깔끔하지 않다.
 * * Entity에서 NamedQuery를 작성해도 Repository에서 다시 작성해줘야 한다. (한 번 작성할 거 두 번 작성)
 */
@NamedQuery(
        name="Member.findByUsername",
        query="select m from Member m where m.username = :username"
)
public class Member extends BaseEntity{

    @Id
    @GeneratedValue
    @Column(name = "member_id")
    private Long id;
    private String username;
    private int age;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    public Member(String username) {
        this.username = username;
    }

    public Member(String username, int age, Team team) {
        this.username = username;
        this.age = age;
        if (team != null) {
            changeTeam(team);
        }
    }

    public Member(String username, int age) {
        this.username = username;
        this.age = age;
    }

    // 연관관계 메서드
    public void changeTeam(Team team) {
        this.team = team;
        team.getMembers().add(this);
    }
}
