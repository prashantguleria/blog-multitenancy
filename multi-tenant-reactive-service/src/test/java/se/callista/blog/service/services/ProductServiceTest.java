package se.callista.blog.service.services;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.database.rider.core.api.dataset.DataSet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import se.callista.blog.service.annotation.SpringBootDbIntegrationTest;
import se.callista.blog.service.model.ProductValue;
import se.callista.blog.service.multi_tenancy.config.tenant.liquibase.DynamicSchemaBasedMultiTenantSpringLiquibase;
import se.callista.blog.service.multi_tenancy.util.TenantContext;
import se.callista.blog.service.persistence.PostgresqlTestContainer;

@Testcontainers
@SpringBootDbIntegrationTest
public class ProductServiceTest {

    @Container
    private static final PostgresqlTestContainer POSTGRESQL_CONTAINER = PostgresqlTestContainer.getInstance();

    @Autowired
    private ProductService productService;

    @Autowired
    DynamicSchemaBasedMultiTenantSpringLiquibase tenantSpringLiquibase;

    @BeforeEach
    @DataSet(value = {"tenants.yml"}, executeStatementsBefore = {
        "CREATE SCHEMA IF NOT EXISTS tenant1_schema;",
        "CREATE SCHEMA IF NOT EXISTS tenant2_schema;"
    })
    public void setUpSchemas() throws Exception {
        tenantSpringLiquibase.afterPropertiesSet();
    }

    @AfterEach
    @DataSet(executeStatementsBefore = {
        "DROP SCHEMA tenant1_schema cascade;",
        "DROP SCHEMA tenant2_schema cascade;"
    })
    public void tearDownSchemas() throws Exception {
    }

    @Test
    @DataSet(value = {"products.yml"})
    public void getProductForTenant1() {

        ProductValue product = TenantContext.withTenantId("tenant1",
            productService.getProduct(1))
            .block();
        assertThat(product.getName()).isEqualTo("Product 1");

    }

    @Test
    @DataSet(value = {"products.yml"})
    public void getProductForTenant2() {

        ProductValue product = TenantContext.withTenantId("tenant2",
                productService.getProduct(1))
            .block();
        assertThat(product).isNull();

    }

}