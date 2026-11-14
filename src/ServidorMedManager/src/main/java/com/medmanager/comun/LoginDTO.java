package com.medmanager.comun;

import java.io.Serializable;

public class LoginDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String usuario;
    private final String password;

    public LoginDTO(String usuario, String password) {
        this.usuario = usuario;
        this.password = password;
    }

    public String getUsuario() { return usuario; }
    public String getPassword() { return password; }
}