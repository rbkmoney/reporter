package com.rbkmoney.reporter;

import com.rbkmoney.reporter.model.PartyModel;
import com.rbkmoney.reporter.model.ShopAccountingModel;
import com.rbkmoney.reporter.service.PartyService;
import com.rbkmoney.reporter.service.SignService;
import com.rbkmoney.reporter.service.StatisticService;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.util.EnvironmentTestUtils;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Base64;

import static io.github.benas.randombeans.api.EnhancedRandom.random;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ContextConfiguration(classes = ReporterApplication.class, initializers = AbstractIntegrationTest.Initializer.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class AbstractIntegrationTest {

    @ClassRule
    public static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:9.6");

    @ClassRule
    public static LocalStackContainer localstack = new LocalStackContainer()
            .withServices(S3);

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            EnvironmentTestUtils.addEnvironment("testcontainers", configurableApplicationContext.getEnvironment(),
                    "spring.datasource.url=" + postgres.getJdbcUrl(),
                    "spring.datasource.username=" + postgres.getUsername(),
                    "spring.datasource.password=" + postgres.getPassword(),
                    "flyway.url=" + postgres.getJdbcUrl(),
                    "flyway.user=" + postgres.getUsername(),
                    "flyway.password=" + postgres.getPassword(),
                    "storage.endpoint=" + localstack.getEndpointConfiguration(S3).getServiceEndpoint(),
                    "storage.signingRegion=" + localstack.getEndpointConfiguration(S3).getSigningRegion(),
                    "AWS_ACCESS_KEY=" + localstack.getDefaultCredentialsProvider().getCredentials().getAWSAccessKeyId(),
                    "AWS_SECRET_KEY=" + localstack.getDefaultCredentialsProvider().getCredentials().getAWSSecretKey()
            );
        }
    }

    @MockBean
    private StatisticService statisticService;

    @MockBean
    private PartyService partyService;

    @MockBean
    private SignService signService;

    @Before
    public void setup() {
        given(statisticService.getShopAccounting(anyString(), anyString(), any(Instant.class), any(Instant.class))).willReturn(random(ShopAccountingModel.class));
        given(partyService.getPartyRepresentation(anyString(), anyString(), any(Instant.class))).willReturn(random(PartyModel.class));
        given(signService.sign(any(Path.class)))
                .willAnswer(
                        (Answer<byte[]>) invocation -> Base64.getEncoder().encode(Files.readAllBytes(invocation.getArgumentAt(0, Path.class)))
                );
    }

    @Value("${local.server.port}")
    protected int port;

}
