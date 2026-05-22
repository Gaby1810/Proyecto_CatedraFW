package sv.edu.udb.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = DuiValidator.class) // Aquí vinculamos con la lógica
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDui {
    // Mensaje de error que verá el usuario en el Frontend
    String message() default "Formato de DUI no válido (ej: 00000000-0)";
    
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}