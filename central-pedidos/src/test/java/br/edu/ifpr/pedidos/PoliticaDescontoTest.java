package br.edu.ifpr.pedidos;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class PoliticaDescontoTest {
    private final PoliticaDesconto politica = new PoliticaDesconto();

    @Test
    void deveRejeitarSubtotalNegativo() {
        assertThrows(IllegalArgumentException.class,
                () -> politica.calcular(new Cliente(false, false, 1), -1, null));
    }

    @Test
    void vipDeveReceberDezPorCento() {
        long desconto = politica.calcular(new Cliente(true, false, 1), 10_000, null);
        assertEquals(1_000L, desconto);
    }

    @Test
    void clienteComumDeveReceberCincoPorCentoAoAtingirQuinhentosReais() {
        long desconto = politica.calcular(new Cliente(false, false, 1), 50_000, null);
        assertEquals(2_500L, desconto);
    }

    @Test
    void clienteComumNaoDeveReceberDescontoBaseAbaixoDeQuinhentosReais() {
        long desconto = politica.calcular(new Cliente(false, false, 1), 49_999, null);
        assertEquals(0L, desconto);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   "})
    void cupomEmBrancoDeveManterDescontoBase(String cupom) {
        long desconto = politica.calcular(new Cliente(false, false, 1), 50_000, cupom);
        assertEquals(2_500L, desconto);
    }

    @Test
    void bemVindoDeveSomarVinteReaisParaPrimeiraCompraElegivel() {
        long desconto = politica.calcular(new Cliente(false, false, 0), 10_000, "BEMVINDO");
        assertEquals(2_000L, desconto);
    }

    @Test
    void bemVindoNaoDeveSomarDescontoAbaixoDeCemReais() {
        long desconto = politica.calcular(new Cliente(false, false, 0), 9_999, "BEMVINDO");
        assertEquals(0L, desconto);
    }

    @Test
    void bemVindoNaoDeveSomarDescontoParaClienteComCompraAnterior() {
        long desconto = politica.calcular(new Cliente(false, false, 1), 10_000, "BEMVINDO");
        assertEquals(0L, desconto);
    }

    @Test
    void extra10DeveSomarDezPorCentoAoAtingirDuzentosReais() {
        long desconto = politica.calcular(new Cliente(false, false, 1), 20_000, "EXTRA10");
        assertEquals(2_000L, desconto);
    }

    @Test
    void extra10NaoDeveSomarDescontoAbaixoDeDuzentosReais() {
        long desconto = politica.calcular(new Cliente(false, false, 1), 19_999, "EXTRA10");
        assertEquals(0L, desconto);
    }

    @Test
    void deveNormalizarEspacosEMaiusculasDoCupom() {
        long desconto = politica.calcular(new Cliente(false, false, 1), 20_000, "  extra10  ");
        assertEquals(2_000L, desconto);
    }

    @Test
    void deveRejeitarCupomDesconhecido() {
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> politica.calcular(new Cliente(false, false, 1), 20_000, "PROMO99"));

        assertEquals("Cupom desconhecido", erro.getMessage());
    }

    @Test
    void deveLimitarDescontoCombinadoAVintePorCento() {
        long desconto = politica.calcular(new Cliente(true, false, 0), 10_000, "BEMVINDO");
        assertEquals(2_000L, desconto);
    }

    @Test
    void descontoCombinadoAbaixoDoTetoDeveSerMantido() {
        long desconto = politica.calcular(new Cliente(false, false, 1), 50_000, "EXTRA10");
        assertEquals(7_500L, desconto);
    }

    @Test
    void percentualDeveTruncarCentavosParaBaixo() {
        long desconto = politica.calcular(new Cliente(true, false, 1), 10_001, null);
        assertEquals(1_000L, desconto);
    }
}
