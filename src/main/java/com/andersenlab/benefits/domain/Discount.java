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
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "discount_id_seq")
    @SequenceGenerator(name = "discount_id_seq", sequenceName = "discount_id", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "category_id")
    private Long CategoryId;

    @Column(name = "company_id")
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
    private Long area;

    @Column(name = "image", nullable = false, length = 300)
    private String imageDiscount;

}
