package com.andersenlab.benefits.service.impl;

import com.andersenlab.benefits.domain.LocationEntity;
import com.andersenlab.benefits.domain.RoleEntity;
import com.andersenlab.benefits.domain.UserEntity;
import com.andersenlab.benefits.repository.LocationRepository;
import com.andersenlab.benefits.repository.RoleRepository;
import com.andersenlab.benefits.repository.UserRepository;
import com.andersenlab.benefits.service.UserService;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.*;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.Response;
import java.util.*;

import static com.andersenlab.benefits.service.impl.ValidateUtils.errAlreadyExistMessage;
import static com.andersenlab.benefits.service.impl.ValidateUtils.validateEntityFieldsAnnotations;

/**
 * An implementation for performing operations on a {@link UserEntity}.
 *
 * @author Andrei Rabchun
 * @version 1.0
 * @see UserService
 */
@Service
public class UserServiceImpl implements UserService {
    private final Environment env;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final LocationRepository locationRepository;
    private final Keycloak keycloak;

    @Autowired
    public UserServiceImpl(final Environment env,
                           final UserRepository userRepository,
                           final RoleRepository roleRepository,
                           final LocationRepository locationRepository) {
        this.env = env;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.locationRepository = locationRepository;
        this.keycloak = initKeycloak();
    }

    @Override
    public Page<UserEntity> findAll(final Pageable pageable) {
        return this.userRepository.findAll(pageable);
    }

    @Override
    public Optional<UserEntity> findById(final Long id) {
        return this.userRepository.findById(id);
    }

    @Override
    public Optional<UserEntity> findByLogin(final String login) {
        return this.userRepository.findByLogin(login);
    }

    @Override
    public UserEntity createNewUser(final String login, final String password) {
        this.userRepository.findByLogin(login).ifPresent(foundUser -> {
            throw new IllegalStateException(errAlreadyExistMessage("User", "login", login));});
        final RoleEntity role = this.roleRepository.findByCode("ROLE_USER").orElseThrow(() ->
                new IllegalStateException("No suitable role for ordinary users"));
        final LocationEntity location = this.locationRepository.findByCity("Белоруссия", "Минск").orElseThrow(() ->
                new IllegalStateException("No base location Белоруссия/Минск found"));
        final UserEntity user = new UserEntity(login, role, location);
        validateEntityFieldsAnnotations(user, true);
        try (final Response ignored = addKeycloakUser(user.getLogin(), password)) {
            return save(user);
        }
    }

    @Override
    public UserEntity save(final UserEntity user) {
        return this.userRepository.save(user);
    }

    @Override
    public void updateUserEntity(final Long id, final String login, final RoleEntity roleEntity, final LocationEntity location) {
        final UserEntity user = new UserEntity(id, login, roleEntity, location);
        validateEntityFieldsAnnotations(user, false);
        this.userRepository.updateUserEntity(user.getId(), user.getLogin(), user.getRoleEntity(), user.getLocation());
    }

    @Override
    public void delete(final Long id) {
        final UserEntity user = this.userRepository.findById(id).orElseThrow(() ->
                new IllegalStateException(ValidateUtils.errNotFoundMessage("User", id)));
        try (final Response ignored = deleteKeycloakUser(user)) {
            this.userRepository.delete(user);
        }
    }
    private Keycloak initKeycloak() {
        final String serverUrl = this.env.getProperty("keycloak.auth-server-url");
        final String mainRealmName = "master";
        final String adminLogin = "admin";
        final String adminPass = "admin";
        final String clientId = "admin-cli";
        return KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(mainRealmName)
                .grantType(OAuth2Constants.PASSWORD)
                .clientId(clientId)
                .username(adminLogin)
                .password(adminPass)
                .build();
    }

    private Response deleteKeycloakUser(final UserEntity user) {
        final RealmResource realm = this.keycloak.realm(this.env.getProperty("keycloak.realm"));
        final UsersResource users = realm.users();
        final List<UserRepresentation> existingUser = users.search(user.getLogin());
        if (!Objects.isNull(existingUser) && existingUser.size() > 0) {
            final String userId = existingUser.get(0).getId();
            return users.delete(userId);
        }
        return Response.notModified().build();
    }

    private Response addKeycloakUser(final String login, final String password) {
        final RealmResource realm = this.keycloak.realm(this.env.getProperty("keycloak.realm"));
        final UsersResource users = realm.users();
        final RoleRepresentation userRole = realm.roles().get("ROLE_USER").toRepresentation();
        final UserRepresentation newUser = new UserRepresentation();
        newUser.setUsername(login);
        newUser.setEnabled(true);
        try (final Response response = users.create(newUser)) {
            final String userId = CreatedResponseUtil.getCreatedId(response);
            final UserResource addedUser = users.get(userId);
            addedUser.roles().realmLevel().add(List.of(userRole));
            final CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
            credentialRepresentation.setTemporary(false);
            credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
            credentialRepresentation.setValue(password);
            addedUser.resetPassword(credentialRepresentation);
            return response;
        }
    }
}
