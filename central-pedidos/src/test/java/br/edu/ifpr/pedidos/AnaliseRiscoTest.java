package br.edu.ifpr.pedidos;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AnaliseRiscoTest {
    private final AnaliseRisco analise = new AnaliseRisco();

    @Test
    void deveRejeitarTotalNegativo() {
        assertThrows(IllegalArgumentException.class,
                () -> analise.avaliar(new Cliente(false, false, 1), -1, false));
    }

    @Test
    void clienteBloqueadoDeveSerRecusadoAntesDasDemaisRegras() {
        String resultado = analise.avaliar(new Cliente(false, true, 0), 999_999, true);
        assertEquals("RECUSADO", resultado);
    }

    @Test
    void clienteNovoDeveSerAprovadoNoLimiteDeMilReaisComEntregaNormal() {
        String resultado = analise.avaliar(new Cliente(false, false, 0), 100_000, false);
        assertEquals("APROVADO", resultado);
    }

    @Test
    void clienteNovoDeveIrParaRevisaoUmCentavoAcimaDeMilReais() {
        String resultado = analise.avaliar(new Cliente(false, false, 0), 100_001, false);
        assertEquals("REVISAO", resultado);
    }

    @Test
    void clienteNovoDeveIrParaRevisaoQuandoEntregaForExpressa() {
        String resultado = analise.avaliar(new Cliente(false, false, 0), 100_000, true);
        assertEquals("REVISAO", resultado);
    }

    @Test
    void clienteAntigoComumDeveSerAprovadoNoLimiteDeCincoMilReais() {
        String resultado = analise.avaliar(new Cliente(false, false, 1), 500_000, false);
        assertEquals("APROVADO", resultado);
    }

    @Test
    void clienteAntigoComumDeveIrParaRevisaoAcimaDeCincoMilReais() {
        String resultado = analise.avaliar(new Cliente(false, false, 1), 500_001, false);
        assertEquals("REVISAO", resultado);
    }

    @Test
    void clienteAntigoVipDeveSerAprovadoMesmoAcimaDeCincoMilReais() {
        String resultado = analise.avaliar(new Cliente(true, false, 1), 500_001, false);
        assertEquals("APROVADO", resultado);
    }
}
