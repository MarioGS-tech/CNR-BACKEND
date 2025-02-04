package sv.gov.cnr.cnrpos.controllers.security;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sv.gov.cnr.cnrpos.models.security.Token;
import sv.gov.cnr.cnrpos.security.AuthenticationRequest;
import sv.gov.cnr.cnrpos.security.AuthenticationService;
import sv.gov.cnr.cnrpos.security.RegisterRequest;
import sv.gov.cnr.cnrpos.utils.Utils;

import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final Utils utils;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            return utils.jsonResponse(200, "bienvenido", authenticationService.register(request));
        } catch (Exception ex) {
            return utils.jsonResponse(500, "No se pudo registrar al usuario", ex.getMessage());

        }
    }

    @PostMapping("/authenticate")
    public ResponseEntity<Object> login(@RequestBody AuthenticationRequest request) {
        try {
            return utils.jsonResponse(200, "bienvenido", authenticationService.authenticated(request));
        } catch (Exception ex) {
            return utils.jsonResponse(401, "Usuario y/o Contraseña incorrectos", ex.getMessage());

        }
    }

    @PostMapping("/authenticate-sisucc")
    public ResponseEntity<Object> loginSisucc(@RequestBody AuthenticationRequest request) {
        try {
            Map<String, Object> userData = authenticationService.authenticatedSisucc(request);
            return utils.jsonResponse(userData.get("codigo").equals(1) ? 200 : 500, userData.get("codigo").equals(1) ? "bienvenido" : "No fue posible autentificarse", userData);
        } catch (Exception ex) {
            return utils.jsonResponse(401, "No fue posible autentificarse", ex.getMessage());

        }
    }

    @PostMapping("/cambiar-sisucc")
    public ResponseEntity<Object> cambiarSisucc(@RequestBody AuthenticationRequest request) {
        try {

            return utils.jsonResponse(200, "Contraseña ha sido cambiada", authenticationService.cambiarSisucc(request));
        } catch (Exception ex) {
            return utils.jsonResponse(500, "No se ha cambiado la contraseña", ex.getMessage());

        }
    }

    @PostMapping("/recuperar-sisucc")
    public ResponseEntity<Object> recuperarSisucc(@RequestBody AuthenticationRequest request) {
        try {

            return utils.jsonResponse(200, "Contraseña ha sido recuperada", authenticationService.recuperarSisucc(request));
        } catch (Exception ex) {
            return utils.jsonResponse(500, "No se ha recuperado la contraseña", ex.getMessage());

        }
    }


    @PostMapping("/logout")
    public ResponseEntity<Object> logout(@RequestBody Token token) {
        try {
            authenticationService.logout(token);
            return utils.jsonResponse(200, "logout", null);
        } catch (Exception ex) {
            return utils.jsonResponse(500, "No se encontro el token", ex.getMessage());
        }
    }
}
