package br.edu.ifpr.pedidos;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClienteTest {

    @Test
    void deveCriarClienteComHistoricoValido() {
        Cliente cliente = new Cliente(true, false, 3);

        assertAll(
                () -> assertTrue(cliente.vip()),
                () -> assertFalse(cliente.bloqueado()),
                () -> assertEquals(3, cliente.comprasAnteriores())
        );
    }

    @Test
    void deveAceitarHistoricoZero() {
        Cliente cliente = new Cliente(false, false, 0);
        assertEquals(0, cliente.comprasAnteriores());
    }

    @Test
    void deveRejeitarHistoricoNegativo() {
        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> new Cliente(false, false, -1)
        );

        assertEquals("Histórico inválido", erro.getMessage());
    }
}
