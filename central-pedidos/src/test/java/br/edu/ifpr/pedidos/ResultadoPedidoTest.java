package br.edu.ifpr.pedidos;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResultadoPedidoTest {

    @Test
    void deveManterTodosOsValoresDoResultado() {
        ResultadoPedido resultado = new ResultadoPedido("PAGO", 10_000, 1_000, 500, 9_500);

        assertAll(
                () -> assertEquals("PAGO", resultado.status()),
                () -> assertEquals(10_000L, resultado.subtotalCentavos()),
                () -> assertEquals(1_000L, resultado.descontoCentavos()),
                () -> assertEquals(500L, resultado.freteCentavos()),
                () -> assertEquals(9_500L, resultado.totalCentavos())
        );
    }
}
