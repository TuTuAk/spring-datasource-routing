package de.kimrudolph.routing;

import de.kimrudolph.routing.entities.Customer;
import de.kimrudolph.routing.repositories.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitManager;
import org.springframework.orm.jpa.vendor.AbstractJpaVendorAdapter;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.HashMap;

@Configuration
@EnableJpaRepositories(basePackageClasses = CustomerRepository.class,
        entityManagerFactoryRef = "stagingEntityManager",
        transactionManagerRef = "stagingTransactionManager")
@EnableTransactionManagement
public class StagingDataSourceConfiguration {

    @Autowired(required = false)
    private PersistenceUnitManager persistenceUnitManager;

    @Bean
    @ConfigurationProperties("spring.jpa")
    public JpaProperties customerJpaProperties() {
        return new JpaProperties();
    }
    

    @Autowired
    private DataSource customerTestingDataSource;
    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean stagingEntityManager(
        final JpaProperties customerJpaProperties) {

        EntityManagerFactoryBuilder builder =
            createEntityManagerFactoryBuilder(customerJpaProperties);

        return builder.dataSource(customerTestingDataSource).packages(Customer.class)
            .persistenceUnit("stagingEntityManager").build();
    }

    @Bean
    @Primary
    public JpaTransactionManager stagingTransactionManager(
        @Qualifier("stagingEntityManager") final EntityManagerFactory factory) {
        return new JpaTransactionManager(factory);
    }

    private EntityManagerFactoryBuilder createEntityManagerFactoryBuilder(
        JpaProperties customerJpaProperties) {
        JpaVendorAdapter jpaVendorAdapter =
            createJpaVendorAdapter(customerJpaProperties);
        return new EntityManagerFactoryBuilder(jpaVendorAdapter,
            customerJpaProperties.getProperties(), this.persistenceUnitManager);
    }

    private JpaVendorAdapter createJpaVendorAdapter(
        JpaProperties jpaProperties) {
        AbstractJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        adapter.setShowSql(jpaProperties.isShowSql());
        adapter.setDatabase(jpaProperties.getDatabase());
        adapter.setDatabasePlatform(jpaProperties.getDatabasePlatform());
        adapter.setGenerateDdl(jpaProperties.isGenerateDdl());
        return adapter;
    }
}