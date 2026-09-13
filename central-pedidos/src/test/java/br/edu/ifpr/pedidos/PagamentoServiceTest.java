package br.edu.ifpr.pedidos;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class PagamentoServiceTest {

    @Test
    void deveRejeitarProcessadorNulo() {
        assertThrows(NullPointerException.class, () -> new PagamentoService(null));
    }

    @Test
    void aprovacaoDeveRetornarTrueComUmaUnicaChamada() {
        List<Long> valores = new ArrayList<>();
        PagamentoService service = new PagamentoService(total -> {
            valores.add(total);
            return true;
        });

        assertTrue(service.pagar(12_345, 3));
        assertEquals(List.of(12_345L), valores);
    }

    @Test
    void recusaDefinitivaDeveRetornarFalseSemRepetir() {
        AtomicInteger chamadas = new AtomicInteger();
        PagamentoService service = new PagamentoService(total -> {
            chamadas.incrementAndGet();
            return false;
        });

        assertFalse(service.pagar(10_000, 3));
        assertEquals(1, chamadas.get());
    }

    @Test
    void indisponibilidadeTemporariaDevePermitirNovaTentativa() {
        AtomicInteger chamadas = new AtomicInteger();
        List<Long> valores = new ArrayList<>();
        PagamentoService service = new PagamentoService(total -> {
            valores.add(total);
            if (chamadas.incrementAndGet() == 1) {
                throw new IllegalStateException("temporariamente indisponível");
            }
            return true;
        });

        assertTrue(service.pagar(25_000, 3));
        assertAll(
                () -> assertEquals(2, chamadas.get()),
                () -> assertEquals(List.of(25_000L, 25_000L), valores)
        );
    }

    @Test
    void deveRetornarFalseAoEsgotarTresTentativas() {
        AtomicInteger chamadas = new AtomicInteger();
        PagamentoService service = new PagamentoService(total -> {
            chamadas.incrementAndGet();
            throw new IllegalStateException("fora do ar");
        });

        assertFalse(service.pagar(10_000, 3));
        assertEquals(3, chamadas.get());
    }

    @Test
    void limiteDeUmaTentativaDeveSerRespeitado() {
        AtomicInteger chamadas = new AtomicInteger();
        PagamentoService service = new PagamentoService(total -> {
            chamadas.incrementAndGet();
            throw new IllegalStateException("fora do ar");
        });

        assertFalse(service.pagar(10_000, 1));
        assertEquals(1, chamadas.get());
    }

    @Test
    void excecaoDiferenteDeIllegalStateExceptionDevePropagar() {
        AtomicInteger chamadas = new AtomicInteger();
        PagamentoService service = new PagamentoService(total -> {
            chamadas.incrementAndGet();
            throw new IllegalArgumentException("falha definitiva");
        });

        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> service.pagar(10_000, 3)
        );

        assertAll(
                () -> assertEquals("falha definitiva", erro.getMessage()),
                () -> assertEquals(1, chamadas.get())
        );
    }

    @Test
    void deveRejeitarTotalZero() {
        PagamentoService service = new PagamentoService(total -> true);
        assertThrows(IllegalArgumentException.class, () -> service.pagar(0, 1));
    }

    @Test
    void deveRejeitarTotalNegativo() {
        PagamentoService service = new PagamentoService(total -> true);
        assertThrows(IllegalArgumentException.class, () -> service.pagar(-1, 1));
    }

    @Test
    void deveRejeitarLimiteAbaixoDeUm() {
        PagamentoService service = new PagamentoService(total -> true);
        assertThrows(IllegalArgumentException.class, () -> service.pagar(10_000, 0));
    }

    @Test
    void deveRejeitarLimiteAcimaDeTres() {
        PagamentoService service = new PagamentoService(total -> true);
        assertThrows(IllegalArgumentException.class, () -> service.pagar(10_000, 4));
    }
}
