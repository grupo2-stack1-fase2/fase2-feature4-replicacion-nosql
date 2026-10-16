package gt.edu.umg.desarrolloweb.sync.config;

import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClientBuilder;

/**
 * Crea el cliente de DynamoDB.
 *
 * - Con endpoint (por defecto http://localhost:8000): habla con DynamoDB LOCAL en Docker.
 *   Las credenciales son de mentira ("local"), DynamoDB Local no las valida.
 * - Sin endpoint (variable DYNAMODB_ENDPOINT vacia): habla con DynamoDB REAL de AWS y usa
 *   las credenciales normales (en ECS vendrian del Task Role). El codigo no cambia.
 */
@Configuration
public class DynamoDbConfig {

    @Bean
    public DynamoDbClient dynamoDbClient(@Value("${sync.dynamodb.endpoint:}") String endpoint,
                                         @Value("${sync.dynamodb.region:us-east-2}") String region) {
        DynamoDbClientBuilder builder = DynamoDbClient.builder().region(Region.of(region));

        if (endpoint != null && !endpoint.isBlank()) {
            builder.endpointOverride(URI.create(endpoint))
                   .credentialsProvider(StaticCredentialsProvider.create(
                           AwsBasicCredentials.create("local", "local")));
        } else {
            builder.credentialsProvider(DefaultCredentialsProvider.create());
        }
        return builder.build();
    }
}
