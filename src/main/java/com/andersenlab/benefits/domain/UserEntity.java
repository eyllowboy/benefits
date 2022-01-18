package com.andersenlab.benefits.domain;

import lombok.*;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "users")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor()
public class UserEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@Column
	private String login;
	
	@ManyToOne
	@JoinColumn(name = "role_id")
	private RoleEntity roleEntity;
	
	public UserEntity(String login, RoleEntity roleEntity) {
		this.login = login;
		this.roleEntity = roleEntity;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserEntity that = (UserEntity) o;
		return login.equals(that.login);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(login);
	}
}