package com.andersenlab.benefits.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Set;

@Schema(name = "Category", description = "Category entity")
@Entity
@Table(name = "categories")
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor()
public class CategoryEntity {

    @Schema(description = "Identifier", type = "int64", minimum = "1")
    @Id
    @EqualsAndHashCode.Exclude
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "category_id_seq")
    @SequenceGenerator(name = "category_id_seq", sequenceName = "category_id", allocationSize = 1)
    private Long id;

    @Schema(description = "Category title", type = "string", minLength = 3, maxLength = 20)
    @Size(min = 3, max = 50, message = "Title must be between 3 and 20 characters")
    @NotBlank
    @Column
    private String title;

    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ManyToMany(mappedBy = "categories", fetch = FetchType.LAZY)
    private Set<DiscountEntity> discounts;

    public CategoryEntity(final Long id, final String title) {
        this.id = id;
        this.title = title;
    }

    public CategoryEntity(final  String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return "CategoryEntity{" +
                "id=" + this.id +
                ", title='" + this.title + '\'' +
                '}';
    }
}
