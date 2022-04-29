package com.andersenlab.benefits.service;

import com.andersenlab.benefits.domain.RoleEntity;
import com.andersenlab.benefits.repository.RoleRepository;
import com.andersenlab.benefits.service.impl.RoleServiceImpl;
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
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static com.andersenlab.benefits.service.ServiceTestUtils.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = "spring.main.lazy-initialization=true",
        classes = {RoleService.class, RoleServiceImpl.class})
public class RoleServiceTest {
    private final RoleService roleService;

    @MockBean
    RoleRepository roleRepository;

    @Autowired
    public RoleServiceTest(final RoleService roleService) {
        this.roleService = roleService;
    }

    @Test
    public void whenFindAllSuccess() {
        // given
        final List<RoleEntity> rolesList = getRoleList();
        final Page<RoleEntity> pageOfRole = new PageImpl<>(rolesList);
        when(this.roleRepository.findAll(PageRequest.of(0, 3))).thenReturn(pageOfRole);

        // when
        final Page<RoleEntity> foundRoleEntities = this.roleService.findAll(PageRequest.of(0, 3));

        // then
        assertEquals(pageOfRole, foundRoleEntities);
        verify(this.roleRepository, times(1)).findAll(PageRequest.of(0, 3));
    }

    @Test
    public void whenFindByIdSuccess() {
        // given
        final int rolePos = getRndEntityPos();
        final List<RoleEntity> rolesList = getRoleList();
        when(this.roleRepository.findById(anyLong())).thenAnswer(invocation -> {
            final Long idx = invocation.getArgument(0);
            return Optional.of(rolesList.get(idx.intValue()));
        });

        // when
        final RoleEntity foundRoleEntity = this.roleService.findById((long) rolePos);

        // then
        assertEquals(rolesList.get(rolePos), foundRoleEntity);
        verify(this.roleRepository, times(1)).findById((long) rolePos);
    }

    @Test
    public void whenFindByCodeSuccess() {
        // given
        final int rolePos = getRndEntityPos();
        final List<RoleEntity> rolesList = getRoleList();
        when(this.roleRepository.findByCode(anyString())).thenAnswer(invocation ->
                rolesList.stream().filter(item ->
                        Objects.equals(item.getCode(), invocation.getArgument(0))).findFirst());

        // when
        final Optional<RoleEntity> foundRole = this.roleRepository.findByCode(rolesList.get(rolePos).getCode());

        // then
        assertEquals(Optional.of(rolesList.get(rolePos)), foundRole);
        verify(this.roleRepository, times(1)).findByCode(rolesList.get(rolePos).getCode());
    }

    @Test
    public void whenSaveSuccess() {
        // given
        final List<RoleEntity> rolesList = getRoleList();
        final RoleEntity role = getRole(getRndEntityPos());
        when(this.roleRepository.save(any(RoleEntity.class))).thenAnswer(invocation ->
                saveItem(rolesList, invocation.getArgument(0), Objects::equals));

        // when
        final RoleEntity savedRole = this.roleService.save(role);

        // then
        assertEquals(role, savedRole);
        verify(this.roleRepository, times(1)).save(role);
    }

    @Test
    public void whenUpdateSuccess() {
        // given
        final int rolePos = getRndEntityPos();
        final List<RoleEntity> rolesList = getRoleList();
        final RoleEntity role = rolesList.get(rolePos);
        when(this.roleRepository.findByCode(anyString())).thenReturn(Optional.empty());
        when(this.roleRepository.findById(anyLong())).thenReturn(Optional.of(role));

        //when
        this.roleService.update(role.getId(), role);

        // then
        verify(this.roleRepository, times(1)).save(eq(role));
    }
    @Test
    public void whenUpdateTheSameCode() {
        // given
        final int rolePos = getRndEntityPos();
        final List<RoleEntity> rolesList = getRoleList();
        final RoleEntity role = rolesList.get(rolePos);
        final RoleEntity roletheSameCode = new RoleEntity(role.getName(),role.getCode());
        roletheSameCode.setId(99L);

        // when
        when(this.roleRepository.findByCode(anyString())).thenReturn(Optional.of(roletheSameCode));
        final IllegalStateException exception = assertThrows(IllegalStateException.class, () -> this.roleService.update(role.getId(), role));

        // then
        assertTrue(exception.getMessage().contains(role.getCode() + " already exist in database"));
    }

    @Test
    public void whenDeleteSuccess() {
        // given
        final int rolePos = getRndEntityPos();
        final List<RoleEntity> rolesList = getRoleList();
        final RoleEntity role = rolesList.get(rolePos);
        when(this.roleRepository.findById(anyLong())).thenReturn(Optional.of(role));

        // when
        this.roleService.delete(role.getId());

        // then
        verify(this.roleRepository, times(1)).delete(eq(role));
    }

    @Test
    public void whenFindWithAssociatedUsers() {
        // given
        final int rolePos = getRndEntityPos();
        final List<RoleEntity> rolesList = getRoleList();
        when(this.roleRepository.findWithAssociatedUsers(anyLong())).thenAnswer(invocation -> {
            final Long idx = invocation.getArgument(0);
            final RoleEntity role = rolesList.get(idx.intValue());
            role.setUsers(new HashSet<>(getUserList()));
            return Optional.of(role);
        });

        // when
        final RoleEntity foundRoleEntity = this.roleService.findWithAssociatedUsers((long) rolePos).orElseThrow();

        // then
        assertEquals(rolesList.get(rolePos), foundRoleEntity);
        assertNotNull(foundRoleEntity.getUsers());
        verify(this.roleRepository, times(1)).findWithAssociatedUsers((long) rolePos);
    }
}