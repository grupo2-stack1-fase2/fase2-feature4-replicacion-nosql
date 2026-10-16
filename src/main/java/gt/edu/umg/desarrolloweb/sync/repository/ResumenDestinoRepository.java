package gt.edu.umg.desarrolloweb.sync.repository;

import gt.edu.umg.desarrolloweb.sync.model.ResumenContador;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.BillingMode;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;

/**
 * DESTINO: tabla "resumen_cuentas" en DynamoDB. Un item por contador.
 *
 * Clave primaria: contadorId (numero). En DynamoDB no hay esquema fijo: cada item es un
 * "documento" con atributos; solo la clave es obligatoria.
 *
 * Este repositorio NUNCA borra: solo lee la huella y hace putItem (upsert).
 */
@Repository
public class ResumenDestinoRepository {

    private static final String CLAVE = "contadorId";
    private static final String HUELLA = "huella";

    private final DynamoDbClient dynamo;
    private final String tabla;

    public ResumenDestinoRepository(DynamoDbClient dynamo,
                                    @Value("${sync.dynamodb.tabla:resumen_cuentas}") String tabla) {
        this.dynamo = dynamo;
        this.tabla = tabla;
    }

    /** Crea la tabla la primera vez. Si ya existe, no hace nada. */
    public void asegurarTabla() {
        DescribeTableRequest describe = DescribeTableRequest.builder().tableName(tabla).build();
        try {
            dynamo.describeTable(describe);
        } catch (ResourceNotFoundException noExiste) {
            dynamo.createTable(CreateTableRequest.builder()
                    .tableName(tabla)
                    .keySchema(KeySchemaElement.builder()
                            .attributeName(CLAVE).keyType(KeyType.HASH).build())
                    .attributeDefinitions(AttributeDefinition.builder()
                            .attributeName(CLAVE).attributeType(ScalarAttributeType.N).build())
                    .billingMode(BillingMode.PAY_PER_REQUEST)
                    .build());
            dynamo.waiter().waitUntilTableExists(describe);
        }
    }

    /** Huella guardada en DynamoDB para ese contador, o vacio si todavia no existe. */
    public Optional<String> obtenerHuella(Long contadorId) {
        GetItemResponse respuesta = dynamo.getItem(GetItemRequest.builder()
                .tableName(tabla)
                .key(Map.of(CLAVE, AttributeValue.fromN(String.valueOf(contadorId))))
                .projectionExpression(HUELLA)          // solo traemos la huella, no todo el item
                .build());
        if (!respuesta.hasItem() || respuesta.item().isEmpty()) {
            return Optional.empty();
        }
        AttributeValue huella = respuesta.item().get(HUELLA);
        return huella == null ? Optional.empty() : Optional.ofNullable(huella.s());
    }

    /** Upsert: si el item existe lo reemplaza, si no existe lo crea. Nunca borra otros items. */
    public void guardar(ResumenContador r, String huella, UUID jobId) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put(CLAVE, AttributeValue.fromN(String.valueOf(r.contadorId())));
        texto(item, "numeroRegistro", r.numeroRegistro());
        texto(item, "direccionServicio", r.direccionServicio());
        texto(item, "sector", r.sector());
        item.put("contadorActivo", AttributeValue.fromBool(r.contadorActivo()));
        item.put("clienteId", AttributeValue.fromN(String.valueOf(r.clienteId())));
        texto(item, "clienteNombre", r.clienteNombre());
        item.put("totalLecturas", AttributeValue.fromN(String.valueOf(r.totalLecturas())));
        item.put("lecturasPendientes", AttributeValue.fromN(String.valueOf(r.lecturasPendientes())));
        numero(item, "totalFacturado", r.totalFacturado());
        numero(item, "totalPagado", r.totalPagado());
        numero(item, "saldo", r.saldo());
        texto(item, "ultimoPeriodo", r.ultimoPeriodo());
        numero(item, "ultimaLectura", r.ultimaLectura());
        item.put(HUELLA, AttributeValue.fromS(huella));
        item.put("sincronizadoEn", AttributeValue.fromS(OffsetDateTime.now().toString()));
        item.put("jobId", AttributeValue.fromS(jobId.toString()));

        dynamo.putItem(PutItemRequest.builder().tableName(tabla).item(item).build());
    }

    // Los valores null simplemente no se guardan (en DynamoDB un atributo puede no existir)
    private static void texto(Map<String, AttributeValue> item, String nombre, String valor) {
        if (valor != null) {
            item.put(nombre, AttributeValue.fromS(valor));
        }
    }

    private static void numero(Map<String, AttributeValue> item, String nombre, BigDecimal valor) {
        if (valor != null) {
            item.put(nombre, AttributeValue.fromN(valor.toPlainString()));
        }
    }
}
