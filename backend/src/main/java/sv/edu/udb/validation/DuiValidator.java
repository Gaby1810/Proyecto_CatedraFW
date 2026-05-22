package sv.edu.udb.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DuiValidator implements ConstraintValidator<ValidDui, String> {

    @Override
    public void initialize(ValidDui constraintAnnotation) {
        // No se requiere inicialización especial
    }

    @Override
    public boolean isValid(String duiField, ConstraintValidatorContext context) {
        // Si es nulo, dejamos que @NotNull se encargue si es necesario
        if (duiField == null || duiField.isEmpty()) {
            return true; 
        }

        // Expresión regular para 8 dígitos - 1 dígito
        return duiField.matches("\\d{8}-\\d");
    }
}