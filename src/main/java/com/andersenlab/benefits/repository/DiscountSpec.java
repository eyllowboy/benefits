package com.andersenlab.benefits.repository;

import com.andersenlab.benefits.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
import java.sql.Time;
import java.util.Date;
import java.util.Objects;


@Component
public class DiscountSpec {


    public static Specification<DiscountEntity> getByCategory(String category){
        return new Specification<DiscountEntity>() {
            @Override
            public Predicate toPredicate(Root<DiscountEntity> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                Join<Object, Object> categoryJoin = root.join(DiscountEntity_.CATEGORIES);
                Predicate equalPredicate = criteriaBuilder.equal(categoryJoin.get(CategoryEntity_.TITLE), category);
                return equalPredicate;
            }
        };

    }

    public static Specification<DiscountEntity> getByLocation(String location){
        return new Specification<DiscountEntity>() {
            @Override
            public Predicate toPredicate(Root<DiscountEntity> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                Join<Object, Object> areaJoin = root.join(DiscountEntity_.AREA);
                Predicate equalPredicate = criteriaBuilder.like(areaJoin.get(LocationEntity_.CITY), "%" + location + "%");
                return equalPredicate;
            }
        };
    }

    public static Specification<DiscountEntity> getLastAdded() {
        return new Specification<DiscountEntity>() {
            @Override
            public Predicate toPredicate(Root<DiscountEntity> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                Order order = criteriaBuilder.desc(root.<Date>get(DiscountEntity_.DATE_BEGIN));
                CriteriaQuery cr = query.orderBy(order);
                return cr.getRestriction();
            }
        };
    }

    public static Specification<DiscountEntity> getByType(String type){
        return new Specification<DiscountEntity>() {
            @Override
            public Predicate toPredicate(Root<DiscountEntity> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                return criteriaBuilder.like(root.get(DiscountEntity_.TYPE), "%" + type + "%");
            }
        };

    }

    public static Specification<DiscountEntity> getBySize(String sizeDiscount){
        return new Specification<DiscountEntity>() {
            @Override
            public Predicate toPredicate(Root<DiscountEntity> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                return criteriaBuilder.like(root.get(DiscountEntity_.SIZE_DISCOUNT), "%" + sizeDiscount + "%");
            }
        };
    }



}
