package com.andersenlab.benefits.repository;

import com.andersenlab.benefits.domain.CategoryEntity_;
import com.andersenlab.benefits.domain.DiscountEntity;
import com.andersenlab.benefits.domain.DiscountEntity_;
import com.andersenlab.benefits.domain.LocationEntity_;
import com.andersenlab.benefits.domain.CompanyEntity_;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import javax.persistence.criteria.*;
import java.util.Date;

@Component
public class DiscountSpec {


    public static Specification<DiscountEntity> getByCategory(final String category) {
        return (root, query, criteriaBuilder) -> {
            final Join<Object, Object> categoryJoin = root.join(DiscountEntity_.CATEGORIES);
            return criteriaBuilder.equal(categoryJoin.get(CategoryEntity_.TITLE), category);
        };

    }

    public static Specification<DiscountEntity> getByLocation(final String location) {
        return (root, query, criteriaBuilder) -> {
            final Join<Object, Object> areaJoin = root.join(DiscountEntity_.AREA);
            return criteriaBuilder.like(areaJoin.get(LocationEntity_.CITY), "%" + location + "%");
        };
    }

    public static Specification<DiscountEntity> getLastAdded() {
        return (root, query, criteriaBuilder) -> {
            final Order order = criteriaBuilder.desc(root.<Date>get(DiscountEntity_.DATE_BEGIN));
            return query.orderBy(order).getRestriction();
        };
    }

    public static Specification<DiscountEntity> getByType(final String type) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(root.get(DiscountEntity_.TYPE), "%" + type + "%");
    }

    public static Specification<DiscountEntity> getByCompanyTitle(final String title) {
        return (root, query, criteriaBuilder) -> {
            final Join<Object, Object> companyJoin = root.join(DiscountEntity_.COMPANY);
            return criteriaBuilder.like(companyJoin.get(CompanyEntity_.TITLE), "%" + title + "%");
        };
    }

    public static Specification<DiscountEntity> getBySize(final int sizeDiscount) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.between(
                root.get(DiscountEntity_.SIZE_MIN),
                root.get(DiscountEntity_.SIZE_MAX),
                criteriaBuilder.literal(sizeDiscount));
    }
}
