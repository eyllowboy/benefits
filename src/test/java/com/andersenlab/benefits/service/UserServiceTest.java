package com.andersenlab.benefits.service;

import com.andersenlab.benefits.domain.UserEntity;
import com.andersenlab.benefits.repository.LocationRepository;
import com.andersenlab.benefits.repository.RoleRepository;
import com.andersenlab.benefits.repository.UserRepository;
import com.andersenlab.benefits.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import java.util.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static com.andersenlab.benefits.service.ServiceTestUtils.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = "spring.main.lazy-initialization=true",
        classes = {UserService.class, UserServiceImpl.class})
public class UserServiceTest {
    private final UserServiceImpl userService;

    @MockBean
    private final UserRepository userRepository;
    @MockBean
    private final RoleRepository roleRepository;
    @MockBean
    private final LocationRepository locationRepository;

    @Autowired
    public UserServiceTest(final UserServiceImpl userService,final UserRepository userRepository,
                           final RoleRepository roleRepository, final LocationRepository locationRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.locationRepository = locationRepository;
    }

    @Test
    public void whenFindAllSuccess() {
        // given
        final List<UserEntity> usersList = getUserList();
        final Page<UserEntity> pageOfUsers = new PageImpl<>(usersList);
        when(this.userRepository.findAll(PageRequest.of(0, 10))).thenReturn(pageOfUsers);

        // when
        final Page<UserEntity> foundUserEntities = this.userService.findAll(PageRequest.of(0, 10));

        // then
        assertEquals(pageOfUsers, foundUserEntities);
        verify(this.userRepository, times(1)).findAll(PageRequest.of(0, 10));
    }

    @Test
    public void whenFindByIdSuccess() {
        // given
        final int userPos = getRndEntityPos();
        final List<UserEntity> usersList = getUserList();
        when(this.userRepository.findById(anyLong())).thenAnswer(invocation -> {
            final Long idx = invocation.getArgument(0);
            return Optional.of(usersList.get(idx.intValue()));
        });

        // when
        final UserEntity foundUserEntity = this.userService.findById((long) userPos);

        // then
        assertEquals(usersList.get(userPos), foundUserEntity);
        verify(this.userRepository, times(1)).findById((long) userPos);
    }

    @Test
    public void whenSaveSuccess() {
        // given
        final List<UserEntity> usersList = getUserList();
        final UserEntity user = getUser(getRndEntityPos());
        when(this.userRepository.save(any(UserEntity.class))).thenAnswer(invocation ->
                saveItem(usersList, invocation.getArgument(0), Objects::equals));

        // when
        final UserEntity savedUser = this.userService.save(user);

        // then
        assertEquals(user, savedUser);
        verify(this.userRepository, times(1)).save(user);
    }

    @Test
    public void whenDeleteSuccess() {
        // given
        final int userPos = getRndEntityPos();
        final List<UserEntity> usersList = getUserList();
        final UserEntity user = usersList.get(userPos);
        doAnswer(invocation -> {
            usersList.remove(usersList.stream()
                    .filter(item -> Objects.equals(item.getId(), invocation.getArgument(0)))
                    .findFirst().orElse(null));
            return null;
        }).when(this.userRepository).deleteById(anyLong());

        // when
        this.userService.delete(user.getId());

        // then
        Assertions.assertFalse(usersList.contains(user));
        verify(this.userRepository, times(1)).deleteById(user.getId());
    }

    @Test
    public void whenFindByLoginSuccess() {
        // given
        final int userPos = getRndEntityPos();
        final List<UserEntity> usersList = getUserList();
        when(this.userRepository.findByLogin(anyString())).thenAnswer(invocation -> usersList.stream().filter(item ->
                Objects.equals(item.getLogin(), invocation.getArgument(0))).findFirst());

        // when
        final Optional<UserEntity> foundUser = this.userRepository.findByLogin(usersList.get(userPos).getLogin());

        // then
        assertEquals(Optional.of(usersList.get(userPos)), foundUser);
        verify(this.userRepository, times(1)).findByLogin(usersList.get(userPos).getLogin());
    }

    @Test
    public void whenUpdateSuccess() {
        // given
        final int userPos = getRndEntityPos();
        final List<UserEntity> usersList = getUserList();
        final UserEntity user = usersList.get(userPos);
        doAnswer(invocation -> {
            final UserEntity newUser = new UserEntity(
                    invocation.getArgument(0),
                    invocation.getArgument(1),
                    invocation.getArgument(2),
                    invocation.getArgument(3));
            final Long idx = invocation.getArgument(0);
            usersList.set(idx.intValue(), newUser);
            return null;
        }).when(this.userRepository).save(any());

        // when
        this.userService.update((long) userPos, user);

        // then
        assertEquals(user, usersList.get(userPos));
        verify(this.userRepository, times(1))
                .save(user);
    }
}