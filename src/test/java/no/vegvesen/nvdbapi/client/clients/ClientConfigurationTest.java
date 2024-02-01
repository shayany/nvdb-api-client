package no.vegvesen.nvdbapi.client.clients;

import com.github.tomakehurst.wiremock.WireMockServer;
import no.vegvesen.nvdbapi.client.ClientConfiguration.ClientConfigurationBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.ws.rs.ProcessingException;
import java.util.Random;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ClientConfigurationTest {

    private static WireMockServer wireMockServer;

    @BeforeAll
    public static void setUp() {
        wireMockServer = new WireMockServer(options().port(1024 + new Random().nextInt(20000)));
        wireMockServer.start();
    }

    @AfterAll
    public static void cleanUp() {
        wireMockServer.stop();
    }

    @Test
    public void shouldHonorReadTimeout() {
        configureFor("localhost", wireMockServer.port());
        int delayInMillis = 200;
        stubFor(get(urlEqualTo("/vegobjekttyper/versjon")).willReturn(
                aResponse()
                        .withStatus(200)
                        .withFixedDelay(delayInMillis)));

        ClientFactory clientFactory = new ClientFactory(wireMockServer.baseUrl(),
                "nvdbapi-client-test", ClientConfigurationBuilder.builder()
                                                                 .withReadTimeout(delayInMillis - 100)
                                                                 .withConnectTimeout(delayInMillis * 10)
                                                                 .build());
        Exception exception = Assertions.assertThrows(ProcessingException.class, clientFactory::getDatakatalog);
        assertTrue(exception.getMessage()
                            .contains("Timeout"));
    }
}
