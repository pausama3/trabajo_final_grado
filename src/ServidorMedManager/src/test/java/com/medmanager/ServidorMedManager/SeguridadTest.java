package com.medmanager.ServidorMedManager;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Pruebas unitarias del uso de BCrypt para las contraseñas (RNF1).
 * Es la misma comprobación que hace TrabajadorDAO.autenticar() en el login.
 */
class SeguridadTest {

    @Test
    @DisplayName("RNF1: el hash no guarda la contraseña en claro")
    void hashNoContieneLaContrasena() {
        String hash = BCrypt.hashpw("Enfer123!", BCrypt.gensalt());
        assertNotEquals("Enfer123!", hash);
        assertFalse(hash.contains("Enfer123!"));
        assertTrue(hash.startsWith("$2a$"));
    }

    @Test
    @DisplayName("La contraseña correcta se acepta y una incorrecta se rechaza")
    void compruebaContrasena() {
        String hash = BCrypt.hashpw("Enfer123!", BCrypt.gensalt());
        assertTrue(BCrypt.checkpw("Enfer123!", hash));
        assertFalse(BCrypt.checkpw("enfer123!", hash));   // distingue mayúsculas
        assertFalse(BCrypt.checkpw("otraClave", hash));
    }

    @Test
    @DisplayName("La misma contraseña da un hash distinto cada vez (sal aleatoria)")
    void salAleatoria() {
        String hash1 = BCrypt.hashpw("Medico123!", BCrypt.gensalt());
        String hash2 = BCrypt.hashpw("Medico123!", BCrypt.gensalt());
        assertNotEquals(hash1, hash2);
        assertTrue(BCrypt.checkpw("Medico123!", hash1));
        assertTrue(BCrypt.checkpw("Medico123!", hash2));
    }
}
