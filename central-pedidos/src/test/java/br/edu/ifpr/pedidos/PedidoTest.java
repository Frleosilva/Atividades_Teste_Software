package br.edu.ifpr.pedidos;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PedidoTest {

    private ItemPedido item(String sku, long preco, int quantidade, int estoque, int peso, boolean fragil) {
        return new ItemPedido(sku, preco, quantidade, estoque, peso, fragil);
    }

    @Test
    void deveRejeitarListaNula() {
        assertThrows(IllegalArgumentException.class,
                () -> new Pedido(null, "PR", false, null));
    }

    @Test
    void deveRejeitarMaisDeCemLinhas() {
        ItemPedido item = item("SKU", 100, 1, 1, 100, false);
        List<ItemPedido> itens = new ArrayList<>(Collections.nCopies(101, item));

        assertThrows(IllegalArgumentException.class,
                () -> new Pedido(itens, "PR", false, null));
    }

    @Test
    void deveRejeitarElementoNuloNaLista() {
        List<ItemPedido> itens = new ArrayList<>();
        itens.add(null);

        assertThrows(NullPointerException.class,
                () -> new Pedido(itens, "PR", false, null));
    }

    @Test
    void deveRejeitarUfNula() {
        assertThrows(IllegalArgumentException.class,
                () -> new Pedido(List.of(), null, false, null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "P", "PRR", "pr", "P1", "ÇR"})
    void deveRejeitarUfForaDoFormato(String uf) {
        assertThrows(IllegalArgumentException.class,
                () -> new Pedido(List.of(), uf, false, null));
    }

    @Test
    void deveAceitarQualquerUfComDuasLetrasAsciiMaiusculas() {
        Pedido pedido = assertDoesNotThrow(() -> new Pedido(List.of(), "ZZ", false, null));
        assertEquals("ZZ", pedido.uf());
    }

    @Test
    void deveCopiarListaDefensivamente() {
        List<ItemPedido> origem = new ArrayList<>();
        origem.add(item("A", 100, 1, 1, 100, false));
        Pedido pedido = new Pedido(origem, "PR", false, null);

        origem.add(item("B", 100, 1, 1, 100, false));

        assertAll(
                () -> assertEquals(1, pedido.itens().size()),
                () -> assertThrows(UnsupportedOperationException.class,
                        () -> pedido.itens().add(item("C", 100, 1, 1, 100, false)))
        );
    }

    @Test
    void deveCalcularSubtotalIgnorandoLinhaInativa() {
        Pedido pedido = new Pedido(List.of(
                item("A", 1_000, 2, 5, 100, false),
                item("INATIVO", 9_999, 0, 0, 10_000, true),
                item("B", 250, 3, 3, 200, false)
        ), "PR", false, null);

        assertEquals(2_750L, pedido.subtotalCentavos());
    }

    @Test
    void subtotalDeListaVaziaDeveSerZero() {
        assertEquals(0L, new Pedido(List.of(), "PR", false, null).subtotalCentavos());
    }

    @Test
    void deveCalcularPesoConsiderandoQuantidadeEIgnorandoQuantidadeZero() {
        Pedido pedido = new Pedido(List.of(
                item("A", 100, 2, 2, 100, false),
                item("INATIVO", 100, 0, 0, 50_000, false),
                item("B", 100, 3, 3, 500, false)
        ), "PR", false, null);

        assertEquals(1_700, pedido.pesoGramas());
    }

    @Test
    void pesoDeListaVaziaDeveSerZero() {
        assertEquals(0, new Pedido(List.of(), "PR", false, null).pesoGramas());
    }

    @Test
    void itemFragilInativoNaoDeveMarcarPedidoComoFragil() {
        Pedido pedido = new Pedido(List.of(
                item("INATIVO", 100, 0, 0, 100, true),
                item("NORMAL", 100, 1, 1, 100, false)
        ), "PR", false, null);

        assertFalse(pedido.temFragil());
    }

    @Test
    void itemAtivoNaoFragilNaoDeveMarcarPedidoComoFragil() {
        Pedido pedido = new Pedido(List.of(item("NORMAL", 100, 1, 1, 100, false)), "PR", false, null);
        assertFalse(pedido.temFragil());
    }

    @Test
    void itemAtivoFragilDeveMarcarPedidoComoFragil() {
        Pedido pedido = new Pedido(List.of(
                item("NORMAL", 100, 1, 1, 100, false),
                item("FRAGIL", 100, 1, 1, 100, true)
        ), "PR", false, null);

        assertTrue(pedido.temFragil());
    }

    @Test
    void pedidoVazioNaoDeveSerFragil() {
        assertFalse(new Pedido(List.of(), "PR", false, null).temFragil());
    }

    @Test
    void estoqueDeListaVaziaDeveSerSuficiente() {
        assertTrue(new Pedido(List.of(), "PR", false, null).estoqueSuficiente());
    }

    @Test
    void deveRetornarTrueQuandoTodasAsLinhasTemEstoque() {
        Pedido pedido = new Pedido(List.of(
                item("A", 100, 1, 1, 100, false),
                item("B", 100, 2, 3, 100, false)
        ), "PR", false, null);

        assertTrue(pedido.estoqueSuficiente());
    }

    @Test
    void devePararNaPrimeiraLinhaSemEstoque() {
        Pedido pedido = new Pedido(List.of(
                item("A", 100, 2, 1, 100, false),
                item("B", 100, 1, 1, 100, false)
        ), "PR", false, null);

        assertFalse(pedido.estoqueSuficiente());
    }

    @Test
    void deveDetectarFaltaDeEstoqueNaUltimaLinha() {
        Pedido pedido = new Pedido(List.of(
                item("A", 100, 1, 1, 100, false),
                item("A", 100, 3, 2, 100, false)
        ), "PR", false, null);

        assertFalse(pedido.estoqueSuficiente());
    }
}
