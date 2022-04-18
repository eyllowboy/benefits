package com.andersenlab.benefits.service.impl;

import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Objects;

public class ValidateUtils {
    public static String errNotFoundMessage(final String entityName, final Long id) {
        return String.format("%s with id: %d was not found in the database", entityName, id);
    }

    public static String errAlreadyExistMessage(final String entityName, final String prefix, final String value) {
        return String.format("%s with %s: %s already exist in database", entityName, prefix, value);
    }

    public static String errIllegalSpaces(final String value) {
        return String.format("'%s' contain illegal spaces", value);
    }

    public static String errAssociatedEntity(final String associatedEntity, final String parentEntity) {
        return String.format("There is active %s in this %s in database", associatedEntity, parentEntity);
    }

    public static String errNoData(final String entity, final String field) {
        return String.format("Adding %s haven't done. Obligatory field '%s' has no data", entity, field);
    }

    public static String updateSpaces(final String value, final String entityName, final String fieldName) {
        String tmpValue = value.trim();
        while (tmpValue.contains("  ")) {
            tmpValue = tmpValue.replace("  ", " ");
        }
        if (tmpValue.isEmpty() || tmpValue.equals(" "))
            throw new IllegalStateException(errNoData(entityName, fieldName));
        return tmpValue;
    }

    public static void validateField(final Field field, final Object value, final String entityName) {
        final NotBlank nb = field.getAnnotation(NotBlank.class);
        final NotNull nn = field.getAnnotation(NotNull.class);
        final NotEmpty ne = field.getAnnotation(NotEmpty.class);
        if ((nb != null && (value == null || Objects.equals(value, "")))
                || ((nn != null || ne != null) && value == null))
            throw new IllegalStateException(errNoData(entityName, field.getName()));
    }

    public static void validateEntityPost(final Object entity) {
        try {
            final PropertyAccessor propertyAccessor = PropertyAccessorFactory.forDirectFieldAccess(entity);
            Arrays.stream(entity.getClass().getDeclaredFields()).forEach(field -> {
                Object val = propertyAccessor.getPropertyValue(field.getName());
                validateField(field, val, entity.getClass().getSimpleName());
                if (val instanceof String) {
                    propertyAccessor.setPropertyValue(
                            field.getName(),
                            updateSpaces((String) val, entity.getClass().getSimpleName(), field.getName()
                            ));
                }
            });
        } catch (NullPointerException ex) {
            throw new IllegalStateException("Adding " + entity.getClass().getSimpleName()
                    + " haven't done. Check entity data");
        }
    }

    public static void validateEntityPatch(final Object entity) {
        try {
            final PropertyAccessor propertyAccessor = PropertyAccessorFactory.forDirectFieldAccess(entity);
            Arrays.stream(entity.getClass().getDeclaredFields()).forEach(field -> {
                Object val = propertyAccessor.getPropertyValue(field.getName());
                if (val instanceof String)
                    propertyAccessor.setPropertyValue(
                            field.getName(),
                            updateSpaces((String) val, entity.getClass().getSimpleName(), field.getName()
                            ));
            });
        } catch (NullPointerException ex) {
            throw new IllegalStateException("Adding " + entity.getClass().getSimpleName()
                    + " haven't done. Check entity data");
        }
    }
}
