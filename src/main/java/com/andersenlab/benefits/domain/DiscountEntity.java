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

    @ManyToMany(fetch = FetchType.EAGER, cascade =
            {
                    CascadeType.DETACH,
                    CascadeType.MERGE,
                    CascadeType.REFRESH,
                    CascadeType.PERSIST
            })
    @JoinTable(name = "category_discount",
            joinColumns = @JoinColumn(name = "cd_discount_id"),
            inverseJoinColumns = @JoinColumn(name = "cd_category_id"))
    private Set<CategoryEntity> categories;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private CompanyEntity company_id;
//    @Column(name = "company_id")
//    private Long companyId;

    @Column(name = "type", nullable = false, length = 50)
    private String type;

    @Column(name = "description", nullable = false, length = 2000)
    private String description;

    @Column(name = "discount_condition", nullable = false, length = 500)
    private String discount_condition;

    @Column(name = "size", nullable = false, length = 100)
    private String sizeDiscount;

    @Column(name = "start_date", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date dateBegin;

    @Column(name = "end_date")
    @Temporal(TemporalType.DATE)
    private Date dateFinish;

    @ManyToMany (fetch = FetchType.EAGER, cascade =
            {
                CascadeType.DETACH,
                CascadeType.MERGE,
                CascadeType.REFRESH,
                CascadeType.PERSIST
            })
    @JoinTable (name = "location_discount",
            joinColumns = @JoinColumn(name = "ld_discount_id"),
            inverseJoinColumns = @JoinColumn(name = "ld_location_id"))
    private Set<LocationEntity> area;

    @Column(name = "image", nullable = false, length = 300)
    private String imageDiscount;

}
