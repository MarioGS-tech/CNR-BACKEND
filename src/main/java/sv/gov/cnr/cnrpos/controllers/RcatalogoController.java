package sv.gov.cnr.cnrpos.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sv.gov.cnr.cnrpos.entities.RCatalogos;
import sv.gov.cnr.cnrpos.models.security.Rol;
import sv.gov.cnr.cnrpos.services.RcatalogoService;
import sv.gov.cnr.cnrpos.utils.Utils;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping(path = "/rcatalogos")
public class RcatalogoController {

    private final RcatalogoService rcatalogoService;
    private Utils utils;

    @Autowired
    public RcatalogoController(RcatalogoService rcatalogoService) {
        this.rcatalogoService = rcatalogoService;
    }

    @GetMapping(path = "/list")
    public Object rcatalogoList() throws JsonProcessingException {
        return utils.jsonResponse(200, "listado de Catálogos", rcatalogoService.getAllRcatalogos());
    }

    @GetMapping(path = "/municipios")
    public Object rcatalogoMunicipiosList() throws JsonProcessingException {
        return utils.jsonResponse(200, "listado de Municipios", rcatalogoService.getRCatalogosByGrupo("MUNICIPIOS"));
    }

    @GetMapping(path = "/giro")
    public Object rcatalogoGiroList() throws JsonProcessingException {
        return utils.jsonResponse(200, "listado de Actividad Económica", rcatalogoService.getRCatalogosByGrupo("ACTIVIDAD_ECONOMICA"));
    }

    @GetMapping("/{id}")
    public Object getValue(@PathVariable Long id) {

        String rCatalogos = rcatalogoService.getRcatalogosValue(id);
        if (rCatalogos == null) {
            return ResponseEntity.notFound().build();
        }
        return utils.jsonResponse(200, "Valor del Catálogo", rCatalogos);
    }




}
