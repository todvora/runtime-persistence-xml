## Runtime persistence.xml
[![Build Status](https://travis-ci.org/todvora/runtime-persistence-xml.svg?branch=master)](https://travis-ci.org/todvora/runtime-persistence-xml)
[![Coverage Status](https://img.shields.io/coveralls/todvora/runtime-persistence-xml.svg)](https://coveralls.io/r/todvora/runtime-persistence-xml?branch=master)

Generate and configure your persistence.xml at runtime!

## Usage
```
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
final EntityManager em = entityManagerFactory.createEntityManager();
```