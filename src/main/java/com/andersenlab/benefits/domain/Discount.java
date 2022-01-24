package com.andersenlab.benefits.domain;


import lombok.*;

import javax.persistence.*;
import java.util.Date;


@Entity
@Table(name = "discounts")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class Discount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "category_id", nullable = false, updatable = false)
    private Long CategoryId;

    @Column(name = "company_id", nullable = false, updatable = false)
    private Long CompanyId;

    @Column(name = "title", nullable = false, length = 50)
    private String title;

    @Column(name = "description", nullable = false, length = 2000)
    private String description;

    @Column(name = "size", nullable = false)
    private Integer sizeDiscount;

    @Column(name = "start_date", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date dateBegin;

    @Column(name = "end_date")
    @Temporal(TemporalType.DATE)
    private Date dateFinish;

    @Column(name = "area", nullable = false, updatable = false)
    private Integer area;

    @Column(name = "image", nullable = false, length = 300)
    private String imageDiscount;


}
