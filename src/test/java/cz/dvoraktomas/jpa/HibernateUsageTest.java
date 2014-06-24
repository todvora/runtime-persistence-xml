package cz.dvoraktomas.jpa;

import org.hibernate.jpa.HibernatePersistenceProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceUnitTransactionType;
import java.util.UUID;

public class HibernateUsageTest {

    private EntityManager em;

    @Before
    public void setUp() throws Exception {
        RuntimePersistenceGenerator generator = new RuntimePersistenceGenerator("test", PersistenceUnitTransactionType.RESOURCE_LOCAL, HibernatePersistenceProvider.class);
        generator.addProperty("javax.persistence.jdbc.driver", "org.hsqldb.jdbcDriver");
        generator.addProperty("javax.persistence.jdbc.url", "jdbc:hsqldb:mem:" + UUID.randomUUID().toString());
        generator.addProperty("javax.persistence.jdbc.user", "sa");
        generator.addProperty("javax.persistence.jdbc.password", "");
        generator.addProperty("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");

        generator.addProperty("hibernate.show_sql", "true");
        generator.addProperty("hibernate.connection.shutdown", "true");
        generator.addProperty("hibernate.hbm2ddl.auto", "create-drop");

        generator.addAnnotatedClass(TestObject.class);

        EntityManagerFactory entityManagerFactory = generator.createEntityManagerFactory();
        em = entityManagerFactory.createEntityManager();
        em.getTransaction().begin();
    }

    @Test
    public void testCrud() throws Exception {
        TestObject testObject = new TestObject("lorem.ipsum", "lorem.ipsum@dolor.sit");
        em.persist(testObject);
        TestObject fromDb = em.find(TestObject.class, "lorem.ipsum");
        Assert.assertEquals(testObject, fromDb);

        testObject.setEmail("foo.bar@example.com");
        em.persist(testObject);

        TestObject fromDbUpdated = em.find(TestObject.class, "lorem.ipsum");
        Assert.assertEquals("foo.bar@example.com", fromDbUpdated.getEmail());

        em.remove(testObject);

        Assert.assertNull(em.find(TestObject.class, "lorem.ipsum"));
    }
}
