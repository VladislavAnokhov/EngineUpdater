package anokhov.EngineUpdater.example;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;


import anokhov.EngineUpdater.facility.RegNumber;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.io.File;

@Component
public class NexiPreset extends CustomPreset {
    private static final String PRESET_LABEL = "nexi";
    private  int DEFAULT_OBJS_PER_FILE = 20;

    private String regNumberPrefix;
    private int idCounter = 50;
    private int fileCount = 1;
    private List<NexiObject> nexiObjects = new ArrayList<>();

    @Value("${default.regNumberPrefix:defaultPrefix}") // значение по умолчанию
    public void setRegNumberPrefix(String regNumberPrefix) {
        this.regNumberPrefix = regNumberPrefix;
    }
    public void setDEFAULT_OBJS_PER_FILE(int maxObjsPerFile){
        DEFAULT_OBJS_PER_FILE=maxObjsPerFile;
    }
    public int getDEFAULT_OBJS_PER_FILE(){
        return DEFAULT_OBJS_PER_FILE;
    }

    public String getRegNumberPrefix() {
        return regNumberPrefix;
    }

    public String getPreset() {
        return PRESET_LABEL;
    }

    @Override
    public CustomObject generateObject(String[] preset, String[] columns) {
        NexiObject obj = new NexiObject(this.getRegNumberPrefix());
        obj.populate(preset, columns);
        return obj;
    }

    @Override
    public void addObj(CustomObject finalObj) {
        nexiObjects.add((NexiObject) finalObj);
    }

    @Override
    public void writeXml() throws ParserConfigurationException, TransformerException {
        if (nexiObjects.isEmpty()) {
            System.out.println("LIST EMPTIED, PROGRAM CLOSING");
            System.exit(0);
        }

        Configuration cfg = new Configuration(Configuration.VERSION_2_3_0);
        cfg.setClassForTemplateLoading(this.getClass(), "/templates");
        Template template;

        try {
            template = cfg.getTemplate("template.ftl");
        } catch (IOException e) {
            System.out.println("Error with template: " + e.getMessage());
            throw new RuntimeException(e);
        }

        int ind = 0;
        System.out.println("===============================ВЫЗОВ ЗАПИСИ=================================================");

        while (ind < nexiObjects.size()) {
            System.out.println("начало выполнения кода");
            List<Map<String, String>> data = new ArrayList<>();

            for (int i = ind; i < ind + DEFAULT_OBJS_PER_FILE && i < nexiObjects.size(); i++) {
                NexiObject obj = nexiObjects.get(i);
                Map<String, String> record = new HashMap<>();
                record.put("REG_NUMBER", RegNumber.getRegNumber(obj.getRegNumberPrefix()));
                record.put("N_PAYM", obj.getPaymentContractId());
                record.put("IBAN", obj.getProductCode()); // Add the correct IBAN value here
                record.put("CONTACT_CODE", obj.getRegNumberPrefix()); // Add the correct Contact Code value here
                data.add(record);
            }

            Map<String, Object> templateData = new HashMap<>();
            templateData.put("creationDate", LocalDate.now().toString());
            templateData.put("creationTime", LocalTime.now().truncatedTo(ChronoUnit.SECONDS).toString());
            templateData.put("data", data);
            System.out.println("начало записи файлов . Куда пишет: "+destination);
         //   System.out.println("название файла: "+ destination + RegNumber.getRegNumber("result") + ".xml");
            File outputFile = new File(destination+"\\" + RegNumber.getRegNumber("result") + ".xml");
            try (Writer fileWriter = new FileWriter(outputFile)) {
                template.process(templateData, fileWriter);
            } catch (IOException | TemplateException e) {
                System.out.println("Error writing XML file: " + e.getMessage());
                e.printStackTrace();
            }
            System.out.println("конец записи файла");
            ind += DEFAULT_OBJS_PER_FILE;

        }
        clearNexiObjects(ind);
        /*if (!nexiObjects.isEmpty()) {
            writeXml();
        }*/

     /*   DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document doc = documentBuilder.newDocument();

        Element applicationFile = addElementInNewElement(doc, "ApplicationFile", createFileHeader(doc));

        Element applicationsList = doc.createElement("ApplicationsList");

        int ind = 0;

        for (int i = 0; i < nexiObjects.size(); i++) {
            if (i >= DEFAULT_OBJS_PER_FILE) break;
            ind = i + 1;
            NexiObject obj = nexiObjects.get(i);
            Element application = createApplicationElement(doc, obj);
            application.appendChild(createSubAppListElement(doc, obj));
            applicationsList.appendChild(application);
        }

        applicationFile.appendChild(applicationsList);
        doc.appendChild(applicationFile);

        createXmlFile(doc);

        clearNexiObjects(ind);
        if (!nexiObjects.isEmpty()) writeXml();*/
    }

    /*private Element createFileHeader(Document doc) {
        Element fileHeader = doc.createElement("FileHeader");

        createElementWithTextContent(doc, fileHeader, "FormatVersion", "2.0");
        createElementWithTextContent(doc, fileHeader, "Sender", "NESFDC");
        createElementWithTextContent(doc, fileHeader, "CreationDate", LocalDate.now().toString());
        createElementWithTextContent(doc, fileHeader, "CreationTime", LocalTime.now().truncatedTo(ChronoUnit.SECONDS).toString());
        createElementWithTextContent(doc, fileHeader, "Number", "99993");
        createElementWithTextContent(doc, fileHeader, "Institution", "0002");

        return fileHeader;
    }

    private Element createApplicationElement(Document doc, NexiObject obj) {
        Element application = doc.createElement("Application");

        createElementWithTextContent(doc, application, "RegNumber", RegNumber.getRegNumber(obj.getRegNumberPrefix()));
        createElementWithTextContent(doc, application, "Institution", "0002");
        createElementWithTextContent(doc, application, "InstitutionIDType", "Branch");
        createElementWithTextContent(doc, application, "OrderDprt", "0102");
        createElementWithTextContent(doc, application, "ObjectType", "Contract");
        createElementWithTextContent(doc, application, "ActionType", "Update");
        createElementWithAddElement(doc, application, "ObjectFor", addContractIDT(doc, obj));

        if(obj.getProductCode() != null && !obj.getProductCode().isEmpty())
            createElementWithAddElement(doc, application, "Data", addContract(doc, obj));

        return application;
    }

    private Element createSubAppListElement(Document doc, NexiObject obj) {
        Element application = doc.createElement("Application");

        createElementWithTextContent(doc, application, "RegNumber", RegNumber.getRegNumber(obj.getRegNumberPrefix()));
        createElementWithTextContent(doc, application, "ObjectType", "ContractParameter");
        createElementWithTextContent(doc, application, "ActionType", "AddOrUpdate");
        createElementWithAddElement(doc, application, "Data", addCustomerParameters(doc, obj));

        return addElementInNewElement(doc, "SubApplList", application);
    }

    private Element addCustomerParameters(Document doc, NexiObject obj) {
        Element customerParameters = doc.createElement("CustomerParameters");

        for (int j = 0; j < obj.getParams().size(); j++) {
            Element parameter = doc.createElement("Parameter");
            createElementWithTextContent(doc, parameter, "Code", obj.getParams().get(j));
            createElementWithTextContent(doc, parameter, "Value", obj.getValues().get(j));
            customerParameters.appendChild(parameter);
        }

        return customerParameters;
    }

    private Element addContractIDT(Document doc, NexiObject obj) {
        Element contractIDT = doc.createElement("ContractIDT");

        createElementWithTextContent(doc, contractIDT, "ContractNumber", obj.getPaymentContractId());

        return contractIDT;
    }

    private Element addContract(Document doc, NexiObject obj) {
        Element product = doc.createElement("Product");

        createElementWithTextContent(doc, product, "ProductCode1", obj.getProductCode());

        return addElementInNewElement(doc, "Contract", product);
    }

    private void createElementWithTextContent(Document doc, Element parent, String elementName, String textContent) {
        Element element = doc.createElement(elementName);
        element.setTextContent(textContent);
        parent.appendChild(element);
    }

    private void createElementWithAddElement(Document doc, Element parent, String elementName, Element element) {
        Element newElement = doc.createElement(elementName);
        newElement.appendChild(element);
        parent.appendChild(newElement);
    }

    private Element addElementInNewElement(Document doc, String parentName, Element element) {
        Element newElement = doc.createElement(parentName);
        newElement.appendChild(element);
        return newElement;
    }

    private void createXmlFile(Document doc) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        DOMSource source = new DOMSource(doc);

        StreamResult result = new StreamResult(new File(destination + "result" + fileCount++ + ".xml"));

        transformer.transform(source, result);
    }*/

    private void clearNexiObjects(int ind) {
        if (ind > 0 && ind <= nexiObjects.size()) {
            nexiObjects.subList(0, ind).clear();
        }
    }

}

class NexiObject extends CustomObject {

    private String regNumberPrefix;
    private String paymentContractId = "";
    private String productCode = "";
    private List<String> params = new ArrayList<>();
    private List<String> values = new ArrayList<>();

    public NexiObject(String regNumberPrefix) {
        this.regNumberPrefix = regNumberPrefix;
    }

    public String getRegNumberPrefix() {
        return regNumberPrefix;
    }

    public String getPaymentContractId() {
        return paymentContractId;
    }

    public String getProductCode() {
        return productCode;
    }

    public List<String> getParams() {
        return params;
    }

    public List<String> getValues() {
        return values;
    }

    public void populate(String[] preset, String[] columns) {
        paymentContractId = columns[0];
        productCode = columns[1];

        for (int i = 2; i < preset.length && i + 1 < columns.length; i += 2) {
            params.add(columns[i]);
            values.add(columns[i +1]); //+1
        }
    }

    @Override
    public String toString() {
        return regNumberPrefix + " " + getParams() + " " + getValues();
    }
}
