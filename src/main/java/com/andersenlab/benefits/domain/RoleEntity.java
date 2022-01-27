package com.andersenlab.benefits.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.Objects;

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
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="role_id_seq")
	@SequenceGenerator(name="role_id_seq", sequenceName="role_id", allocationSize=1)
	private Long id;
	
	@Schema(description = "Role name", type = "string", minLength = 3, maxLength = 25)
	@NotBlank
	@Column
	private String name;
	
	@Schema(description = "Role unique identification code", type = "string", minLength = 3, maxLength = 20)
	@NotBlank
	@Column
	private String code;
	
	public RoleEntity(String name, String code) {
		this.name = name;
		this.code = code;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		RoleEntity that = (RoleEntity) o;
		return name.equals(that.name) && code.equals(that.code);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(name, code);
	}
}