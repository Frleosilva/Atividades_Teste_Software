package br.edu.ifpr.pedidos;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class PedidoServiceTest {

    private ItemPedido item(String sku, long preco, int quantidade, int estoque, int peso, boolean fragil) {
        return new ItemPedido(sku, preco, quantidade, estoque, peso, fragil);
    }

    @Test
    void deveRejeitarProcessadorNulo() {
        assertThrows(NullPointerException.class, () -> new PedidoService(null));
    }

    @Test
    void deveRejeitarPedidoNulo() {
        PedidoService service = new PedidoService(total -> true);
        assertThrows(NullPointerException.class,
                () -> service.fechar(null, new Cliente(false, false, 1)));
    }

    @Test
    void deveRejeitarClienteNulo() {
        PedidoService service = new PedidoService(total -> true);
        Pedido pedido = new Pedido(List.of(item("A", 100, 1, 1, 100, false)), "PR", false, null);

        assertThrows(NullPointerException.class, () -> service.fechar(pedido, null));
    }

    @Test
    void deveFecharPedidoDeClienteComumComFreteDoParanaEPagamentoAprovado() {
        Cliente cliente = new Cliente(false, false, 1);
        ItemPedido item = item("LIVRO-JAVA", 10_000, 1, 5, 1_000, false);
        Pedido pedido = new Pedido(List.of(item), "PR", false, null);

        List<Long> cobrancas = new ArrayList<>();
        PedidoService service = new PedidoService(total -> {
            cobrancas.add(total);
            return true;
        });

        ResultadoPedido resultado = service.fechar(pedido, cliente);

        assertAll(
                () -> assertEquals("PAGO", resultado.status()),
                () -> assertEquals(10_000L, resultado.subtotalCentavos()),
                () -> assertEquals(0L, resultado.descontoCentavos()),
                () -> assertEquals(1_200L, resultado.freteCentavos()),
                () -> assertEquals(11_200L, resultado.totalCentavos()),
                () -> assertEquals(List.of(11_200L), cobrancas)
        );
    }

    @Test
    void bloqueadoDeveRetornarAntesDeSubtotalCupomEPagamento() {
        AtomicInteger chamadas = new AtomicInteger();
        PedidoService service = new PedidoService(total -> {
            chamadas.incrementAndGet();
            return true;
        });
        Pedido pedidoVazioComCupomDesconhecido = new Pedido(List.of(), "PR", false, "INVALIDO");

        ResultadoPedido resultado = service.fechar(
                pedidoVazioComCupomDesconhecido,
                new Cliente(false, true, 0)
        );

        assertAll(
                () -> assertEquals("BLOQUEADO", resultado.status()),
                () -> assertEquals(0L, resultado.subtotalCentavos()),
                () -> assertEquals(0L, resultado.descontoCentavos()),
                () -> assertEquals(0L, resultado.freteCentavos()),
                () -> assertEquals(0L, resultado.totalCentavos()),
                () -> assertEquals(0, chamadas.get())
        );
    }

    @Test
    void subtotalZeroDeveLancarExcecaoSemCobrar() {
        AtomicInteger chamadas = new AtomicInteger();
        PedidoService service = new PedidoService(total -> {
            chamadas.incrementAndGet();
            return true;
        });
        Pedido pedido = new Pedido(List.of(
                item("INATIVO", 1_000, 0, 0, 100, false)
        ), "PR", false, null);

        assertThrows(IllegalArgumentException.class,
                () -> service.fechar(pedido, new Cliente(false, false, 1)));
        assertEquals(0, chamadas.get());
    }

    @Test
    void faltaDeEstoqueDeveRetornarAntesDeValidarCupomEPagamento() {
        AtomicInteger chamadas = new AtomicInteger();
        PedidoService service = new PedidoService(total -> {
            chamadas.incrementAndGet();
            return true;
        });
        Pedido pedido = new Pedido(List.of(
                item("SEM-ESTOQUE", 10_000, 2, 1, 100, false)
        ), "PR", false, "CUPOM-INVALIDO");

        ResultadoPedido resultado = service.fechar(pedido, new Cliente(false, false, 1));

        assertAll(
                () -> assertEquals("SEM_ESTOQUE", resultado.status()),
                () -> assertEquals(0L, resultado.subtotalCentavos()),
                () -> assertEquals(0L, resultado.descontoCentavos()),
                () -> assertEquals(0L, resultado.freteCentavos()),
                () -> assertEquals(0L, resultado.totalCentavos()),
                () -> assertEquals(0, chamadas.get())
        );
    }

    @Test
    void clienteNovoComEntregaExpressaDeveIrParaRevisaoSemCobrar() {
        AtomicInteger chamadas = new AtomicInteger();
        PedidoService service = new PedidoService(total -> {
            chamadas.incrementAndGet();
            return true;
        });
        Pedido pedido = new Pedido(List.of(
                item("A", 10_000, 1, 1, 1_000, false)
        ), "PR", true, null);

        ResultadoPedido resultado = service.fechar(pedido, new Cliente(false, false, 0));

        assertAll(
                () -> assertEquals("REVISAO", resultado.status()),
                () -> assertEquals(10_000L, resultado.subtotalCentavos()),
                () -> assertEquals(0L, resultado.descontoCentavos()),
                () -> assertEquals(2_700L, resultado.freteCentavos()),
                () -> assertEquals(12_700L, resultado.totalCentavos()),
                () -> assertEquals(0, chamadas.get())
        );
    }

    @Test
    void clienteNovoComTotalAcimaDeMilReaisDeveIrParaRevisaoSemCobrar() {
        AtomicInteger chamadas = new AtomicInteger();
        PedidoService service = new PedidoService(total -> {
            chamadas.incrementAndGet();
            return true;
        });
        Pedido pedido = new Pedido(List.of(
                item("ALTO", 120_000, 1, 1, 1_000, false)
        ), "PR", false, null);

        ResultadoPedido resultado = service.fechar(pedido, new Cliente(false, false, 0));

        assertAll(
                () -> assertEquals("REVISAO", resultado.status()),
                () -> assertEquals(120_000L, resultado.subtotalCentavos()),
                () -> assertEquals(6_000L, resultado.descontoCentavos()),
                () -> assertEquals(0L, resultado.freteCentavos()),
                () -> assertEquals(114_000L, resultado.totalCentavos()),
                () -> assertEquals(0, chamadas.get())
        );
    }

    @Test
    void clienteAntigoComumComTotalAltoDeveIrParaRevisao() {
        AtomicInteger chamadas = new AtomicInteger();
        PedidoService service = new PedidoService(total -> {
            chamadas.incrementAndGet();
            return true;
        });
        Pedido pedido = new Pedido(List.of(
                item("CARO", 600_000, 1, 1, 1_000, false)
        ), "PR", false, null);

        ResultadoPedido resultado = service.fechar(pedido, new Cliente(false, false, 1));

        assertAll(
                () -> assertEquals("REVISAO", resultado.status()),
                () -> assertEquals(600_000L, resultado.subtotalCentavos()),
                () -> assertEquals(30_000L, resultado.descontoCentavos()),
                () -> assertEquals(0L, resultado.freteCentavos()),
                () -> assertEquals(570_000L, resultado.totalCentavos()),
                () -> assertEquals(0, chamadas.get())
        );
    }

    @Test
    void clienteAntigoVipComTotalAltoPodeSerAprovadoECobrado() {
        List<Long> cobrancas = new ArrayList<>();
        PedidoService service = new PedidoService(total -> {
            cobrancas.add(total);
            return true;
        });
        Pedido pedido = new Pedido(List.of(
                item("CARO", 600_000, 1, 1, 1_000, false)
        ), "PR", false, null);

        ResultadoPedido resultado = service.fechar(pedido, new Cliente(true, false, 1));

        assertAll(
                () -> assertEquals("PAGO", resultado.status()),
                () -> assertEquals(600_000L, resultado.subtotalCentavos()),
                () -> assertEquals(60_000L, resultado.descontoCentavos()),
                () -> assertEquals(0L, resultado.freteCentavos()),
                () -> assertEquals(540_000L, resultado.totalCentavos()),
                () -> assertEquals(List.of(540_000L), cobrancas)
        );
    }

    @Test
    void pagamentoRecusadoDeveManterValoresCalculados() {
        List<Long> cobrancas = new ArrayList<>();
        PedidoService service = new PedidoService(total -> {
            cobrancas.add(total);
            return false;
        });
        Pedido pedido = new Pedido(List.of(
                item("A", 10_000, 1, 1, 1_000, false)
        ), "PR", false, null);

        ResultadoPedido resultado = service.fechar(pedido, new Cliente(false, false, 1));

        assertAll(
                () -> assertEquals("PAGAMENTO_RECUSADO", resultado.status()),
                () -> assertEquals(10_000L, resultado.subtotalCentavos()),
                () -> assertEquals(0L, resultado.descontoCentavos()),
                () -> assertEquals(1_200L, resultado.freteCentavos()),
                () -> assertEquals(11_200L, resultado.totalCentavos()),
                () -> assertEquals(List.of(11_200L), cobrancas)
        );
    }

    @Test
    void deveTentarPagamentoAteTresVezesQuandoProcessadorEstaTemporariamenteIndisponivel() {
        AtomicInteger chamadas = new AtomicInteger();
        List<Long> valores = new ArrayList<>();
        PedidoService service = new PedidoService(total -> {
            valores.add(total);
            int atual = chamadas.incrementAndGet();
            if (atual < 3) throw new IllegalStateException("temporário");
            return true;
        });
        Pedido pedido = new Pedido(List.of(item("A", 10_000, 1, 1, 1_000, false)), "PR", false, null);

        ResultadoPedido resultado = service.fechar(pedido, new Cliente(false, false, 1));

        assertAll(
                () -> assertEquals("PAGO", resultado.status()),
                () -> assertEquals(3, chamadas.get()),
                () -> assertEquals(List.of(11_200L, 11_200L, 11_200L), valores)
        );
    }

    @Test
    void deveRetornarPagamentoRecusadoQuandoTresTentativasFicamIndisponiveis() {
        AtomicInteger chamadas = new AtomicInteger();
        PedidoService service = new PedidoService(total -> {
            chamadas.incrementAndGet();
            throw new IllegalStateException("temporário");
        });
        Pedido pedido = new Pedido(List.of(item("A", 10_000, 1, 1, 1_000, false)), "PR", false, null);

        ResultadoPedido resultado = service.fechar(pedido, new Cliente(false, false, 1));

        assertAll(
                () -> assertEquals("PAGAMENTO_RECUSADO", resultado.status()),
                () -> assertEquals(3, chamadas.get())
        );
    }

    @Test
    void cupomDesconhecidoDeveInterromperAntesDoPagamento() {
        AtomicInteger chamadas = new AtomicInteger();
        PedidoService service = new PedidoService(total -> {
            chamadas.incrementAndGet();
            return true;
        });
        Pedido pedido = new Pedido(List.of(item("A", 20_000, 1, 1, 1_000, false)), "PR", false, "INVALIDO");

        assertThrows(IllegalArgumentException.class,
                () -> service.fechar(pedido, new Cliente(false, false, 1)));
        assertEquals(0, chamadas.get());
    }

    @Test
    void excecaoDefinitivaDoProcessadorDevePropagar() {
        AtomicInteger chamadas = new AtomicInteger();
        PedidoService service = new PedidoService(total -> {
            chamadas.incrementAndGet();
            throw new IllegalArgumentException("erro definitivo");
        });
        Pedido pedido = new Pedido(List.of(item("A", 10_000, 1, 1, 1_000, false)), "PR", false, null);

        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> service.fechar(pedido, new Cliente(false, false, 1)));

        assertAll(
                () -> assertEquals("erro definitivo", erro.getMessage()),
                () -> assertEquals(1, chamadas.get())
        );
    }
}
