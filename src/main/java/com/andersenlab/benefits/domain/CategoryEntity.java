package com.andersenlab.benefits.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.Objects;
import java.util.Set;

@Schema(name = "Category", description = "Category entity")
@Entity
@Table(name = "categories")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor()
public class CategoryEntity {

    @Schema(description = "Identifier", type = "int64", minimum = "1")
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "category_id_seq")
    @SequenceGenerator(name = "category_id_seq", sequenceName = "category_id", allocationSize = 1)
    private Long id;

    @Schema(description = "Category title", type = "string", minLength = 3, maxLength = 20)
    @NotBlank
    @Column
    private String title;

    @JsonIgnore
    @ManyToMany(mappedBy = "categories", fetch = FetchType.LAZY)
    private Set<DiscountEntity> discounts;

    public CategoryEntity(Long id, String title) {
        this.id = id;
        this.title = title;
    }

    public CategoryEntity(String title) {
        this.title = title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CategoryEntity that = (CategoryEntity) o;
        return Objects.equals(getTitle(), that.getTitle());
    }

    @Override
    public int hashCode() {
        return Objects.hash(title);
    }
}
