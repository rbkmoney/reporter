package com.rbkmoney.reporter.config;

import com.rbkmoney.easyway.TestContainers;
import com.rbkmoney.easyway.TestContainersBuilder;
import com.rbkmoney.easyway.TestContainersParameters;
import com.rbkmoney.reporter.ReporterApplication;
import com.rbkmoney.reporter.service.ReportNewProtoService;
import com.rbkmoney.reporter.service.StorageService;
import com.rbkmoney.reporter.service.impl.TaskServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.function.Supplier;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ContextConfiguration(classes = ReporterApplication.class)
@TestPropertySource("classpath:application.yml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Slf4j
public abstract class AbstractInvoicingServiceConfig {

    @MockBean
    private ReportNewProtoService reportNewProtoService;

    @MockBean
    private StorageService S3StorageServiceImpl;

    @MockBean
    private TaskServiceImpl taskService;

    private static TestContainers testContainers =
            TestContainersBuilder.builderWithTestContainers(getTestContainersParametersSupplier())
                    .addPostgresqlTestContainer()
                    .build();

    private static Supplier<TestContainersParameters> getTestContainersParametersSupplier() {
        return () -> {
            TestContainersParameters testContainersParameters = new TestContainersParameters();
            testContainersParameters.setPostgresqlJdbcUrl("jdbc:postgresql://localhost:5432/reporter");

            return testContainersParameters;
        };
    }

}
