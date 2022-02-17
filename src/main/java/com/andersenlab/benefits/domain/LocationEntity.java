package com.andersenlab.benefits.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.Objects;

@Schema(name = "Location", description = "Location entity")
@Entity
@Table(name = "locations")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor()
public class LocationEntity {
    @Schema(description = "Identifier", type = "int64", minimum = "1")
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "location_id_seq")
    @SequenceGenerator(name = "location_id_seq", sequenceName = "location_id", allocationSize = 1)
    private Long id;

    @Schema(description = "Location country", type = "string", minLength = 1, maxLength = 15)
    @NotBlank
    @Column
    private String country;

    @Schema(description = "Location city", type = "string", minLength = 1, maxLength = 25)
    @NotBlank
    @Column
    private String city;

    public LocationEntity(String country, String city) {
        this.country = country;
        this.city = city;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final LocationEntity that = (LocationEntity) o;
        return country.equals(that.country) && city.equals(that.city);
    }

    @Override
    public int hashCode() {
        return Objects.hash(country, city);
    }
}