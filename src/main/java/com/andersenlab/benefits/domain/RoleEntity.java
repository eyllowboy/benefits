package com.andersenlab.benefits.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.Set;

@Schema(name = "Role", description = "Role entity")
@Entity
@Table(name = "roles")
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class RoleEntity {

    @Schema(description = "Identifier", type = "int64", minimum = "1")
    @EqualsAndHashCode.Exclude
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

    @EqualsAndHashCode.Exclude
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
    public String toString() {
        return "RoleEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                '}';
    }
}