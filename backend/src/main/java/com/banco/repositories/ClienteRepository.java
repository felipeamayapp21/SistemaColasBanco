package com.banco.repositories;

import com.banco.models.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    List<Cliente> findByEstadoOrderByPrioridadDescHoraIngresoAsc(String estado);
    Optional<Cliente> findByDocumentoAndEstado(String documento, String estado);
    boolean existsByDocumentoAndEstado(String documento, String estado);
    boolean existsByDocumento(String documento);
    Optional<Cliente> findByDocumento(String documento);

    @Query("SELECT c FROM Cliente c WHERE c.estado = 'ESPERA' AND c.prioridad = true ORDER BY c.horaIngreso ASC")
    List<Cliente> findPrioritariosEnEspera();

    @Query("SELECT c FROM Cliente c WHERE c.estado = 'ESPERA' AND c.prioridad = false ORDER BY c.horaIngreso ASC")
    List<Cliente> findRegularesEnEspera();

    @Query("SELECT MAX(CAST(SUBSTRING(c.ticket, 2) AS int)) FROM Cliente c WHERE c.ticket LIKE :prefijo")
    Integer maxNumeroTicket(@Param("prefijo") String prefijo);

    @Query("SELECT c FROM Cliente c WHERE c.estado IN ('ESPERA', 'EN_ATENCION') AND (LOWER(c.nombre) LIKE LOWER(CONCAT('%', :termino, '%')) OR c.documento LIKE CONCAT('%', :termino, '%'))")
    List<Cliente> buscarActivos(@Param("termino") String termino);

    @Query("SELECT c FROM Cliente c WHERE LOWER(c.nombre) LIKE LOWER(CONCAT('%', :termino, '%')) OR c.documento LIKE CONCAT('%', :termino, '%')")
    List<Cliente> buscarTodos(@Param("termino") String termino);
}
