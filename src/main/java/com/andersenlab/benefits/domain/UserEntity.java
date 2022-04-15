package com.andersenlab.benefits.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Objects;

@Schema(name = "User", description = "User entity")
@Entity
@Table(name = "users")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor()
public class UserEntity {
    @Schema(description = "Identifier", type = "int64", minimum = "1")
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_id_seq")
    @SequenceGenerator(name = "user_id_seq", sequenceName = "user_id", allocationSize = 1)
    private Long id;

    @Schema(description = "Unique login", type = "string", minLength = 3, maxLength = 20)
    @NotBlank
    @Column
    private String login;

    @Schema(description = "Role", type = "RoleEntity")
    @NotNull
    @ManyToOne
    @JoinColumn(name = "role_id")
    private RoleEntity roleEntity;

    @Schema(description = "Location", type = "LocationEntity")
    @ManyToOne
    @JoinColumn(name = "location_id")
    private LocationEntity location;

    public UserEntity(final String login, final RoleEntity roleEntity, final LocationEntity location) {
        this.login = login;
        this.roleEntity = roleEntity;
        this.location = location;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final UserEntity that = (UserEntity) o;
        return login.equals(that.login);
    }

    @Override
    public int hashCode() {
        return Objects.hash(login);
    }
}