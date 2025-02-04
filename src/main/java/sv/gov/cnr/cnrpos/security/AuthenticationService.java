package sv.gov.cnr.cnrpos.security;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import sv.gob.cnr.ClienteSisucc.APIClient;
import sv.gov.cnr.cnrpos.config.JwtService;
import sv.gov.cnr.cnrpos.entities.Sucursal;
import sv.gov.cnr.cnrpos.exceptions.ResourceNotFoundException;
import sv.gov.cnr.cnrpos.models.dto.sisucc.DtoSisucc;
import sv.gov.cnr.cnrpos.models.enums.TokenType;
import sv.gov.cnr.cnrpos.models.security.Auth;
import sv.gov.cnr.cnrpos.models.security.Rol;
import sv.gov.cnr.cnrpos.models.security.Token;
import sv.gov.cnr.cnrpos.models.security.User;
import sv.gov.cnr.cnrpos.repositories.security.RolRepository;
import sv.gov.cnr.cnrpos.repositories.security.TokenRepository;
import sv.gov.cnr.cnrpos.repositories.security.UserRepository;
import sv.gov.cnr.cnrpos.services.MenuService;
import org.springframework.beans.factory.annotation.Value;
import sv.gov.cnr.cnrpos.services.SucursalService;
import sv.gov.cnr.cnrpos.services.UserService;
import sv.gov.cnr.cnrpos.utils.Utils;

import java.net.ConnectException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final MenuService menuService;
    private final UserService userService;
    private final RolRepository rolRepository;
    private final Utils utils;
    private final SucursalService sucursalService;

    @Value("${cnrapps.SISID}")
    private long sisId;

    public Auth register(RegisterRequest request) {
        var user = User.builder().firstname(request.getFirstname()).lastname(request.getLastname()).email(request.getEmail()).password(passwordEncoder.encode(request.getPassword()))
//                .role(request.getRole())
                .build();

        var userSaved = userRepository.save(user);
        var jwtToken = jwtService.generateToken(user);
        //var refreshToken = jwtService.generateRefreshToken(user);

        return Auth.builder().token(jwtToken).build();


    }

    public Map<String, Object> authenticated(AuthenticationRequest request) {

        try {
            Map<String, Object> userData = new HashMap<>();
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsuario(), request.getClave()));
            User user = userRepository.findByUsuario(request.getUsuario()).orElse(null);

            if (user == null) {
                throw new BadCredentialsException("Usuario y/o contrañsena incorrectos");
            }
            String jwtToken = jwtService.generateToken(user);

            revokeAllUserTokens(user);
            saveUserToken(user, jwtToken);

            Map<String, Object> requeridos = new HashMap<>();
            //los indices deben ser los mismos nombres que los campos del formulario en angular
            if (user.getIdBranch() == null) {
                requeridos.put("idBranch", true);
            }

            if (user.getDocNumber() == null) {
                requeridos.put("docNumber", true);
            }

            userData.put("reset_password", user.getResetPassword());
            userData.put("requerido_facturacion", requeridos);
            userData.put("username", user.getUsuario());
            userData.put("token", Auth.builder().token(jwtToken).build().getToken());
            userData.put("menu", menuService.findMenuMenuItemsIdsByUserIdv2(user.getIdUser()));

            return userData;

        } catch (BadCredentialsException bd) {
            throw new BadCredentialsException("Usuario y/o contrañsena incorrectos");
        } catch (Exception ex) {
            throw new ResourceNotFoundException("Error desconocido");
        }
    }


    public Map<String, Object> authenticatedSisucc(AuthenticationRequest request) throws Exception {
        try {
            APIClient api = new APIClient();
            Map<String, Object> userData = new HashMap<>();
            String resp = api.login(request.getUsuario(), request.getClave(), "" + this.sisId);
            String[] nombreSplit = new String[]{};


            //System.out.println("Respuesta: " + resp);
            if (resp == null) {
                throw new ResourceNotFoundException("api - sissuc no retorno el resultado esperado");
            }

            DtoSisucc dtoSisucc = new Gson().fromJson(resp, DtoSisucc.class);

            System.out.println("codigo: " + dtoSisucc.getCodigo() + " Mensaje: " + dtoSisucc.getMensaje());
            if (dtoSisucc.getCodigo() != 1) {
                userData.put("mensaje", dtoSisucc.getMensaje() + " - CÓDIGO: " + dtoSisucc.getCodigo());
                userData.put("codigo", dtoSisucc.getCodigo());
                return userData;
            }

            if (dtoSisucc.getUsuario().getEmpNombreCnr() != null) {
                nombreSplit = Utils.splitBySecondSpace(dtoSisucc.getUsuario().getEmpNombreCnr());
            }


            User user = userRepository.loginSissuc(dtoSisucc.getUsuario().getCarnet(), dtoSisucc.getUsuario().getUsuario()).orElse(null);


            if (user == null) {
                //No se encontro el usuario entonces se creara uno nuevo

                User userNuevo = new User();
                userNuevo.setFirstname(nombreSplit[0]);
                userNuevo.setLastname(nombreSplit[1]);
                userNuevo.setDocNumber(dtoSisucc.getUsuario().getEmpDui().replaceAll("-", ""));
                userNuevo.setUsuario(dtoSisucc.getUsuario().getUsuario());
                userNuevo.setCarnet(dtoSisucc.getUsuario().getCarnet());
                userNuevo.setPassword(passwordEncoder.encode(request.getClave()));
                userNuevo.setEmail(dtoSisucc.getUsuario().getUsuario() + "@cnr.gob.sv");

                //deberia existir un roll para nuevo ingreso
                userNuevo.setRolIds(Arrays.asList(1L, 21L));

                // Actualizar roles (si se proporcionan)
                Set<Rol> roles = userNuevo.getRolIds() != null && !userNuevo.getRolIds().isEmpty() ? new HashSet<>(rolRepository.findAllById(userNuevo.getRolIds())) : null;

                if (roles != null) {
                    userNuevo.setRoles(roles);
                } else {
                    userNuevo.getRoles().clear();
                }

                User userSaved = userRepository.save(userNuevo);
                if (userSaved.getIdUser() == null) {
                    throw new ResourceNotFoundException("No fue posible crear un nuevo usuario");
                }
                user = userSaved;
            }

            if (!passwordEncoder.matches(request.getClave(), user.getPassword())) {
                //se guarda una nueva contraseña debido a que si llega hasta aqui es por sisucc
                user.setPassword(passwordEncoder.encode(request.getClave()));
                userRepository.save(user);
            }

            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsuario(), request.getClave()));
            String jwtToken = jwtService.generateToken(user);
            revokeAllUserTokens(user);
            saveUserToken(user, jwtToken);

            Map<String, Object> requeridos = new HashMap<>();
            //los indices deben ser los mismos nombres que los campos del formulario en angular
            if (user.getIdBranch() == null) {
                requeridos.put("idBranch", true);
            }

            if (user.getDocNumber() == null) {
                requeridos.put("docNumber", true);
            }

            userData.put("reset_password", user.getResetPassword());
            userData.put("requerido_facturacion", requeridos);
            userData.put("username", user.getUsuario());
            userData.put("codigo", dtoSisucc.getCodigo());
            userData.put("token", Auth.builder().token(jwtToken).build().getToken());
            userData.put("menu", menuService.findMenuMenuItemsIdsByUserIdv2(user.getIdUser()));
            String codDepartamento = null;
            if (user.getIdBranch() != null){
                Sucursal sucursal = sucursalService.getSucursal(user.getIdBranch());
                codDepartamento = String.valueOf(sucursal.getIdDeptoBranch() != null ? sucursal.getIdDeptoBranch() : 80);
            }else {
                codDepartamento = "80";
            }


            userData.put("departamento", codDepartamento);
            userData.put("nombre", user.getFirstname().concat(" ").concat(user.getLastname()));
            userData.put("dui", user.getDocNumber().replace("-", ""));

            return userData;

        } catch (ResourceNotFoundException ex) {
            throw new ResourceNotFoundException("No se encontro ningun resultado");
        } catch (ArrayIndexOutOfBoundsException aubo) {
            throw new ArrayIndexOutOfBoundsException("longitud de nombre de empleado es incorrecta");
        } catch (ConnectException e) {
            throw new ConnectException("No se obtuvo respuesta del servidor - sissuc");
        } catch (Exception exc) {
            throw new Exception("Error desconocido: " + exc.getMessage());
        }

    }

    public Map<String, Object> cambiarSisucc(AuthenticationRequest request) throws Exception {
        try {
            APIClient api = new APIClient();
            Map<String, Object> userData = new HashMap<>();

            String resp = api.cambiarClave(request.getUsuario(), request.getClave(), request.getNuevaClave(), request.getClaveConfirmada(), "" + this.sisId);

            if (resp == null) {
                throw new ResourceNotFoundException("API - cambiar clave no retorno el resultado esperado");
            }

            DtoSisucc dtoSisucc = new Gson().fromJson(resp, DtoSisucc.class);

            if (dtoSisucc.getCodigo() != 20) {
                throw new ResourceNotFoundException(dtoSisucc.getMensaje() + " - CÓDIGO: " + dtoSisucc.getCodigo());
            }

            List<User> user = userRepository.findByUsuarioOverrided(request.getUsuario());
            User userNew;

            if (user.isEmpty()) {
                // User does not exist, create a new one
                userNew = new User();
                userNew.setFirstname(dtoSisucc.getUsuario().getUsuario());
                userNew.setLastname(dtoSisucc.getUsuario().getUsuario());
                userNew.setUsuario(dtoSisucc.getUsuario().getUsuario());
                userNew.setCarnet(dtoSisucc.getUsuario().getCarnet());
                userNew.setEmail(dtoSisucc.getUsuario().getUsuario() + "@cnr.gob.sv");
                userNew.setPassword(passwordEncoder.encode(request.getClave()));
                userNew.setIsActive(true);
                userNew.setTestMode(false);
                userNew.setIdCompany(1L);
                userNew.setRolIds(Arrays.asList(1L, 21L));

                User userSaved = userService.saveUser(userNew);
                if (userSaved == null) {
                    throw new ResourceNotFoundException("No fue posible crear un nuevo usuario");
                }

                userData.put("sisucc", dtoSisucc);
                return userData;
            } else {
                userNew = user.get(0);
                userNew.setPassword(passwordEncoder.encode(request.getNuevaClave()));
                userNew.setResetPassword(false);

                userRepository.save(userNew);

                userData.put("sisucc", dtoSisucc);
                return userData;
            }

        } catch (ResourceNotFoundException ex) {
            throw new ResourceNotFoundException(ex.getMessage());
        } catch (ConnectException e) {
            throw new ConnectException("No se obtuvo respuesta del servidor");
        } catch (Exception exc) {
            throw new Exception("Error desconocido: " + exc.getMessage());
        }
    }


    public Map<String, Object> recuperarSisucc(AuthenticationRequest request) throws Exception {
        System.out.println("recuperarSisucc: " + this.sisId);

        try {
            APIClient api = new APIClient();
            Map<String, Object> userData = new HashMap<>();
            String resp = null;
            resp = api.olvidoClave(request.getUsuario(), "" + this.sisId);

            if (resp == null) {
                throw new ResourceNotFoundException("api - cambiar clave no retorno el resultado esperado");
            }

            DtoSisucc dtoSisucc = new Gson().fromJson(resp, DtoSisucc.class);
            if (dtoSisucc.getCodigo() != 42) {
                throw new ResourceNotFoundException(dtoSisucc.getMensaje() + " - CÓDIGO: " + dtoSisucc.getCodigo());
            }

            User user = userRepository.findByUsuario(request.getUsuario()).orElse(null);

            if (user == null) {
                throw new ResourceNotFoundException("No se encontro usuario para en sistema de facturacion");
            }

            user.setResetPassword(true);
            userRepository.save(user);

            userData.put("sisucc", dtoSisucc.getMensaje());
            return userData;
        } catch (ResourceNotFoundException ex) {
            throw new ResourceNotFoundException(ex.getMessage());
        } catch (ConnectException e) {
            throw new ConnectException("No se obtuvo respuesta del servidor");
        } catch (Exception exc) {
            throw new Exception("Error desconocido: " + exc.getMessage());
        }
    }


    public User loggedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.getPrincipal() instanceof User) {
            return (User) auth.getPrincipal();
        }
        return null;
    }

    private void saveUserToken(User user, String jwtToken) {
        var token = Token.builder().user(user).token(jwtToken).tokenType(TokenType.BEARER).expired(false).revoked(false).build();
        tokenRepository.save(token);
    }

    private void revokeAllUserTokens(User user) {
        var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getIdUser());
        if (validUserTokens.isEmpty()) return;
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }

    public Token logout(Token tokenReq) {
        String tokenReqs = tokenReq.getToken().substring(7);
        Token token = tokenRepository.findByToken(tokenReqs).orElse(null);

        if (token == null) {
            throw new ResourceNotFoundException("No se encontro el token");
        }

        token.setRevoked(true);
        token.setExpired(true);
        return tokenRepository.save(token);

    }
}
