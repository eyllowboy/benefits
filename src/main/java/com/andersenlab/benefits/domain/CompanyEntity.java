package com.andersenlab.benefits.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.Objects;

@Schema(name = "Company", description = "Company entity")
@Entity
@Table(name = "company")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CompanyEntity {

    @Schema(description = "Identifier", type = "int64", minimum = "1")
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "company_id_seq")
    @SequenceGenerator(name = "company_id_seq", sequenceName = "company_id", allocationSize = 1)
    private Long id;

    @Schema(description = "Title Company", type = "string", minLength = 1, maxLength = 50)
    @NotBlank
    @Column(name = "title", length = 50)
    private String title;

    @Schema(description = "Description company", type = "string", minLength = 1, maxLength = 1000)
    @NotBlank
    @Column(name = "description", length = 1000)
    private String description;

    @Schema(description = "Address company", type = "string", minLength = 1, maxLength = 150)
    @NotBlank
    @Column(name = "address", length = 150)
    private String address;

    @Schema(description = "Phone company", type = "string", minLength = 1, maxLength = 20)
    @NotBlank
    @Column(name = "phone", length = 20)
    private String phone;

    @Schema(description = "Link company", type = "string", minLength = 1)
    @NotBlank
    @Column(name = "link")
    private String link;

    public CompanyEntity(String title, String description, String address, String phone, String link) {
        this.title = title;
        this.description = description;
        this.address = address;
        this.phone = phone;
        this.link = link;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompanyEntity that = (CompanyEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(title, that.title) && Objects.equals(description, that.description) && Objects.equals(address, that.address) && Objects.equals(phone, that.phone) && Objects.equals(link, that.link);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, description, address, phone, link);
    }
}
