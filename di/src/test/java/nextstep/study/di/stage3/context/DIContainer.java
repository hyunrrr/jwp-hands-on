package nextstep.study.di.stage3.context;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 스프링의 BeanFactory, ApplicationContext에 해당되는 클래스
 */
class DIContainer {

    private final Set<Object> beans;

    /**
     * 직접 빈으로 등록할 객체를 전달받음.
     * 필드에 인터페이스나 추상클래스가 있으면 적절한 구현체가 빈으로 등록되어 있지 않을떄 예외가 발생함
     * 지금 단계에선 해결이 힘듦
     */
    public DIContainer(final Set<Class<?>> classes) {
        beans = new HashSet<>();
        for (Class<?> aClass : classes) {
            System.out.println("order: " + aClass);
            beans.add(makeObject(aClass, classes));
        }
    }

    private <T> T makeObject(Class<T> aClass, Set<Class<?>> classes) {
        if (!isClassManagedByContainer(aClass, classes)) {
            throw new IllegalArgumentException(aClass.getName() + " is not bean.");
        }
        if (isObjectAlreadyExist(aClass)) {
            System.out.println("@@@@@in");
            return getBean(aClass);
        }

        try {
            Constructor<T> constructor = getProperConstructor(aClass);
            List<Object> parameterObjects = new ArrayList<>();
            System.out.println(constructor.getParameterTypes().length);
            for (Class<?> parameterClass : constructor.getParameterTypes()) {
                parameterObjects.add(makeObject(parameterClass, classes));
            }
            constructor.setAccessible(true);
            return constructor.newInstance(parameterObjects.toArray());
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            throw new IllegalStateException("make bean failed");
        }
    }

    private <T> Constructor<T> getProperConstructor(Class<T> aClass) {
        Constructor<?>[] constructors = aClass.getDeclaredConstructors();
        System.out.println("length: " + constructors.length);
        if (constructors.length != 1) {
            throw new IllegalStateException("constructor ambiguous exception");
        }
        return (Constructor<T>) constructors[0];
    }

    private boolean isObjectAlreadyExist(Class<?> aClass) {
        return beans.stream()
                .anyMatch(bean -> aClass.isAssignableFrom(bean.getClass()));
    }

    private <T> boolean isClassManagedByContainer(Class<T> target, Set<Class<?>> classes) {
        return classes.stream()
                .anyMatch(target::isAssignableFrom);
    }

    @SuppressWarnings("unchecked")
    public <T> T getBean(final Class<T> aClass) {
        return (T) beans.stream()
                .filter(aClass::isInstance)
                .findAny()
                .orElseThrow(IllegalArgumentException::new);
    }
}
