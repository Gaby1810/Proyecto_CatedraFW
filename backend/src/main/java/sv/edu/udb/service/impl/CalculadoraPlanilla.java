package sv.edu.udb.service.impl;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Calculadora de planilla bajo el marco legal de El Salvador.
 *
 * <p>Referencias legales aplicadas:
 * <ul>
 *   <li><b>Art. 161 CT</b>: jornada ordinaria máxima — 8 h/día, 44 h/semana.
 *       Convención de cálculo mensual: 160 h (20 días × 8 h).</li>
 *   <li><b>Art. 168 CT</b>: horas extraordinarias se pagan con un recargo del
 *       100 % (es decir, al doble de la tarifa ordinaria).</li>
 *   <li><b>Ley del ISSS, Art. 29</b>: cuota empleado = 3 % sobre salario,
 *       base imponible máxima $1 000/mes → cuota máxima $30/mes.</li>
 *   <li><b>Ley SAP (AFP)</b>: cotización empleado = 7.25 % sobre salario, sin tope.</li>
 *   <li><b>Código Tributario Art. 156 / Tabla DGII</b>: retención mensual de ISR
 *       calculada por tramos sobre la base imponible neta (bruto − ISSS − AFP).</li>
 * </ul>
 */
@Component
public class CalculadoraPlanilla {

    /** Horas ordinarias de referencia mensual (Art. 161 CT: 8 h/día × 20 días). */
    private static final BigDecimal HORAS_BASE_MENSUALES = BigDecimal.valueOf(160);

    /** Tasa ISSS empleado: 3 % (Ley del ISSS, Art. 29). */
    private static final BigDecimal ISSS_RATE = BigDecimal.valueOf(0.03);

    /** Tope de base imponible para ISSS: $1 000/mes (Ley del ISSS, Art. 29). */
    private static final BigDecimal ISSS_CAP = BigDecimal.valueOf(1000);

    /** Tasa AFP empleado: 7.25 % sin tope (Ley SAP). */
    private static final BigDecimal AFP_RATE = BigDecimal.valueOf(0.0725);

    // -----------------------------------------------------------------------
    // Salario bruto
    // -----------------------------------------------------------------------

    /**
     * Calcula el salario bruto respetando el Código de Trabajo de El Salvador.
     *
     * <ul>
     *   <li>Horas ordinarias (≤ 160 h/mes): tarifa = salarioBase / 160.</li>
     *   <li>Horas extraordinarias (> 160 h/mes): recargo del 100 % → se pagan
     *       al <b>doble</b> de la tarifa ordinaria (Art. 168 CT).</li>
     * </ul>
     *
     * @param salarioBase      salario mensual base pactado en contrato (para 160 h)
     * @param horasTrabajadas  horas efectivamente trabajadas en el período
     * @param bonificacion     bonificación adicional (puede ser 0)
     * @return salario bruto del período
     */
    public Double calcularSalarioBruto(Double salarioBase, Double horasTrabajadas, Double bonificacion) {
        BigDecimal base     = bd(salarioBase);
        BigDecimal horas    = bd(horasTrabajadas);
        BigDecimal tarifa   = base.divide(HORAS_BASE_MENSUALES, 6, RoundingMode.HALF_UP);

        // Horas ordinarias (máximo 160)
        BigDecimal horasOrd = horas.min(HORAS_BASE_MENSUALES);
        BigDecimal salOrd   = tarifa.multiply(horasOrd);

        // Horas extraordinarias pagadas al doble (Art. 168 CT)
        BigDecimal horasExt = horas.subtract(HORAS_BASE_MENSUALES).max(BigDecimal.ZERO);
        BigDecimal salExt   = tarifa.multiply(BigDecimal.valueOf(2)).multiply(horasExt);

        return redondear(salOrd.add(salExt).add(bd(bonificacion))).doubleValue();
    }

    /**
     * Devuelve las horas extraordinarias del período (por encima de 160 h).
     * Útil para mostrar el desglose en la boleta.
     */
    public Double calcularHorasExtra(Double horasTrabajadas) {
        return Math.max(0.0, horasTrabajadas - 160.0);
    }

    /**
     * Monto bruto correspondiente sólo a horas extraordinarias (sin salario ordinario ni bonificación).
     * Recargo del 100 % sobre la tarifa ordinaria (Art. 168 CT).
     */
    public Double calcularPagoHorasExtra(Double salarioBase, Double horasTrabajadas) {
        BigDecimal tarifa   = bd(salarioBase).divide(HORAS_BASE_MENSUALES, 6, RoundingMode.HALF_UP);
        BigDecimal horasExt = bd(horasTrabajadas).subtract(HORAS_BASE_MENSUALES).max(BigDecimal.ZERO);
        return redondear(tarifa.multiply(BigDecimal.valueOf(2)).multiply(horasExt)).doubleValue();
    }

    // -----------------------------------------------------------------------
    // Descuentos de ley
    // -----------------------------------------------------------------------

    /**
     * Cuota ISSS empleado: 3 % sobre salario bruto con tope de base $1 000
     * (Ley del ISSS, Art. 29). Cuota máxima: $30/mes.
     */
    public Double calcularISSS(Double salarioBruto) {
        BigDecimal baseIsss = bd(salarioBruto).min(ISSS_CAP);
        return redondear(baseIsss.multiply(ISSS_RATE)).doubleValue();
    }

    /**
     * Cotización AFP empleado: 7.25 % sobre salario bruto completo, sin tope
     * (Ley SAP).
     */
    public Double calcularAFP(Double salarioBruto) {
        return redondear(bd(salarioBruto).multiply(AFP_RATE)).doubleValue();
    }

    /**
     * Retención mensual de ISR según la tabla oficial de la DGII El Salvador
     * (Código Tributario Art. 156).
     *
     * <p><b>Base imponible</b>: salario bruto <em>menos</em> cuotas ISSS y AFP
     * (ambas son deducibles antes de calcular el ISR).
     *
     * <table border="1">
     *   <tr><th>Tramo</th><th>Cuota fija</th><th>% excedente</th></tr>
     *   <tr><td>$0.01 – $472.00</td><td>$0.00</td><td>0 %</td></tr>
     *   <tr><td>$472.01 – $895.24</td><td>$17.67</td><td>10 %</td></tr>
     *   <tr><td>$895.25 – $2 038.10</td><td>$60.00</td><td>20 %</td></tr>
     *   <tr><td>> $2 038.10</td><td>$288.57</td><td>30 %</td></tr>
     * </table>
     *
     * @param salarioBaseImponible salarioBruto − ISSS − AFP
     */
    public Double calcularRenta(Double salarioBaseImponible) {
        BigDecimal s = bd(salarioBaseImponible);

        if (s.compareTo(BigDecimal.valueOf(472.00)) <= 0) {
            return 0.0;
        }
        if (s.compareTo(BigDecimal.valueOf(895.24)) <= 0) {
            return redondear(
                s.subtract(BigDecimal.valueOf(472.00))
                 .multiply(BigDecimal.valueOf(0.10))
                 .add(BigDecimal.valueOf(17.67))
            ).doubleValue();
        }
        if (s.compareTo(BigDecimal.valueOf(2038.10)) <= 0) {
            return redondear(
                s.subtract(BigDecimal.valueOf(895.24))
                 .multiply(BigDecimal.valueOf(0.20))
                 .add(BigDecimal.valueOf(60.00))
            ).doubleValue();
        }
        return redondear(
            s.subtract(BigDecimal.valueOf(2038.10))
             .multiply(BigDecimal.valueOf(0.30))
             .add(BigDecimal.valueOf(288.57))
        ).doubleValue();
    }

    // -----------------------------------------------------------------------
    // Totales
    // -----------------------------------------------------------------------

    public Double calcularTotalDescuentos(Double isss, Double afp, Double renta) {
        return redondear(bd(isss).add(bd(afp)).add(bd(renta))).doubleValue();
    }

    public Double calcularSalarioNeto(Double salarioBruto, Double totalDescuentos) {
        return redondear(bd(salarioBruto).subtract(bd(totalDescuentos))).doubleValue();
    }

    // -----------------------------------------------------------------------
    // Helpers privados
    // -----------------------------------------------------------------------

    private BigDecimal bd(Double valor) {
        return BigDecimal.valueOf(valor == null ? 0.0 : valor);
    }

    private BigDecimal redondear(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }
}
