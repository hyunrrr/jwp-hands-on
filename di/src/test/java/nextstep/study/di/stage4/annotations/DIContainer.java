package nextstep.study.di.stage4.annotations;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javassist.tools.reflect.Reflection;
import org.reflections.Reflections;

/**
 * 스프링의 BeanFactory, ApplicationContext에 해당되는 클래스
 */
class DIContainer {

    private final Set<Object> beans;

    private DIContainer(final Set<Class<?>> classes) {
        beans = new HashSet<>();
        for (Class<?> aClass : classes) {
            beans.add(makeObject(aClass, classes));
        }
    }

    private Object makeObject(Class<?> aClass, Set<Class<?>> classes) {
        if (!isManagedByContainer(aClass, classes)) {
            throw new IllegalArgumentException(aClass + " is not registered as bean.");
        }
        if (isAlreadyExistObject(aClass)) {
            return getBean(aClass);
        }

        Object object = makeInitialObject(aClass, classes);
        Set<Field> fields = Arrays.stream(aClass.getDeclaredFields())
                .filter(it -> it.isAnnotationPresent(Inject.class))
                .collect(Collectors.toSet());
        for (Field field : fields) {
            injectField(object, field, classes);
        }
        return object;
    }

    private boolean isAlreadyExistObject(Class<?> aClass) {
        return beans.stream()
                .anyMatch(bean -> bean.getClass().isAssignableFrom(aClass));
    }

    private boolean isManagedByContainer(Class<?> target, Set<Class<?>> classes) {
        return classes.stream()
                .anyMatch(target::isAssignableFrom);
    }

    private void injectField(Object object, Field field, Set<Class<?>> classes) {
        field.setAccessible(true);
        Object fieldObject = makeObject(field.getType(), classes);
        try {
            field.set(object, fieldObject);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private Object makeInitialObject(Class<?> aClass, Set<Class<?>> classes) {
        try {
            if (aClass.isInterface()) {
                aClass = findImplementClass(aClass, classes);
            }
            Constructor<?> constructor = aClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
            throw new IllegalStateException("can't make instance of " + aClass);
        }
    }

    private Class<?> findImplementClass(Class<?> anInterface, Set<Class<?>> classes) {
        return classes.stream()
                .filter(anInterface::isAssignableFrom)
                .findAny()
                .orElseThrow(IllegalArgumentException::new);
    }


    public static DIContainer createContainerForPackage(final String rootPackageName) {
        Reflections reflection = new Reflections(rootPackageName);
        Set<Class<?>> classes = new HashSet<>();
        classes.addAll(reflection.getTypesAnnotatedWith(Service.class));
        classes.addAll(reflection.getTypesAnnotatedWith(Inject.class));
        classes.addAll(reflection.getTypesAnnotatedWith(Repository.class));
        return new DIContainer(classes);
    }

    @SuppressWarnings("unchecked")
    public <T> T getBean(final Class<T> aClass) {
        return (T) beans.stream()
                .filter(bean -> bean.getClass().isAssignableFrom(aClass))
                .findAny()
                .orElseThrow(IllegalArgumentException::new);
    }
}
