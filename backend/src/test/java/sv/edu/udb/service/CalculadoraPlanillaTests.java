package sv.edu.udb.service;

import org.junit.jupiter.api.Test;
import sv.edu.udb.service.impl.CalculadoraPlanilla;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CalculadoraPlanillaTests {

    private final CalculadoraPlanilla calculadora = new CalculadoraPlanilla();

    @Test
    void shouldCalculateGrossSalaryUsingHoursAndBonus() {
        Double bruto = calculadora.calcularSalarioBruto(800.00, 160.0, 50.00);
        assertEquals(850.00, bruto);
    }

    @Test
    void shouldCapIsssAndComputePayrollFigures() {
        Double isss = calculadora.calcularISSS(1200.00);
        Double afp = calculadora.calcularAFP(1200.00);
        Double renta = calculadora.calcularRenta(1083.00);
        Double total = calculadora.calcularTotalDescuentos(isss, afp, renta);
        Double neto = calculadora.calcularSalarioNeto(1200.00, total);

        assertEquals(30.00, isss);
        assertEquals(87.00, afp);
        assertEquals(97.55, renta);
        assertEquals(214.55, total);
        assertEquals(985.45, neto);
    }
}
