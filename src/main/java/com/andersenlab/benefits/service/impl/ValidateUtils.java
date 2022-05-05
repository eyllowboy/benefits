package com.andersenlab.benefits.service.impl;

import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Objects;

public class ValidateUtils {
    public static String errIdNotFoundMessage(final String entityName, final Long id) {
        return String.format("%s with id: %d was not found in the database", entityName, id);
    }

    public static String errEntityNotFoundMessage(final String entityName, final String searchCriteria, final String value) {
        return String.format("%s with %s: %s was not found in the database", entityName, searchCriteria, value);
    }

    public static String errAlreadyExistMessage(final String entityName, final String searchCriteria, final String value) {
        return String.format("%s with %s: %s already exist in database", entityName, searchCriteria, value);
    }

    public static String errAssociatedEntity(final String associatedEntity, final String parentEntity) {
        return String.format("There is active %s in this %s in database", associatedEntity, parentEntity);
    }

    public static String errIncorrectSize(final String field, final int minSize, final int maxSize) {
        return String.format("Incorrect field %s data size - must be between %d and %d", field, minSize, maxSize);
    }

    public static String errNoData(final String entity, final String field) {
        return String.format("Adding %s haven't done. Obligatory field '%s' has no data", entity, field);
    }

    public static String updateSpaces(final String value, final String entityName, final String fieldName) {
        String tmpValue = value.trim();
        while (tmpValue.contains("  ")) {
            tmpValue = tmpValue.replace("  ", " ");
        }
        if (tmpValue.isEmpty() || tmpValue.equals(" ")) {
            throw new IllegalStateException(errNoData(entityName, fieldName));
        }
        return tmpValue;
    }

    public static void validateField(final Field field, final Object value, final String entityName, final boolean validateNull) {
        final NotBlank nb = field.getAnnotation(NotBlank.class);
        final NotNull nn = field.getAnnotation(NotNull.class);
        final NotEmpty ne = field.getAnnotation(NotEmpty.class);
        final Size size = field.getAnnotation(Size.class);
        if (validateNull && (!Objects.isNull(nb) && (Objects.isNull(value) || Objects.equals(value, "")))
                || ((!Objects.isNull(nn) || !Objects.isNull(ne)) && Objects.isNull(value))) {
            throw new IllegalStateException(errNoData(entityName, field.getName()));
        }
        if (!Objects.isNull(size)
                && value instanceof String
                && (((String) value).length() < size.min() || ((String) value).length() > size.max())) {
            throw new IllegalStateException(errIncorrectSize(field.getName(), size.min(), size.max()));
        }
    }

    public static void validateEntityFieldsAnnotations(final Object entity, final boolean validateNull) {
        try {
            final PropertyAccessor propertyAccessor = PropertyAccessorFactory.forDirectFieldAccess(entity);
            Arrays.stream(entity.getClass().getDeclaredFields()).forEach(field -> {
                final Object val = propertyAccessor.getPropertyValue(field.getName());
                if (val instanceof String) {
                    propertyAccessor.setPropertyValue(
                            field.getName(),
                            updateSpaces((String) val, entity.getClass().getSimpleName(), field.getName())
                    );
                }
                validateField(field, val, entity.getClass().getSimpleName(), validateNull);
            });
        } catch (final NullPointerException ex) {
            throw new IllegalStateException("Adding " + entity.getClass().getSimpleName()
                    + " haven't done. Check entity data");
        }
    }
}
