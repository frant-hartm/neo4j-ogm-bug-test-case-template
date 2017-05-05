package org.neo4j.testcasetemplate;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.TestServer;
import org.neo4j.testcasetemplate.domain.User;
import org.neo4j.testcasetemplate.repository.UserRepository;
import org.neo4j.testcasetemplate.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.transaction.Neo4jTransactionManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = SdnTestCase.Config.class)
@EnableTransactionManagement
public class SdnTestCase {

    @ClassRule
    public static Neo4jRule neoServer = new Neo4jRule();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Test
    public void sampleTest() {
        User user = new User("noone@nowhere.com", "No", "One");
        HashSet<String> emails = new HashSet<>();
        emails.add("a@example.com");
        emails.add("b@example.com");
        user.setEmails(emails);
        User saved = userRepository.save(user);

        assertThat(userRepository.findByEmailsContains("b@example.com")).isNotNull();
    }

    @Configuration
    @EnableNeo4jRepositories(basePackageClasses = UserRepository.class)
    @ComponentScan(basePackageClasses = UserService.class)
    static class Config {

        @Bean
        public SessionFactory getSessionFactory() {
            return new SessionFactory(configuration(), User.class.getPackage().getName());
        }

        @Bean
        public Neo4jTransactionManager transactionManager() throws Exception {
            return new Neo4jTransactionManager(getSessionFactory());
        }

        @Bean
        public org.neo4j.ogm.config.Configuration configuration() {
            org.neo4j.ogm.config.Configuration configuration = new org.neo4j.ogm.config.Configuration();
            configuration.driverConfiguration()
                    .setDriverClassName("org.neo4j.ogm.drivers.bolt.driver.BoltDriver")
                    .setURI(neoServer.boltURI().toString());
// use this for HTTP driver
//					.setDriverClassName("org.neo4j.ogm.drivers.http.driver.HttpDriver")
//					.setURI(neoServer.boltURI().toString());

            return configuration;
        }
    }
}
