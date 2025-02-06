package sv.gov.cnr.cnrpos.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import sv.gov.cnr.cnrpos.entities.Producto;
import sv.gov.cnr.cnrpos.services.ProductoService;

import java.util.List;

@RestController
@RequestMapping("/productos")

class ProductoController {

    @Autowired
    private ProductoService productoService;

    @GetMapping
    public List<Producto> listarProductos() {
        return productoService.obtenerTodosLosProductos();
    }

    @PostMapping
    public Producto guardarProducto(@RequestBody Producto producto) {
        return productoService.guardarProducto(producto);
    }


}
