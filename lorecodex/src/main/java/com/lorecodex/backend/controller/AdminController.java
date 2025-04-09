package com.lorecodex.backend.controller;

import com.lorecodex.backend.dto.request.AdminRoleRequest;
import com.lorecodex.backend.dto.response.UserListResponse;
import com.lorecodex.backend.model.Role;
import com.lorecodex.backend.model.User;
import com.lorecodex.backend.service.RoleService;
import com.lorecodex.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@CrossOrigin("*")
@RestController
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final RoleService roleService;

    @Autowired
    public AdminController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<UserListResponse>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        List<UserListResponse> userResponses = users.stream()
                .map(user -> new UserListResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getRoles().stream().map(Role::getName).collect(Collectors.toSet())
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(userResponses);
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Integer id) {
        try {
            Integer currentUserId = userService.getCurrentUserId();
            if (id.equals(currentUserId)) {
                return ResponseEntity.badRequest().body("No puedes eliminar tu propio usuario");
            }

            userService.deleteUser(id);
            return ResponseEntity.ok("Usuario eliminado correctamente");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al eliminar el usuario: " + e.getMessage());
        }
    }

    @PostMapping("/users/role")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> toggleAdminRole(@RequestBody AdminRoleRequest request) {
        try {
            Optional<User> userOpt = userService.getUserById(request.getUserId());
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Usuario no encontrado");
            }

            User user = userOpt.get();
            Role adminRole = roleService.getRoleByName("ROLE_ADMIN")
                    .orElseGet(() -> roleService.createRoleIfNotFound("ROLE_ADMIN"));

            Integer currentUserId = userService.getCurrentUserId();
            if (request.getUserId().equals(currentUserId) && !request.isAddRole()) {
                return ResponseEntity.badRequest().body("No puedes quitarte tu propio rol de administrador");
            }

            if (request.isAddRole()) {
                user.getRoles().add(adminRole);
            } else {
                user.getRoles().removeIf(role -> role.getName().equals("ROLE_ADMIN"));
            }

            userService.updateUser(user.getId(), user);
            return ResponseEntity.ok("Rol de administrador " +
                    (request.isAddRole() ? "asignado" : "removido") + " correctamente");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al modificar el rol: " + e.getMessage());
        }
    }
}