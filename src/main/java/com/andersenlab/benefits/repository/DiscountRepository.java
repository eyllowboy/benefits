package com.andersenlab.benefits.repository;

import com.andersenlab.benefits.domain.Discount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface DiscountRepository extends JpaRepository<Discount, Integer> {

    List<Discount> findByTitle(String title);

    List<Discount> filterByCategoryID(Integer category);

    List<Discount> filterBySizeDiscount(Integer size);


}
