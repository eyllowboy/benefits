package com.andersenlab.benefits.domain;


import lombok.*;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;


@Entity
@Table(name = "discounts")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class DiscountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "discount_id_seq")
    @SequenceGenerator(name = "discount_id_seq", sequenceName = "discount_id", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "type", nullable = false, length = 50)
    private String type;

    @Column(name = "description", nullable = false, length = 2000)
    private String description;

    @Column(name = "discount_condition", nullable = false, length = 500)
    private String discount_condition;

    @Column(name = "size", nullable = false, length = 100)
    private String sizeDiscount;

    @Enumerated(EnumType.STRING)
    private DiscountType discount_type;

    @Column(name = "start_date", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date dateBegin;

    @Column(name = "end_date")
    @Temporal(TemporalType.DATE)
    private Date dateFinish;

    @Column(name = "image", nullable = false, length = 300)
    private String imageDiscount;

    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(name = "category_discount",
            joinColumns = @JoinColumn(name = "cd_discount_id"),
            inverseJoinColumns = @JoinColumn(name = "cd_category_id"))
    private Set<CategoryEntity> categories;

    @ManyToMany
    @JoinTable (name = "location_discount",
            joinColumns = @JoinColumn(name = "ld_discount_id"),
            inverseJoinColumns = @JoinColumn(name = "ld_location_id"))
    private Set<LocationEntity> area;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private CompanyEntity company_id;

    public DiscountEntity(Long id, String type, String description, String discount_condition, String sizeDiscount, DiscountType discount_type, Date dateBegin, Date dateFinish, String imageDiscount) {
        this.id = id;
        this.type = type;
        this.description = description;
        this.discount_condition = discount_condition;
        this.sizeDiscount = sizeDiscount;
        this.discount_type = discount_type;
        this.dateBegin = dateBegin;
        this.dateFinish = dateFinish;
        this.imageDiscount = imageDiscount;
    }
}
