package com.andersenlab.benefits.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.List;

@Schema(name = "Company", description = "Company entity")
@Entity
@Table(name = "company")
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class CompanyEntity {

    @Schema(description = "Identifier", type = "int64", minimum = "1")
    @Id
    @EqualsAndHashCode.Exclude
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "company_id_seq")
    @SequenceGenerator(name = "company_id_seq", sequenceName = "company_id", allocationSize = 1)
    private Long id;

    @Schema(description = "Title Company", type = "string", minLength = 1, maxLength = 50)
    @NotBlank
    @Column(name = "title")
    private String title;

    @Schema(description = "Description company", type = "string", minLength = 1, maxLength = 1000)
    @NotBlank
    @Column(name = "description")
    private String description;

    @Schema(description = "Address company", type = "string", minLength = 1, maxLength = 150)
    @Column(name = "address")
    private String address;

    @Schema(description = "Phone company", type = "string", minLength = 1, maxLength = 20)
    @NotBlank
    @Column(name = "phone")
    private String phone;

    @Schema(description = "Link company", type = "string", minLength = 1)
    @NotBlank
    @Column(name = "link")
    private String link;

    @OneToMany(mappedBy = "company")
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private List<DiscountEntity> discounts;

    public CompanyEntity(String title, String description, String address, String phone, String link) {
        this.title = title;
        this.description = description;
        this.address = address;
        this.phone = phone;
        this.link = link;
    }

    public CompanyEntity(Long id, String title, String description, String address, String phone, String link) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.address = address;
        this.phone = phone;
        this.link = link;
    }

    @Override
    public String toString() {
        return "CompanyEntity{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", address='" + address + '\'' +
                ", phone='" + phone + '\'' +
                ", link='" + link + '\'' +
                '}';
    }
}
