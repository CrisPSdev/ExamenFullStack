package cl.duoc.resenas.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI resenasOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Microservicio de Reseñas API")
                        .description("API para registrar y consultar reseñas y notas de destinos turísticos. "
                                + "Cada reseña se asocia a un destino (Destination Service) y a un usuario "
                                + "autenticado (Login Service).")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Cristóbal Pardo")
                                .email("vi.cofref@profesor.duoc.cl"))
                        .license(new License()
                                .name("Apache 2.0")));
    }
}
