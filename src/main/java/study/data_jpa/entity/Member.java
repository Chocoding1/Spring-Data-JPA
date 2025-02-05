package study.data_jpa.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class Member {

    @Id
    @GeneratedValue
    private Long id;
    private String username;

    /**
     * JPA 스펙 상 Entity는 기본 생성자가 하나 있어야 한다.
     * JPA는 기본적으로 프록시 객체를 생성하거나 할 때 기본 생성자를 사용하기 떼문에 기본 생성자를 필수로 명시해야 한다.
     * 누군가가 기본 생성자를 사용하지 않도록 protected로 지정
     * private는 불가. JPA 구현체들이 프록시 객체 생성 등에 사용할 수 있어야 한다.
     */
    protected Member() {
    }

    public Member(String username) {
        this.username = username;
    }
}
