package sv.edu.udb.service.impl;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class CalculadoraPlanilla {

    private static final BigDecimal HORAS_BASE_MENSUALES = BigDecimal.valueOf(160);
    private static final BigDecimal ISSS_RATE = BigDecimal.valueOf(0.03);
    private static final BigDecimal AFP_RATE = BigDecimal.valueOf(0.0725);
    private static final BigDecimal ISSS_CAP = BigDecimal.valueOf(1000);

    public Double calcularSalarioBruto(Double salarioBase, Double horasTrabajadas, Double bonificacion) {
        BigDecimal tarifaHora = bd(salarioBase).divide(HORAS_BASE_MENSUALES, 6, RoundingMode.HALF_UP);
        BigDecimal bruto = tarifaHora.multiply(bd(horasTrabajadas)).add(bd(bonificacion));
        return redondear(bruto).doubleValue();
    }

    public Double calcularISSS(Double salarioBruto) {
        BigDecimal baseIsss = bd(salarioBruto).min(ISSS_CAP);
        return redondear(baseIsss.multiply(ISSS_RATE)).doubleValue();
    }

    public Double calcularAFP(Double salarioBruto) {
        return redondear(bd(salarioBruto).multiply(AFP_RATE)).doubleValue();
    }

    public Double calcularRenta(Double salarioBaseImponible) {
        BigDecimal salario = bd(salarioBaseImponible);

        if (salario.compareTo(BigDecimal.valueOf(472.00)) <= 0) {
            return 0.0;
        }

        if (salario.compareTo(BigDecimal.valueOf(895.24)) <= 0) {
            BigDecimal renta = salario.subtract(BigDecimal.valueOf(472))
                    .multiply(BigDecimal.valueOf(0.10))
                    .add(BigDecimal.valueOf(17.67));
            return redondear(renta).doubleValue();
        }

        if (salario.compareTo(BigDecimal.valueOf(2038.10)) <= 0) {
            BigDecimal renta = salario.subtract(BigDecimal.valueOf(895.24))
                    .multiply(BigDecimal.valueOf(0.20))
                    .add(BigDecimal.valueOf(60));
            return redondear(renta).doubleValue();
        }

        BigDecimal renta = salario.subtract(BigDecimal.valueOf(2038.10))
                .multiply(BigDecimal.valueOf(0.30))
                .add(BigDecimal.valueOf(288.57));
        return redondear(renta).doubleValue();
    }

    public Double calcularTotalDescuentos(Double isss, Double afp, Double renta) {
        return redondear(bd(isss).add(bd(afp)).add(bd(renta))).doubleValue();
    }

    public Double calcularSalarioNeto(Double salarioBruto, Double totalDescuentos) {
        return redondear(bd(salarioBruto).subtract(bd(totalDescuentos))).doubleValue();
    }

    private BigDecimal bd(Double valor) {
        return BigDecimal.valueOf(valor == null ? 0.0 : valor);
    }

    private BigDecimal redondear(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }
}
