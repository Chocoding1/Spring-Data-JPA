package study.data_jpa.repository;

import org.springframework.beans.factory.annotation.Value;

public interface UsernameOnly {

//    @Value("#{target.username + ' ' + target.age}") // 얘까지 붙이면 open projection
    String getUsername(); // 얘만 적으면 close projection
}
