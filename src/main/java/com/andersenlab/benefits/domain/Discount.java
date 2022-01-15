package com.andersenlab.benefits.domain;



import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.Size;
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
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="id", nullable=false)
    private Integer id;

    @Column(name="category_id", insertable=false, updatable=false)
    private Integer CategoryId;

    @Column(name="company_id", insertable=false, updatable=false)
    private Integer CompanyId;

    @Column(name="title", nullable=false)
    @Size(max = 50)
    private String title;

    @Column(name="description", nullable=false)
    @Size(max = 2000)
    private String description;

    @Column(name="size", nullable=false)
    private Integer sizeDiscount;

    @Column(name="start_date", nullable=false)
    @Temporal(TemporalType.DATE)
    private Date dateBegin;

    @Column(name="end_date")
    @Temporal(TemporalType.DATE)
    private Date dateFinish;

    @Column(name="area", nullable=false)
    private Integer area;

    @Column(name="image", nullable=false)
    @Size(max = 300)
    private String imageDiscount;


}
