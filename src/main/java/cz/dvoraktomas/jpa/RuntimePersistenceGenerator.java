package cz.dvoraktomas.jpa;


import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class RuntimePersistenceGenerator {
    private final String unitName;
    private final PersistenceUnitTransactionType transactionType;
    private final Class<? extends PersistenceProvider> provider;

    private final Set<Class> classes = new LinkedHashSet<>();
    private final Map<String, String> properties = new LinkedHashMap<>();

    public RuntimePersistenceGenerator(String unitName, PersistenceUnitTransactionType transactionType, Class<? extends PersistenceProvider> provider) {
        this.unitName = unitName;
        this.transactionType = transactionType;
        this.provider = provider;
    }

    public RuntimePersistenceGenerator addProperty(final String key, final String value) {

        if (key == null) {
            throw new IllegalArgumentException("Property key cannot be null");
        }

        if (value == null) {
            throw new IllegalArgumentException("Property value cannot be null");
        }
        properties.put(key, value);
        return this;
    }

    public RuntimePersistenceGenerator addAnnotatedClass(final Class clazz) {
        classes.add(clazz);
        return this;
    }

    public EntityManagerFactory createEntityManagerFactory() {
        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            final String persistenceContent = generateXml();
            Thread.currentThread().setContextClassLoader(createClassLoader(persistenceContent));
            return Persistence.createEntityManagerFactory(this.unitName);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }
    }

    protected Document createDocument() throws ParserConfigurationException {
        final Document doc = createXmlDocument();
        final Element persistence = createPersistenceElement(doc);
        doc.appendChild(persistence);
        final Element unit = createPersistenceUnitElement(doc);
        persistence.appendChild(unit);
        unit.appendChild(createProviderElement(doc, provider.getName()));

        for (final Class clazz : this.classes) {
            final Element classElement = doc.createElement("class");
            classElement.setTextContent(clazz.getName());
            unit.appendChild(classElement);
        }

        if (!properties.isEmpty()) {
            final Element propertiesElement = createPropertiesElement(doc);
            unit.appendChild(propertiesElement);
        }
        return doc;
    }

    private Element createPersistenceUnitElement(Document doc) {
        final Element unit = doc.createElement("persistence-unit");
        unit.setAttribute("name", unitName);
        unit.setAttribute("transaction-type", transactionType.name());
        return unit;
    }

    private Element createPropertiesElement(Document doc) {
        final Element propertiesElement = doc.createElement("properties");
        for (final Map.Entry<String, String> entry : properties.entrySet()) {
            final Element property = createPropertyElement(doc, entry);
            propertiesElement.appendChild(property);
        }
        return propertiesElement;
    }

    private Element createPropertyElement(Document doc, Map.Entry<String, String> entry) {
        final Element property = doc.createElement("property");
        property.setAttribute("name", entry.getKey());
        property.setAttribute("value", entry.getValue());
        return property;
    }

    protected Element createPersistenceElement(final Document doc) {
        final Element persistence = doc.createElement("persistence");
        persistence.setAttribute("version", "2.0");
        persistence.setAttribute("xmlns", "http://java.sun.com/xml/ns/persistence");
        persistence.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        persistence.setAttribute("xsi:schemaLocation", "http://java.sun.com/xml/ns/persistence http://java.sun.com/xml/ns/persistence/persistence_2_0.xsd");
        return persistence;
    }

    protected Element createProviderElement(final Document doc, final String provider) {
        final Element element = doc.createElement("provider");
        element.setTextContent(provider);
        return element;
    }

    protected Document createXmlDocument() throws ParserConfigurationException {
        final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        return docBuilder.newDocument();
    }

    protected ClassLoader createClassLoader(final String persistenceContent) {
        return new ClassLoader() {
            @Override
            protected Enumeration<URL> findResources(final String name) throws IOException {
                if ("META-INF/persistence.xml".equals(name)) {
                    // lets hack it
                    final Path file = getTempFile();
                    final BufferedWriter bufferedWriter = Files.newBufferedWriter(file, Charset.defaultCharset());

                    bufferedWriter.write(persistenceContent);
                    bufferedWriter.close();

                    final URL url = file.toUri().toURL();
                    return java.util.Collections.enumeration(new HashSet<>(Arrays.asList(url)));
                }
                return super.findResources(name);
            }
        };
    }

    private Path getTempFile() throws IOException {
        final Path directory = Files.createTempDirectory("meta");
        directory.toFile().deleteOnExit();
        final Path metaDir = Files.createDirectory(directory.resolve("META-INF"));
        return Files.createFile(metaDir.resolve("persistence.xml"));
    }

    protected String generateXml() throws TransformerException, ParserConfigurationException {
        final Document doc = createDocument();
        final TransformerFactory transformerFactory = TransformerFactory.newInstance();
        final Transformer transformer = transformerFactory.newTransformer();
        final DOMSource source = new DOMSource(doc);
        final StringWriter writer = new StringWriter();
        final StreamResult result = new StreamResult(writer);
        transformer.transform(source, result);
        return writer.toString();
    }

    @Override
    public String toString() {
        try {
            return generateXml();
        } catch (TransformerException | ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }
}
