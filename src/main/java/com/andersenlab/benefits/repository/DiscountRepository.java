package com.andersenlab.benefits.repository;

import com.andersenlab.benefits.domain.Discount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface DiscountRepository extends JpaRepository<Discount, Long> {

    List<Discount> findByTitle(String title);

    //List<Discount> findByCategoryId(Long category);

    //List<Discount> findBySizeDiscount(Long size);

    //List<Discount> findByArea(String area);

    //List<Discount> findByTitleOrderByIdDesc(Long id);


}
