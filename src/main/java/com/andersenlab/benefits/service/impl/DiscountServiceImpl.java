package com.andersenlab.benefits.service.impl;

import com.andersenlab.benefits.domain.DiscountEntity;
import com.andersenlab.benefits.repository.DiscountRepository;
import com.andersenlab.benefits.repository.DiscountSpec;
import com.andersenlab.benefits.service.CategoryService;
import com.andersenlab.benefits.service.CompanyService;
import com.andersenlab.benefits.service.DiscountService;
import com.andersenlab.benefits.service.LocationService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

import static com.andersenlab.benefits.service.impl.ValidateUtils.*;

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
    private final CompanyService companyService;
    private final LocationService locationService;
    private final CategoryService categoryService;


    @Autowired
    public DiscountServiceImpl(final DiscountRepository discountRepository,
                               final CompanyService companyService,
                               final LocationService locationService,
                               final CategoryService categoryService) {
        this.discountRepository = discountRepository;
        this.companyService = companyService;
        this.locationService = locationService;
        this.categoryService = categoryService;
    }

    @Override
    public DiscountEntity findById(final Long id) {
        return this.discountRepository.findById(id).orElseThrow(() ->
                new IllegalStateException(errIdNotFoundMessage("Discount", id)));
    }

    @Override
    public Page<DiscountEntity> findAll(final Pageable pageable) {
        return this.discountRepository.findAll(pageable);
    }

    @Override
    public Page<DiscountEntity> getDiscountsByCriteria(final Specification<DiscountEntity> specificDiscountEntity, final Pageable pageable) {
        return this.discountRepository.findAll(specificDiscountEntity,pageable);
    }

    @Override
    public List<DiscountEntity> getSimilarDiscounts(final String category,
                                                    final Integer sizeDiscount,
                                                    final String city,
                                                    final Integer limit) {
        final Specification<DiscountEntity> specificationCategory = DiscountSpec.getByCategory(category)
                .and(DiscountSpec.getBySize(sizeDiscount));
        final Specification<DiscountEntity> specificationFinal;
        if (Objects.isNull(city)) {
            specificationFinal = Specification.where(specificationCategory);
        } else {
            specificationFinal = Specification.where(specificationCategory
                    .and(DiscountSpec.getByLocation(city)));
        }
        return this.discountRepository.findAll(specificationFinal);
    }

    @Override
    public DiscountEntity update(final Long id, final DiscountEntity discount) {
        final DiscountEntity existingDiscount = findById(id);
        validateCompanyLocationCategory(discount);
        validateEntityFieldsAnnotations(discount, false);
        BeanUtils.copyProperties(discount, existingDiscount, "id");
        return this.discountRepository.save(discount);
    }

    @Override
    public DiscountEntity save(final DiscountEntity discount) {
        validateCompanyLocationCategory(discount);
        discount.setId(null);
        validateEntityFieldsAnnotations(discount, true);
        return this.discountRepository.save(discount);
    }

    @Override
    public void delete(final Long id) {
        final DiscountEntity discount = findById(id);
        this.discountRepository.delete(discount);
    }

    private void validateCompanyLocationCategory(final DiscountEntity discount) {
        this.companyService.findById(discount.getCompany().getId());
        discount.getArea().forEach(location -> this.locationService.findById(location.getId()));
        discount.getCategories().forEach(category -> this.categoryService.findById(category.getId()));
    }
}
