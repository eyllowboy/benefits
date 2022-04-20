package com.andersenlab.benefits.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Set;

@Schema(name = "Location", description = "Location entity")
@Entity
@Table(name = "locations")
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor()
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class LocationEntity {
    @Schema(description = "Identifier", type = "int64", minimum = "1")
    @EqualsAndHashCode.Exclude
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "location_id_seq")
    @SequenceGenerator(name = "location_id_seq", sequenceName = "location_id", allocationSize = 1)
    private Long id;

    @Schema(description = "Location country", type = "string", minLength = 1, maxLength = 15)
    @Size(min = 1, max = 15, message = "Country must be between 1 and 15 characters")
    @NotBlank
    @Column
    private String country;

    @Schema(description = "Location city", type = "string", minLength = 1, maxLength = 25)
    @Size(min = 1, max = 25, message = "City must be between 1 and 25 characters")
    @NotBlank
    @Column
    private String city;

    @EqualsAndHashCode.Exclude
    @JsonIgnore
    @ManyToMany(mappedBy = "area", fetch = FetchType.LAZY)
    private Set<DiscountEntity> discounts;

    public LocationEntity(Long id, String country, String city) {
        this.country = country;
        this.city = city;
        this.id = id;
    }

    public LocationEntity(String country, String city) {
        this.country = country;
        this.city = city;
    }

    @Override
    public String toString() {
        return "LocationEntity{" +
                "id=" + id +
                ", country='" + country + '\'' +
                ", city='" + city + '\'' +
                '}';
    }
}
