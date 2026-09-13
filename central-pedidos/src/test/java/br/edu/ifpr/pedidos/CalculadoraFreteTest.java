package br.edu.ifpr.pedidos;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CalculadoraFreteTest {
    private final CalculadoraFrete calculadora = new CalculadoraFrete();

    private Pedido pedido(String uf, boolean expresso, int peso, boolean fragil) {
        ItemPedido item = new ItemPedido("SKU", 1_000, 1, 1, peso, fragil);
        return new Pedido(List.of(item), uf, expresso, null);
    }

    @Test
    void deveRejeitarValorLiquidoNegativo() {
        assertThrows(IllegalArgumentException.class,
                () -> calculadora.calcular(pedido("PR", false, 1_000, false),
                        new Cliente(false, false, 1), -1));
    }

    @ParameterizedTest
    @CsvSource({
            "PR, 1200",
            "SP, 2000",
            "RJ, 2000",
            "SC, 3000"
    })
    void deveAplicarTarifaBasePorUf(String uf, long esperado) {
        long frete = calculadora.calcular(
                pedido(uf, false, 1_000, false),
                new Cliente(false, false, 1),
                10_000
        );
        assertEquals(esperado, frete);
    }

    @ParameterizedTest
    @CsvSource({
            "2000, 1200",
            "2001, 1500",
            "3000, 1500",
            "3001, 1800",
            "4500, 2100"
    })
    void deveCobrarTrezentosPorKgExcedenteOuFracao(int peso, long esperado) {
        long frete = calculadora.calcular(
                pedido("PR", false, peso, false),
                new Cliente(false, false, 1),
                10_000
        );
        assertEquals(esperado, frete);
    }

    @Test
    void deveZerarBaseEPesoComValorLiquidoDeTrezentosReaisEEntregaNormal() {
        long frete = calculadora.calcular(
                pedido("PR", false, 3_001, false),
                new Cliente(false, false, 1),
                30_000
        );
        assertEquals(0L, frete);
    }

    @Test
    void naoDeveDarFreteGratisUmCentavoAbaixoDoLimite() {
        long frete = calculadora.calcular(
                pedido("PR", false, 3_001, false),
                new Cliente(false, false, 1),
                29_999
        );
        assertEquals(1_800L, frete);
    }

    @Test
    void expressoNaoDeveReceberGratuidadeMesmoAcimaDoLimite() {
        long frete = calculadora.calcular(
                pedido("PR", true, 1_000, false),
                new Cliente(false, false, 1),
                30_000
        );
        assertEquals(2_700L, frete);
    }

    @Test
    void vipDevePagarMetadeDaBaseEPeso() {
        long frete = calculadora.calcular(
                pedido("PR", false, 3_001, false),
                new Cliente(true, false, 1),
                10_000
        );
        assertEquals(900L, frete);
    }

    @Test
    void expressoDeveAcrescentarQuinzeReais() {
        long frete = calculadora.calcular(
                pedido("PR", true, 1_000, false),
                new Cliente(false, false, 1),
                10_000
        );
        assertEquals(2_700L, frete);
    }

    @Test
    void fragilDeveAcrescentarCincoReais() {
        long frete = calculadora.calcular(
                pedido("PR", false, 1_000, true),
                new Cliente(false, false, 1),
                10_000
        );
        assertEquals(1_700L, frete);
    }

    @Test
    void adicionaisContinuamMesmoQuandoBaseFoiZerada() {
        long frete = calculadora.calcular(
                pedido("PR", false, 1_000, true),
                new Cliente(false, false, 1),
                30_000
        );
        assertEquals(500L, frete);
    }

    @Test
    void variosItensFrageisDevemAcrescentarCincoReaisUmaUnicaVez() {
        Pedido pedido = new Pedido(List.of(
                new ItemPedido("A", 1_000, 1, 1, 500, true),
                new ItemPedido("B", 1_000, 1, 1, 500, true)
        ), "PR", false, null);

        long frete = calculadora.calcular(pedido, new Cliente(false, false, 1), 10_000);
        assertEquals(1_700L, frete);
    }

    @Test
    void itemFragilInativoNaoDeveGerarAdicional() {
        Pedido pedido = new Pedido(List.of(
                new ItemPedido("INATIVO", 1_000, 0, 0, 500, true),
                new ItemPedido("ATIVO", 1_000, 1, 1, 500, false)
        ), "PR", false, null);

        long frete = calculadora.calcular(pedido, new Cliente(false, false, 1), 10_000);
        assertEquals(1_200L, frete);
    }

    @Test
    void deveCombinarVipExpressoEFragilNaOrdemDoContrato() {
        long frete = calculadora.calcular(
                pedido("SC", true, 1_000, true),
                new Cliente(true, false, 1),
                10_000
        );

        assertEquals(3_500L, frete);
    }
}
