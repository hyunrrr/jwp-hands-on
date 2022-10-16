package nextstep.study.di.stage3.context;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashSet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DIContainerTest {


    /**
     * 순서에 아직 의존적임.
     */
    @DisplayName("di컨테이너 객체 생성 시 빈 생성이 정상적으로 된다.")
    @Test
    void constructor() {
        var classes = new LinkedHashSet<Class<?>>();
        classes.add(InMemoryUserDao.class);
        classes.add(UserService.class);
        DIContainer diContainer = new DIContainer(classes);

        InMemoryUserDao inMemoryUserDao = diContainer.getBean(InMemoryUserDao.class);
        UserService userService = diContainer.getBean(UserService.class);

        assertThat(inMemoryUserDao).isInstanceOf(InMemoryUserDao.class);
        assertThat(userService).isInstanceOf(UserService.class);
        assertThat(userService.getUserDao()).isInstanceOf(InMemoryUserDao.class);
    }
}
