package com.andersenlab.benefits.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Schema(name = "User", description = "User entity")
@Entity
@Table(name = "users")
@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor()
public class UserEntity {
    @Schema(description = "Identifier", type = "int64", minimum = "1")
    @EqualsAndHashCode.Exclude
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_id_seq")
    @SequenceGenerator(name = "user_id_seq", sequenceName = "user_id", allocationSize = 1)
    private Long id;

    @Schema(description = "Unique login", type = "string", minLength = 3, maxLength = 20)
    @Size(min = 3, max = 20, message = "Login must be between 3 and 20 characters")
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
}
