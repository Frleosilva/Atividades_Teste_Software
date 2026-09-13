package br.edu.ifpr.boletim;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BoletimTest {

    @Test
    void deveAprovarAlunoComMediaOito() {
        Boletim boletim = new Boletim();
        String resultado = boletim.verificarSituacao(8);
        assertEquals("APROVADO", resultado);
    }

    @Test
    void deveAprovarAlunoNoLimiteSete() {
        Boletim boletim = new Boletim();
        String resultado = boletim.verificarSituacao(7);
        assertEquals("APROVADO", resultado);
    }

    @Test
    void deveRecuperarAlunoNoLimiteQuatro() {
        Boletim boletim = new Boletim();
        String resultado = boletim.verificarSituacao(4);
        assertEquals("RECUPERACAO", resultado);
    }

    @Test
    void deveRecuperarAlunoComMediaEntreQuatroESete() {
        Boletim boletim = new Boletim();
        String resultado = boletim.verificarSituacao(6.9);
        assertEquals("RECUPERACAO", resultado);
    }

    @Test
    void deveReprovarAlunoComMediaAbaixoDeQuatro() {
        Boletim boletim = new Boletim();
        String resultado = boletim.verificarSituacao(3.9);
        assertEquals("REPROVADO", resultado);
    }

    @Test
    void deveCalcularMediaInteira() {
        Boletim boletim = new Boletim();
        double resultado = boletim.calcularMedia(5, 5);
        assertEquals(5.0, resultado, 0.0001);
    }

    @Test
    void deveCalcularMediaComParteDecimal() {
        Boletim boletim = new Boletim();
        double resultado = boletim.calcularMedia(8.5, 7.0);
        assertEquals(7.75, resultado, 0.0001);
    }

    @Test
    void deveRetornarZeroParaArrayVazio() {
        Boletim boletim = new Boletim();
        int resultado = boletim.contarAprovados(new double[] {});
        assertEquals(0, resultado);
    }

    @Test
    void deveContarUmAprovadoComUmElemento() {
        Boletim boletim = new Boletim();
        int resultado = boletim.contarAprovados(new double[] {7});
        assertEquals(1, resultado);
    }

    @Test
    void deveContarZeroAprovadosComUmElementoReprovado() {
        Boletim boletim = new Boletim();
        int resultado = boletim.contarAprovados(new double[] {6.9});
        assertEquals(0, resultado);
    }

    @Test
    void deveContarAprovadosEmArrayComVariasMedias() {
        Boletim boletim = new Boletim();
        int resultado = boletim.contarAprovados(new double[] {8, 5, 7, 3, 9});
        assertEquals(3, resultado);
    }
}
