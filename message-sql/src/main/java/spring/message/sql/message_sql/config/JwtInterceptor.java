package spring.message.sql.message_sql.config;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        // Aquí debes obtener el token JWT de alguna fuente segura
        String jwtToken = "Bearer " + getJwtToken();
        request.getHeaders().add(HttpHeaders.AUTHORIZATION, jwtToken);
        return execution.execute(request, body);
    }

    private String getJwtToken() {
        // Implementa la lógica para obtener el token JWT
        return "your-jwt-token";
    }
}