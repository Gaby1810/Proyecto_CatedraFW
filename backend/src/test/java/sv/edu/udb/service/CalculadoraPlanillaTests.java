package sv.edu.udb.service;

import org.junit.jupiter.api.Test;
import sv.edu.udb.service.impl.CalculadoraPlanilla;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Pruebas unitarias de CalculadoraPlanilla.
 *
 * Marco legal aplicado: Código de Trabajo El Salvador (CT),
 * Ley del ISSS, Ley SAP y tabla DGII de retención mensual de ISR.
 */
class CalculadoraPlanillaTests {

    private final CalculadoraPlanilla calculadora = new CalculadoraPlanilla();

    // ------------------------------------------------------------------
    // Salario bruto — horas ordinarias (Art. 161 CT)
    // ------------------------------------------------------------------

    /** 160 horas exactas = salario base completo + bonificación (caso nominal). */
    @Test
    void horasOrdinariasCubrenSalarioBase() {
        // tarifa = 800/160 = $5/h  →  bruto = 160×5 + 50 = $850
        Double bruto = calculadora.calcularSalarioBruto(800.00, 160.0, 50.00);
        assertEquals(850.00, bruto);
    }

    /** Período parcial: menos de 160 horas → pago proporcional. */
    @Test
    void horasParcialPagoProporcionado() {
        // tarifa = 720/160 = $4.50/h  →  bruto = 80×4.50 = $360.00
        Double bruto = calculadora.calcularSalarioBruto(720.00, 80.0, 0.0);
        assertEquals(360.00, bruto);
    }

    // ------------------------------------------------------------------
    // Horas extraordinarias — recargo 100 % (Art. 168 CT)
    // ------------------------------------------------------------------

    /**
     * Horas extras se pagan al doble de la tarifa ordinaria (Art. 168 CT).
     * 200 h = 160 ordinarias + 40 extraordinarias.
     * tarifa = 720/160 = $4.50/h
     * bruto = (160 × 4.50) + (40 × 2 × 4.50) = $720 + $360 = $1 080
     */
    @Test
    void horasExtrasAlDobleSegunArt168CT() {
        Double bruto = calculadora.calcularSalarioBruto(720.00, 200.0, 0.0);
        assertEquals(1080.00, bruto);
    }

    /** Método auxiliar calcularHorasExtra. */
    @Test
    void calcularHorasExtraDevuelveExcedenteDe160() {
        assertEquals(40.0,  calculadora.calcularHorasExtra(200.0));
        assertEquals(0.0,   calculadora.calcularHorasExtra(160.0));
        assertEquals(0.0,   calculadora.calcularHorasExtra(100.0));
    }

    /** Pago de horas extras aislado (sin salario ordinario ni bono). */
    @Test
    void pagoHorasExtraEsDobleDetallaOrdinaria() {
        // tarifa = 720/160 = 4.50 → 40 h × 2 × 4.50 = $360
        Double pago = calculadora.calcularPagoHorasExtra(720.00, 200.0);
        assertEquals(360.00, pago);
    }

    // ------------------------------------------------------------------
    // ISSS — 3 %, tope base $1 000 (Ley del ISSS, Art. 29)
    // ------------------------------------------------------------------

    /** Salario bajo el tope → 3 % normal. */
    @Test
    void isssCalculadoSinTope() {
        // 720 × 3 % = $21.60
        assertEquals(21.60, calculadora.calcularISSS(720.00));
    }

    /** Salario sobre el tope → cuota máxima $30. */
    @Test
    void isssTopeMillDolares() {
        assertEquals(30.00, calculadora.calcularISSS(1200.00));
        assertEquals(30.00, calculadora.calcularISSS(5000.00));
    }

    // ------------------------------------------------------------------
    // AFP — 7.25 % sin tope (Ley SAP)
    // ------------------------------------------------------------------

    @Test
    void afpSietePuntoVeinticinco() {
        // 1 200 × 7.25 % = $87.00
        assertEquals(87.00, calculadora.calcularAFP(1200.00));
    }

    // ------------------------------------------------------------------
    // ISR mensual — tabla DGII (Código Tributario Art. 156)
    // ------------------------------------------------------------------

    /** Tramo 1: base ≤ $472 → $0. */
    @Test
    void rentaCeroParaTramoDireccion() {
        assertEquals(0.0, calculadora.calcularRenta(400.00));
        assertEquals(0.0, calculadora.calcularRenta(472.00));
    }

    /** Tramo 2: $472.01 – $895.24 → cuota $17.67 + 10 % excedente. */
    @Test
    void rentaTramo2() {
        // base = $600 → ($600 - $472) × 10 % + $17.67 = $12.80 + $17.67 = $30.47
        assertEquals(30.47, calculadora.calcularRenta(600.00));
    }

    /** Tramo 3: $895.25 – $2 038.10 → cuota $60 + 20 % excedente. */
    @Test
    void rentaTramo3() {
        // base = $1 083 (bruto $1 200 − ISSS $30 − AFP $87)
        // ($1083 - $895.24) × 20 % + $60 = $187.76 × 0.20 + $60 = $37.55 + $60 = $97.55
        assertEquals(97.55, calculadora.calcularRenta(1083.00));
    }

    /** Tramo 4: > $2 038.10 → cuota $288.57 + 30 % excedente. */
    @Test
    void rentaTramo4() {
        // base = $2 500 → ($2500 - $2038.10) × 30 % + $288.57 = $461.90 × 0.30 + $288.57
        //                = $138.57 + $288.57 = $427.14
        assertEquals(427.14, calculadora.calcularRenta(2500.00));
    }

    // ------------------------------------------------------------------
    // Flujo completo de planilla
    // ------------------------------------------------------------------

    /** Verificación de flujo completo con tope ISSS y tabla ISR. */
    @Test
    void flujoCompletoConTopes() {
        Double isss  = calculadora.calcularISSS(1200.00);        // $30.00
        Double afp   = calculadora.calcularAFP(1200.00);         // $87.00
        Double renta = calculadora.calcularRenta(1083.00);       // $97.55  (base = 1200-30-87)
        Double total = calculadora.calcularTotalDescuentos(isss, afp, renta);
        Double neto  = calculadora.calcularSalarioNeto(1200.00, total);

        assertEquals(30.00,  isss);
        assertEquals(87.00,  afp);
        assertEquals(97.55,  renta);
        assertEquals(214.55, total);
        assertEquals(985.45, neto);
    }
}
