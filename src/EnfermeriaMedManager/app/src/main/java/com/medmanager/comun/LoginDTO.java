package com.medmanager.comun;

import java.io.Serializable;

/**
 * Credenciales que el cliente envía al servidor con la acción {@link Acciones#LOGIN}.
 * <p>
 * Es inmutable: usuario y contraseña se fijan en el constructor.
 */
public class LoginDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String usuario;
    private final String password;   // en texto plano; el servidor la compara con su hash

    /**
     * @param usuario  nombre de usuario introducido
     * @param password contraseña introducida
     */
    public LoginDTO(String usuario, String password) {
        this.usuario = usuario;
        this.password = password;
    }

    // Getters
    public String getUsuario() { return usuario; }
    public String getPassword() { return password; }
}