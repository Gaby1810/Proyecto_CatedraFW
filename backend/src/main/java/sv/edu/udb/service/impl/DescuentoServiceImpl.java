package sv.edu.udb.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sv.edu.udb.dto.request.DescuentoRequest;
import sv.edu.udb.dto.response.DescuentoResponse;
import sv.edu.udb.entity.Descuento;
import sv.edu.udb.exception.ConflictException;
import sv.edu.udb.exception.ResourceNotFoundException;
import sv.edu.udb.repository.DescuentoRepository;
import sv.edu.udb.service.IDescuentoService;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DescuentoServiceImpl implements IDescuentoService {

    @Autowired
    private DescuentoRepository descuentoRepository;

    @Override
    public DescuentoResponse guardarDescuento(DescuentoRequest request) {
        if (descuentoRepository.existsByTipoIgnoreCase(request.getTipo())) {
            throw new ConflictException("Ya existe un descuento registrado con el tipo: " + request.getTipo());
        }

        Descuento descuento = new Descuento();
        mapToEntity(descuento, request);
        return mapToResponse(descuentoRepository.save(descuento));
    }

    @Override
    public DescuentoResponse actualizarDescuento(Long id, DescuentoRequest request) {
        Descuento descuento = descuentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Descuento no encontrado con ID: " + id));

        descuentoRepository.findByTipoIgnoreCase(request.getTipo())
                .filter(existente -> !existente.getId_descuento().equals(id))
                .ifPresent(existente -> {
                    throw new ConflictException("Ya existe otro descuento con el tipo: " + request.getTipo());
                });

        mapToEntity(descuento, request);
        return mapToResponse(descuentoRepository.save(descuento));
    }

    @Override
    public List<DescuentoResponse> listarTodos() {
        return descuentoRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public DescuentoResponse obtenerPorId(Long id) {
        Descuento descuento = descuentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Descuento no encontrado con ID: " + id));
        return mapToResponse(descuento);
    }

    @Override
    public void eliminarDescuento(Long id) {
        if (!descuentoRepository.existsById(id)) {
            throw new ResourceNotFoundException("No se encontró el descuento con ID: " + id);
        }
        descuentoRepository.deleteById(id);
    }

    private void mapToEntity(Descuento descuento, DescuentoRequest request) {
        descuento.setTipo(request.getTipo().trim().toUpperCase());
        descuento.setPorcentaje(request.getPorcentaje());
        descuento.setVigencia(request.getVigencia());
    }

    private DescuentoResponse mapToResponse(Descuento descuento) {
        DescuentoResponse response = new DescuentoResponse();
        response.setId_descuento(descuento.getId_descuento());
        response.setTipo(descuento.getTipo());
        response.setPorcentaje(descuento.getPorcentaje());
        response.setVigencia(descuento.getVigencia());
        return response;
    }
}
