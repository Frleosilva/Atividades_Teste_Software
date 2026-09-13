package br.edu.ifpr.pedidos;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class ItemPedidoTest {

    @Test
    void deveCalcularTotalEInformarDisponibilidade() {
        ItemPedido item = new ItemPedido("SKU-1", 1_234, 3, 3, 500, true);

        assertAll(
                () -> assertEquals(3_702L, item.totalCentavos()),
                () -> assertTrue(item.disponivel()),
                () -> assertEquals("SKU-1", item.sku()),
                () -> assertTrue(item.fragil())
        );
    }

    @Test
    void deveInformarIndisponibilidadeQuandoQuantidadeSuperaEstoque() {
        ItemPedido item = new ItemPedido("SKU-2", 500, 4, 3, 100, false);
        assertFalse(item.disponivel());
    }

    @Test
    void deveRejeitarSkuNulo() {
        assertThrows(IllegalArgumentException.class,
                () -> new ItemPedido(null, 100, 1, 1, 100, false));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   "})
    void deveRejeitarSkuEmBranco(String sku) {
        assertThrows(IllegalArgumentException.class,
                () -> new ItemPedido(sku, 100, 1, 1, 100, false));
    }

    @ParameterizedTest
    @ValueSource(longs = {-1, 0, 1_000_001})
    void deveRejeitarPrecoForaDoDominio(long preco) {
        assertThrows(IllegalArgumentException.class,
                () -> new ItemPedido("SKU", preco, 1, 1, 100, false));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 101})
    void deveRejeitarQuantidadeForaDoDominio(int quantidade) {
        assertThrows(IllegalArgumentException.class,
                () -> new ItemPedido("SKU", 100, quantidade, 1, 100, false));
    }

    @Test
    void deveRejeitarEstoqueNegativo() {
        assertThrows(IllegalArgumentException.class,
                () -> new ItemPedido("SKU", 100, 1, -1, 100, false));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 100_001})
    void deveRejeitarPesoForaDoDominio(int peso) {
        assertThrows(IllegalArgumentException.class,
                () -> new ItemPedido("SKU", 100, 1, 1, peso, false));
    }

    @Test
    void deveAceitarLimitesInferioresValidos() {
        ItemPedido item = assertDoesNotThrow(
                () -> new ItemPedido("MIN", 1, 0, 0, 1, false)
        );
        assertEquals(0L, item.totalCentavos());
    }

    @Test
    void deveAceitarLimitesSuperioresValidos() {
        ItemPedido item = assertDoesNotThrow(
                () -> new ItemPedido("MAX", 1_000_000, 100, 100, 100_000, false)
        );
        assertAll(
                () -> assertEquals(100_000_000L, item.totalCentavos()),
                () -> assertTrue(item.disponivel())
        );
    }
}
