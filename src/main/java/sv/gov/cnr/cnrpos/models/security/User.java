package sv.gov.cnr.cnrpos.models.security;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "CNRPOS_USERS")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "users_seq")
    @SequenceGenerator(name = "users_seq", sequenceName = "USERS_SEQ", allocationSize = 1)
    @Column(name = "ID_USER")
    private Long idUser;

    @Nullable
    private String firstname;
    @Nullable
    private String lastname;


    @Nullable
    @Column(unique = true)
    private String email;

    @Nullable
    private Long docType;

    @Nullable
    private String docNumber;
    private String password;
    @Nullable
    private String phone;

    private Boolean isActive = true;

    @Nullable
    private Boolean resetPassword = false;

    private Boolean testMode = false;

    @Nullable
    private Long idCompany = 1L;

    @Nullable
    private Long idBranch;

    @Nullable
    private Long idPosition;

    //    @NotNull(message = "Carnet es requerido")
    @Column(unique = true)
    private String carnet;

    //    @NotNull(message = "Usuario es requerido")
    @Nullable
    @Column(unique = true)
    private String usuario;


    //    @NotNull(message = "Tipo es requerido")
    @Nullable
    private String tipo;

    @Column(name = "CREATED_AT")
    @CreationTimestamp
    private Timestamp createdAt;

    @Column(name = "UPDATED_AT")
    @UpdateTimestamp
    private Timestamp updatedAt;

    @Column(name = "DELETED_AT")
    private Timestamp deletedAt;


    @Transient
    private List<Long> rolIds;

//    @Enumerated(EnumType.STRING)
//    private Role role;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "CNRPOS_USERS_ROLES", joinColumns = @JoinColumn(name = "ID_USER"), inverseJoinColumns = @JoinColumn(name = "ID_ROLE"))
    private Set<Rol> roles;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
//        return List.of(new SimpleGrantedAuthority(role.name()));
        return null;
    }

    //    @Override
    public List<Long> getRoless() {
//        return List.of(getRoles().)
//        return roles.stream().map(Rol::getIdRole).collect(Collectors.toList());
        return Optional.ofNullable(roles).map(r -> r.stream().map(Rol::getIdRole).collect(Collectors.toList())).orElse(Collections.emptyList());

    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return usuario;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
