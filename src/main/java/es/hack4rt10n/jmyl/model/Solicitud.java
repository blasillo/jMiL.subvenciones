package es.hack4rt10n.jmyl.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicitudes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Solicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false)
    private Double montoSolicitado;

    @Column(nullable = false)
    private String categoria; // "Educación", "Vivienda", "Empresa", etc.

    @Column(nullable = false)
    private String estado; // "PENDIENTE", "APROBADA", "DENEGADA"

    @Column
    private String razonRechazo;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    @Column
    private LocalDateTime fechaResolucion;

    @Column
    private String nombreArchivo;

    @Column
    private String rutaArchivo;

    @Column
    private String tipoArchivo;

    @Column
    private Double ingresoMensual;

    @Column
    private String situacionLaboral;

    @Column
    private Integer miembrosFamilia;
}
