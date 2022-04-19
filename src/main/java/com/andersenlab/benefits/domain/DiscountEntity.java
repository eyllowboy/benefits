package com.andersenlab.benefits.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.Set;

@Schema(name = "Discount", description = "Discount entity")
@Entity
@Table(name = "discounts")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Getter
@Setter
public class DiscountEntity {

    @Schema(description = "Identifier", type = "int64", minimum = "1")
    @Id
    @EqualsAndHashCode.Exclude
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "discount_id_seq")
    @SequenceGenerator(name = "discount_id_seq", sequenceName = "discount_id", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    @Schema(description = " Type of company or service", type = "string", minLength = 1, maxLength = 50)
    @Size(min = 1, max = 50, message = "Type of company or service must be between 1 and 50 characters")
    @NotBlank
    @Column(name = "type")
    private String type;

    @Schema(description = "Discount description", type = "string", minLength = 1, maxLength = 2000)
    @Size(min = 1, max = 2000, message = "Discount description must be between 1 and 2000 characters")
    @NotBlank
    @Column(name = "description")
    private String description;

    @Schema(description = "Discount condition", type = "string", minLength = 1, maxLength = 500)
    @Size(min = 1, max = 500, message = "Discount condition must be between 3 and 20 characters")
    @NotBlank
    @Column(name = "discount_condition")
    private String discount_condition;

    @Schema(description = "Size of discount", type = "string", minLength = 1, maxLength = 100)
    @Size(min = 1, max = 100, message = "Size of discount must be between 1 and 100 characters")
    @NotBlank
    @Column(name = "size")
    private String sizeDiscount;

    @Schema(description = "Type of discount", type = "enum", maxLength = 10)
    @NotNull
    @Enumerated(EnumType.STRING)
    private DiscountType discount_type;

    @Schema(description = "Date of beginning", type = "date")
    @EqualsAndHashCode.Exclude
    @NotNull
    @Column(name = "start_date")
    @Temporal(TemporalType.DATE)
    private Date dateBegin;

    @Schema(description = "Date of ending", type = "date")
    @EqualsAndHashCode.Exclude
    @Column(name = "end_date")
    @Temporal(TemporalType.DATE)
    private Date dateFinish;

    @Schema(description = "Image of discount", type = "picture, png", maxLength = 300)
    @NotBlank
    @Column(name = "image")
    private String imageDiscount;

    @Schema(description = "Location of discount", type = "collection of entities")
    @EqualsAndHashCode.Exclude
    @NotEmpty
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "location_discount",
            joinColumns = @JoinColumn(name = "discount_id"),
            inverseJoinColumns = @JoinColumn(name = "location_id"))
    private Set<LocationEntity> area;

    @Schema(description = "Categories", type = "collections of entities")
    @EqualsAndHashCode.Exclude
    @NotEmpty
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "category_discount",
            joinColumns = @JoinColumn(name = "discount_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id"))
    private Set<CategoryEntity> categories;

    @Schema(description = "Company", type = "entities")
    @EqualsAndHashCode.Exclude
    @NotNull
    @ManyToOne
    @JoinColumn(name = "company_id")
    private CompanyEntity company;
}
