package com.fincen.sar.service;

import com.fincen.sar.exception.SarValidationException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

@Service
public class FincenSchemaValidationService {

    private static final String SCHEMA_ROOT = "fincen-schema/";
    private volatile Schema schema;

    public void validate(String xml) {
        try {
            Validator validator = schema().newValidator();
            validator.validate(new StreamSource(new StringReader(xml)));
        } catch (SAXException | IOException ex) {
            throw new SarValidationException("Generated XML does not conform to the FinCEN SARX schema: "
                    + ex.getMessage());
        }
    }

    private Schema schema() {
        Schema local = schema;
        if (local != null) {
            return local;
        }

        synchronized (this) {
            if (schema == null) {
                schema = loadSchema();
            }
            return schema;
        }
    }

    private Schema loadSchema() {
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            factory.setResourceResolver(new ClasspathSchemaResolver());
            return factory.newSchema(new StreamSource(
                    new ClassPathResource(SCHEMA_ROOT + "SARXBatchSchema.xsd").getInputStream()));
        } catch (SAXException | IOException ex) {
            throw new IllegalStateException("Failed to load FinCEN SARX schema resources", ex);
        }
    }

    private static final class ClasspathSchemaResolver implements LSResourceResolver {

        @Override
        public LSInput resolveResource(String type, String namespaceURI, String publicId,
                                       String systemId, String baseURI) {
            String resourceName = mapResource(systemId);
            if (resourceName == null) {
                return null;
            }

            try {
                ClassPathResource resource = new ClassPathResource(SCHEMA_ROOT + resourceName);
                return new SimpleLsInput(publicId, systemId,
                        new StringReader(new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8)));
            } catch (IOException ex) {
                throw new IllegalStateException("Unable to resolve schema resource " + resourceName, ex);
            }
        }

        private String mapResource(String systemId) {
            if (systemId == null) {
                return null;
            }
            if (systemId.endsWith("SARXBatchSchema.xsd")) {
                return "SARXBatchSchema.xsd";
            }
            if (systemId.endsWith("BSA_XML_2.0.xsd")) {
                return "BSA_XML_2.0.xsd";
            }
            if (systemId.endsWith("IC-ISM.xsd")) {
                return "IC-ISM.xsd";
            }
            if (systemId.endsWith("/codes") || systemId.endsWith("codes.xsd") || systemId.contains("fincen.gov/codes")) {
                return "codes.xsd";
            }
            return null;
        }
    }

    private static final class SimpleLsInput implements LSInput {
        private final String publicId;
        private final String systemId;
        private Reader characterStream;

        private SimpleLsInput(String publicId, String systemId, Reader characterStream) {
            this.publicId = publicId;
            this.systemId = systemId;
            this.characterStream = characterStream;
        }

        @Override public Reader getCharacterStream() { return characterStream; }
        @Override public void setCharacterStream(Reader characterStream) { this.characterStream = characterStream; }
        @Override public java.io.InputStream getByteStream() { return null; }
        @Override public void setByteStream(java.io.InputStream byteStream) { }
        @Override public String getStringData() { return null; }
        @Override public void setStringData(String stringData) { }
        @Override public String getSystemId() { return systemId; }
        @Override public void setSystemId(String systemId) { }
        @Override public String getPublicId() { return publicId; }
        @Override public void setPublicId(String publicId) { }
        @Override public String getBaseURI() { return null; }
        @Override public void setBaseURI(String baseURI) { }
        @Override public String getEncoding() { return StandardCharsets.UTF_8.name(); }
        @Override public void setEncoding(String encoding) { }
        @Override public boolean getCertifiedText() { return true; }
        @Override public void setCertifiedText(boolean certifiedText) { }
    }
}