package com.andersenlab.benefits.service.impl;

import com.andersenlab.benefits.domain.DiscountEntity;
import com.andersenlab.benefits.repository.DiscountRepository;
import com.andersenlab.benefits.repository.DiscountSpec;
import com.andersenlab.benefits.service.DiscountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.andersenlab.benefits.service.impl.ValidateUtils.validateEntityFieldsAnnotations;

/**
 * The implementation for performing operations on a {@link DiscountEntity}.
 *
 * @author Aleksei Sidorin
 * @version 1.0
 * @see DiscountEntity
 * @see DiscountService
 */


@Service
public class DiscountServiceImpl implements DiscountService {

    private final DiscountRepository discountRepository;


    @Autowired
    public DiscountServiceImpl(final DiscountRepository discountRepository) {
        this.discountRepository = discountRepository;
    }

    @Override
    public Optional<DiscountEntity> findByIdDiscount(final Long id) {
        return this.discountRepository.findById(id);
    }

    @Override
    public Page<DiscountEntity> findAllDiscounts(final Pageable pageable) {
        return this.discountRepository.findAll(pageable);
    }

    @Override
    public Optional<DiscountEntity> createDiscount(final DiscountEntity discount) {
        discount.setId(null);
        validateEntityFieldsAnnotations(discount, true);
        return Optional.of(this.discountRepository.save(discount));
    }

    @Override
    public Optional<DiscountEntity> updateDiscountById(final Long id, final DiscountEntity newDiscount) {
        validateEntityFieldsAnnotations(newDiscount, false);
        return Optional.of(this.discountRepository.save(newDiscount));
    }

    @Override
    public void deleteDiscountById(final Long id) {
        this.discountRepository.deleteById(id);
    }

    @Override
    public Page<DiscountEntity> getDiscountsByCriteria(final Specification<DiscountEntity> specificDiscountEntity, final Pageable pageable) {
        return this.discountRepository.findAll(specificDiscountEntity,pageable);
    }

    @Override
    public List<DiscountEntity> getSimilarDiscounts(final String category,
                                                    final String sizeDiscount,
                                                    final String city,
                                                    final Integer limit) {
        final Specification<DiscountEntity> specificationCategory = DiscountSpec.getByCategory(category);
        final Specification<DiscountEntity> specificationFinal;
        if (Objects.isNull(city)) {
            specificationFinal = Specification.where(specificationCategory);
        } else {
            specificationFinal = Specification.where(specificationCategory
                    .and(DiscountSpec.getByLocation(city)));
        }
        final List<DiscountEntity> discounts = this.discountRepository.findAll(specificationFinal);
        return discounts.stream().filter(discount ->
                (discount.getSizeDiscount().contains(sizeDiscount)
                        || sizeDiscount.contains(discount.getSizeDiscount()))).limit(limit).toList();
    }
}
