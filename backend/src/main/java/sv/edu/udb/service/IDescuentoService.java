package sv.edu.udb.service;

import sv.edu.udb.dto.request.DescuentoRequest;
import sv.edu.udb.dto.response.DescuentoResponse;

import java.util.List;

public interface IDescuentoService {
    DescuentoResponse guardarDescuento(DescuentoRequest request);

    DescuentoResponse actualizarDescuento(Long id, DescuentoRequest request);

    List<DescuentoResponse> listarTodos();

    DescuentoResponse obtenerPorId(Long id);

    void eliminarDescuento(Long id);
}
