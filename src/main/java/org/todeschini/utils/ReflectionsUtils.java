package org.todeschini.utils;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import static java.text.MessageFormat.format;

@Slf4j
public class ReflectionsUtils {

    public static Object runGetter(Field field, Object o) {

        var getMethod = Arrays.stream(o.getClass().getMethods())
                .filter(method ->
                        ( method.getName().startsWith("get"))
                                && (method.getName().length() == (field.getName().length() + 3)))
                .filter(method -> method.getName().toLowerCase().endsWith(field.getName().toLowerCase()))
                .findFirst();

        if (getMethod.isPresent()) {
            try {
                return getMethod.get().invoke(o);
            } catch (IllegalAccessException | InvocationTargetException e) {
                log.error(format("Could not determine method: {0}", getMethod.get().getName()));
            }
        }

        return null;
    }

    public static Object getValue(Field f, Object o) {
        var value = runGetter(f, o);
        if (value == null) {
            f.setAccessible(true);
            try {
                value = f.get(o);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        return value;
    }
}
