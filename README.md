## Runtime persistence.xml
[![Build Status](https://travis-ci.org/todvora/runtime-persistence-xml.svg?branch=master)](https://travis-ci.org/todvora/runtime-persistence-xml)
[![Coverage Status](https://img.shields.io/coveralls/todvora/runtime-persistence-xml.svg)](https://coveralls.io/r/todvora/runtime-persistence-xml?branch=master)

Generate and configure your persistence.xml at runtime!

## Usage

Generator is useful mainly in tests. It lets you easily configure in-memory database like [hsqldb](http://hsqldb.org/) or [h2](http://www.h2database.com/), define hibernate properties and you can specify, which classes will be used to configure persistence. That is possible in pure hibernate ([Configuration.addAnnotatedClass](http://docs.jboss.org/hibernate/stable/annotations/reference/en/html/ch01.html)), but not when you dealing with [JPA](http://en.wikipedia.org/wiki/Java_Persistence_API) EntityManager.


Usage is simple - just configure unit name, transaction type and persistence provider. Than you can add you properties with connection url, password and so one. At the end, you can specify, which annotated classes to add. 

You can get EntityManagerFactory directly from generator, calling method `createEntityManagerFactory()`. It is also possible to get XML in form, that is compatible with XML representation stored in `persistence.xml` by calling `generator.toString()`.

```java
RuntimePersistenceGenerator generator = new RuntimePersistenceGenerator("test", PersistenceUnitTransactionType.RESOURCE_LOCAL, HibernatePersistenceProvider.class);

generator.addProperty("javax.persistence.jdbc.driver", "org.hsqldb.jdbcDriver");
generator.addProperty("javax.persistence.jdbc.url", "jdbc:hsqldb:mem:db_name");
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

## How is it working?
The main trick is hacked classloader, that will provide own generated temp file, when java asks for `persistence.xml`. 
