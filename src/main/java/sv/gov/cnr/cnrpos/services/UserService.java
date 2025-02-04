package sv.gov.cnr.cnrpos.services;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import sv.gob.cnr.ClienteSisucc.APIClient;
import sv.gov.cnr.cnrpos.entities.Menu;
import sv.gov.cnr.cnrpos.exceptions.ResourceNotFoundException;
import sv.gov.cnr.cnrpos.models.dto.RolDTO;
import sv.gov.cnr.cnrpos.models.dto.UserDTO;
import sv.gov.cnr.cnrpos.models.dto.sisucc.DtoSisucc;
import sv.gov.cnr.cnrpos.models.mappers.security.UserMapper;
import sv.gov.cnr.cnrpos.models.security.Rol;
import sv.gov.cnr.cnrpos.models.security.User;
import sv.gov.cnr.cnrpos.repositories.security.RolRepository;
import sv.gov.cnr.cnrpos.repositories.security.UserRepository;
import sv.gov.cnr.cnrpos.security.AuthenticationRequest;
import sv.gov.cnr.cnrpos.security.AuthenticationService;
import sv.gov.cnr.cnrpos.services.sissuc.SissucService;
import sv.gov.cnr.cnrpos.utils.RequestParamParser;

import java.net.ConnectException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final RequestParamParser requestParamParser;


    @Value("${cnrapps.SISID}")
    private long sisId;

    @Autowired
    private UserMapper userMapper;

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> getUser(Long id) {
        return userRepository.findById(id);
    }

    public UserDTO getByUsername(String username) {
        User user = userRepository.findByUsuario(username).orElse(null);
        return userMapper.toDTO(user);
    }

    public UserDTO getUserDTO(Long id) {
        User user = userRepository.findById(id).orElse(null);
        return userMapper.toDTO(user);
    }

    public Page<UserDTO> getPage(int page, int size, String filterBy, String sortBy) {
        Sort sort = sortBy.isEmpty() ? Sort.by("idUser").ascending() : requestParamParser.parseSortBy(sortBy);
        Map<String, String> filterParams = requestParamParser.parseFilterBy(filterBy);

        System.out.println("user service " + sort);
        System.out.println("user service filter params" + filterParams);
        Pageable pageable = PageRequest.of(page, size, sort);


//        Page<User> users = userRepository.findAll(PageRequest.of(page, size, sort));

        Page<User> users;
        if (!filterParams.isEmpty()) {
            users = userRepository.findAll(requestParamParser.withFilters(filterParams), pageable);
        } else {
            users = userRepository.findAll(pageable);
        }

        return users.map(userMapper::toPageDTO);
    }

    public User saveUser(User user) throws ConnectException, RuntimeException, Exception {
        try {
            List<User> emailExist = userRepository.findByEmailOverrided(user.getEmail());

            if (!emailExist.isEmpty()) {
                throw new ResourceNotFoundException("El correo seleccionado ya esta en uso");
            }

            //revisar si el usuario existe dentro del sistema
            List<User> userExist = userRepository.findByUsuarioOverrided(user.getUsuario());

            if (!userExist.isEmpty()) {
                throw new ResourceNotFoundException("El usuario seleccionado ya esta en uso");
            }

            Set<Rol> roles = user.getRolIds() != null && !user.getRolIds().isEmpty() ? new HashSet<>(rolRepository.findAllById(user.getRolIds())) : null;

            if (roles != null) {
                user.setRoles(roles);
            } else {
                user.getRoles().clear();
            }

            //verificar si el usuario existe en el api para guardar el nuevo registro.

            APIClient api = new APIClient();
            String resp = api.login(user.getUsuario(), user.getPassword(), "" + this.sisId);

            if (resp == null) {
                throw new ResourceNotFoundException("api - sissuc no retorno el resultado esperado");
            }

            DtoSisucc dtoSisucc = new Gson().fromJson(resp, DtoSisucc.class);

            if (dtoSisucc.getCodigo() != 1) {
                throw new ResourceNotFoundException(dtoSisucc.getMensaje() + " - CÓDIGO: " + dtoSisucc.getCodigo());
            }


            user.setPassword(passwordEncoder.encode(user.getPassword()));
            return userRepository.save(user);
        } catch (ResourceNotFoundException ex) {
            throw new ResourceNotFoundException(ex.getMessage());
        } catch (ConnectException e) {
            throw new ConnectException("No se obtuvo respuesta del servidor - sissuc");
        } catch (Exception exc) {
            throw new Exception("Error desconocido: " + exc.getMessage());
        }
    }


//    public User updateUser(Long id, User userReq) {
//        User user = getUser(id).orElseThrow(null);
//
//        if (user == null) {
//            throw new ResourceNotFoundException("No se encontro usuario con id " + id);
//        }
//
//        User emailExist = userRepository.findByEmail(userReq.getEmail()).orElseThrow(null);
//        if (emailExist != null) {
//            throw new ResourceNotFoundException("El correo seleccionado ya esta en uso");
//        }
//
//
//        user.setIdUser(userReq.getIdUser());
//        user.setFirstname(userReq.getFirstname());
//        user.setLastname(userReq.getLastname());
//        user.setEmail(userReq.getEmail());
//        user.setDocType(userReq.getDocType());
//        user.setDocNumber(userReq.getDocNumber());
////        user.setPassword(userReq.getPassword());
//        user.setPhone(userReq.getPhone());
//        user.setIsActive(userReq.getIsActive());
//        user.setTestMode(userReq.getTestMode());
//        user.setIdCompany(userReq.getIdCompany());
//        user.setIdBranch(userReq.getIdBranch());
//        user.setIdPosition(userReq.getIdPosition());
//
//
////        if (userReq.getPassword() != null || userReq.getPassword() != "") {
////            user.setPassword(passwordEncoder.encode(userReq.getPassword()));
////        }else {
////            user.setPassword(user.getPassword());
////        }
//
//        Set<Rol> roles = userReq.getRolIds() != null && !userReq.getRolIds().isEmpty() ? new HashSet<>(rolRepository.findAllById(userReq.getRolIds())) : null;
//
//        if (roles != null) {
//            user.setRoles(roles);
//        } else {
//            user.getRoles().clear();
//        }
//
//        return userRepository.save(user);
//
//    }

    public User updateUser(Long id, User userReq) throws ConnectException, RuntimeException, Exception {

        try {
            // Buscar el usuario existente por su ID
            User user = getUser(id).orElseThrow(() -> new ResourceNotFoundException("No se encontró usuario con ID " + id));

            // Verificar si el correo electrónico proporcionado ya está en uso, pero excluir el correo del usuario actual
            if (!user.getEmail().equals(userReq.getEmail())) {
                List<User> emailExist = userRepository.findByEmailOverrided(userReq.getEmail());
                if (!emailExist.isEmpty()) {
                    throw new ResourceNotFoundException("El correo seleccionado ya esta en uso");
                }
            }

            // Verificar si el nombre de usuario proporcionado ya está en uso, pero excluir el nombre de usuario del usuario actual
            if (user.getUsuario() != null && !user.getUsuario().equals(userReq.getUsuario())) {
                List<User> userExist = userRepository.findByUsuarioOverrided(userReq.getUsuario());
                if (!userExist.isEmpty()) {
                    throw new ResourceNotFoundException("El usuario seleccionado ya esta en uso");
                }
            }

            // Verificar si el usuario existe en el API para guardar el nuevo registro
//            APIClient api = new APIClient();
//            String resp = api.login(user.getUsuario(), user.getPassword(), "" + this.sisId);
//
//            if (resp == null) {
//                throw new ResourceNotFoundException("API - SISSUC no retornó el resultado esperado");
//            }
//
//            DtoSisucc dtoSisucc = new Gson().fromJson(resp, DtoSisucc.class);
//
//            if (dtoSisucc.getCodigo() != 1) {
//                throw new ResourceNotFoundException(dtoSisucc.getMensaje() + " - CÓDIGO: " + dtoSisucc.getCodigo());
//            }

            // Actualizar los campos del usuario
            user.setFirstname(userReq.getFirstname());
            user.setLastname(userReq.getLastname());
            user.setEmail(userReq.getEmail());
            user.setDocType(userReq.getDocType());
            user.setDocNumber(userReq.getDocNumber());
            user.setPhone(userReq.getPhone());
            user.setIsActive(userReq.getIsActive());
            user.setTestMode(userReq.getTestMode());
            user.setIdCompany(userReq.getIdCompany());
            user.setIdBranch(userReq.getIdBranch());
            user.setIdPosition(userReq.getIdPosition());

            // Actualizar roles (si se proporcionan)
            Set<Rol> roles = userReq.getRolIds() != null && !userReq.getRolIds().isEmpty() ? new HashSet<>(rolRepository.findAllById(userReq.getRolIds())) : null;

            if (roles != null) {
                user.setRoles(roles);
            } else {
                user.getRoles().clear();
            }

            // Guardar los cambios en la base de datos
            return userRepository.save(user);
        } catch (ResourceNotFoundException ex) {
            throw new ResourceNotFoundException(ex.getMessage());
        } catch (Exception exc) {
            throw new Exception("Error desconocido: " + exc.getMessage());
        }

    }


    public User updateUserStatus(Long id, User userReq) {
        User user = getUser(id).orElseThrow(null);

        if (user == null) {
            throw new ResourceNotFoundException("No se encontro usuario con id " + id);
        }

        user.setIsActive(userReq.getIsActive());
        user.setTestMode(userReq.getTestMode());

        return userRepository.save(user);

    }

    public User deleteUser(Long id) {
        User user = getUser(id).orElse(null);

        if (user == null) {
            throw new ResourceNotFoundException("No se encontró el menu con id " + id);
        }

        user.setDeletedAt(Timestamp.valueOf(LocalDateTime.now()));

        return userRepository.save(user);
    }


}
