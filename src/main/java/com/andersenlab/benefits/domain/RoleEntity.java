package com.andersenlab.benefits.domain;

import lombok.*;
import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "roles")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class RoleEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@Column
	private String name;
	
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