package com.andersenlab.benefits.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

@Schema(name = "Discount", description = "Discount entity")
@Entity
@Table(name = "discounts")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString

public class DiscountEntity {

    @Schema(description = "Identifier", type = "int64", minimum = "1")
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "discount_id_seq")
    @SequenceGenerator(name = "discount_id_seq", sequenceName = "discount_id", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;

    @Schema(description = "Categories", type = "collections of entities")
    @ManyToMany(fetch = FetchType.EAGER, cascade =
            {
                    CascadeType.DETACH,
                    CascadeType.MERGE,
                    CascadeType.REFRESH,
                    CascadeType.PERSIST
            })
    @JoinTable(
        name = "category_discount",
        joinColumns = @JoinColumn(name = "discount_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id"))
    private Set<CategoryEntity> categories;

    @Schema(description = "Companies", type = "entities")
    @ManyToOne
    @JoinColumn(name = "company_id")
    private CompanyEntity company_id;

    @Schema(description = " Type of discount", type = "string", minLength = 1, maxLength = 50)
    @Column(name = "type", nullable = false, length = 50)
    private String type;

    @Schema(description = "Description entities", type = "string", minLength = 1, maxLength = 2000)
    @Column(name = "description", nullable = false, length = 2000)
    private String description;

    @Schema(description = "Discount condition", type = "string", minLength = 1, maxLength = 500)
    @Column(name = "discount_condition", nullable = false, length = 500)
    private String discount_condition;

    @Schema(description = "Size of discount", type = "string", minLength = 1, maxLength = 100)
    @Column(name = "size", nullable = false, length = 100)
    private String sizeDiscount;

    @Schema(description = "View of discount", type = "enum", maxLength = 10)
    @Enumerated(EnumType.STRING)
    private DiscountType discount_type;

    @Schema(description = "Date of beginning", type = "date")
    @Column(name = "start_date", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date dateBegin;

    @Schema(description = "Date of ending", type = "date")
    @Column(name = "end_date")
    @Temporal(TemporalType.DATE)
    private Date dateFinish;

    @Schema(description = "Image of discount", type = "picture, png", maxLength = 300)
    @Column(name = "image", nullable = false, length = 300)
    private String imageDiscount;

    @Schema(description = "Location of discount", type = "collection of entities")
    @ManyToMany(fetch = FetchType.EAGER, cascade =
            {
                    CascadeType.DETACH,
                    CascadeType.MERGE,
                    CascadeType.REFRESH,
                    CascadeType.PERSIST
            })
    @JoinTable (name = "location_discount",
            joinColumns = @JoinColumn(name = "discount_id"),
            inverseJoinColumns = @JoinColumn(name = "location_id"))
    private Set<LocationEntity> area;

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
