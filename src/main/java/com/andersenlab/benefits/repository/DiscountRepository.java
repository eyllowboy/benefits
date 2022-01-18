package com.andersenlab.benefits.repository;

import com.andersenlab.benefits.domain.Discount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface DiscountRepository extends JpaRepository<Discount, Integer> {

    List<Discount> findByTitle(String title);

    List<Discount> findByCategoryId(Integer category);

    List<Discount> findBySizeDiscount(Integer size);

    List<Discount> findByArea(String area);

    List<Discount> findByTitleOrderByIdDesc(Integer id);


}
