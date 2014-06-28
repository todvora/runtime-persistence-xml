package cz.dvoraktomas.jpa;


import org.hibernate.jpa.HibernatePersistenceProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.spi.PersistenceUnitTransactionType;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

public class RuntimePersistenceGeneratorTest {

    private RuntimePersistenceGenerator generator;

    @Before
    public void setUp() throws Exception {
        generator = new RuntimePersistenceGenerator("test", PersistenceUnitTransactionType.RESOURCE_LOCAL, HibernatePersistenceProvider.class);
        generator.addProperty("javax.persistence.jdbc.driver", "org.hsqldb.jdbcDriver");
        generator.addProperty("javax.persistence.jdbc.url", "jdbc:hsqldb:mem:.");
        generator.addProperty("javax.persistence.jdbc.user", "sa");
        generator.addProperty("javax.persistence.jdbc.password", "");
        generator.addProperty("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");

        generator.addProperty("hibernate.show_sql", "true");
        generator.addProperty("hibernate.connection.shutdown", "true");
        generator.addProperty("hibernate.hbm2ddl.auto", "create-drop");

        generator.addAnnotatedClass(TestObject.class);
    }

    @Test
    public void testRenderToXml() throws Exception {
        URI persistenceXmlPath = this.getClass().getResource("/persistence.xml").toURI();
        String textContent = new String(Files.readAllBytes(Paths.get(persistenceXmlPath)));
        String expected = textContent.replaceAll("\\n\\s*", "");
        Assert.assertEquals(expected, generator.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalPropertyKey() throws Exception {
        generator.addProperty(null, "value");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalPropertyValue() throws Exception {
        generator.addProperty("key", null);
    }
}
