package com.andersenlab.benefits.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.Set;

@Schema(name = "Role", description = "Role entity")
@Entity
@Table(name = "roles")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class RoleEntity {

    @Schema(description = "Identifier", type = "int64", minimum = "1")
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "role_id_seq")
    @SequenceGenerator(name = "role_id_seq", sequenceName = "role_id", allocationSize = 1)
    private Long id;

    @Schema(description = "Role name", type = "string", minLength = 3, maxLength = 25)
    @NotBlank
    @Column
    private String name;

    @Schema(description = "Role unique identification code", type = "string", minLength = 3, maxLength = 20)
    @NotBlank
    @Column
    private String code;

    @JsonIgnore
    @OneToMany(mappedBy = "roleEntity", fetch = FetchType.LAZY)
    private Set<UserEntity> users;

    public RoleEntity(final String name, final String code) {
        this.name = name;
        this.code = code;
    }

    public RoleEntity(final Long id, final String name, final String code) {
        this.id = id;
        this.name = name;
        this.code = code;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final RoleEntity that = (RoleEntity) o;
        return name.equals(that.name) && code.equals(that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, code);
    }
}