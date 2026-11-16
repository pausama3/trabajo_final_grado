package com.medmanager.escritorio.vista;

import com.medmanager.comun.TrabajadorDTO;

/** Pantalla principal de recepción: agenda de la clínica, gestión de pacientes y alta de citas. */
public class DashboardRecepcion extends DashboardBase {
    private static final long serialVersionUID = 1L;

    public DashboardRecepcion(TrabajadorDTO usuario) {
        super(usuario, "Recepción");

        PanelBase agenda = new PanelBase("Agenda de la clínica", "Todas las citas, por día y por médico",
                new String[] {"Hora", "Paciente", "Médico", "Motivo", "Estado"}, false);
        // DATOS DE EJEMPLO: se borrarán cuando conectemos el servidor
        agenda.getModelo().addRow(new Object[] {"09:00", "Ana Pérez López", "Carlos Martín", "Revisión anual", "PENDIENTE"});
        agenda.getModelo().addRow(new Object[] {"09:15", "Pedro Sanz Molina", "Carlos Martín", "Dolor de espalda", "PENDIENTE"});
        agenda.getModelo().addRow(new Object[] {"09:15", "Lucía Marín Ortiz", "Marta Vidal", "Vacunación", "PENDIENTE"});
        anadirPantalla("Agenda", agenda);

        anadirPantalla("Pacientes", new PanelBase("Pacientes", "Busca, da de alta, modifica o da de baja",
                new String[] {"DNI", "Nombre", "Teléfono", "Email", "Estado"}, true));
        anadirPantalla("Registrar cita", new PanelBase("Registrar cita", "Aquí irá el formulario para programar una cita",
                new String[] {}, false));
    }
}