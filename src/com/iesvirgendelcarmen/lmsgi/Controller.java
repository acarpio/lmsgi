/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iesvirgendelcarmen.lmsgi;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.WhitespaceStrippingPolicy;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * @author antonio
 */
public class Controller {

    private File file;
    private File fileDTD;
    private File fileXSD;
    private File fileXSLT;  // para hojas de transformación
    private File fileHTML;  // para salida HTML en hojas de transformación

    public Controller() {
        this.file = null;
        this.fileDTD = null;
        this.fileXSD = null;
        this.fileXSLT = null;
        this.fileHTML = null;
    }

    public Controller(File myfile) {
        this.file = myfile;
        this.fileDTD = null;
        this.fileXSD = null;
        this.fileXSLT = null;
        this.fileHTML = null;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public File getFileDTD() {
        return fileDTD;
    }

    public void setFileDTD(File fileDTD) {
        this.fileDTD = fileDTD;
    }

    public File getFileXSD() {
        return fileXSD;
    }

    public void setFileXSD(File fileXSD) {
        this.fileXSD = fileXSD;
    }

    public File getFileXSLT() {
        return fileXSLT;
    }

    public void setFileXSLT(File fileXSLT) {
        this.fileXSLT = fileXSLT;
    }

    public File getFileHTML() {
        return fileHTML;
    }

    public void setFileHTML(File fileHTML) {
        this.fileHTML = fileHTML;
    }

    // método para evaluar la consulta XPath
    public String xPathEvaluate(String stringXPath) {   // le pasamos la consulta XPath como argumento
        String resultado = "";
        try {
            Processor proc = new Processor(false);
            // creamos un DocumentBuilder
            DocumentBuilder builder = proc.newDocumentBuilder();
            builder.setLineNumbering(true);
            builder.setWhitespaceStrippingPolicy(WhitespaceStrippingPolicy.ALL); // borra los espacios en blanco
            // creamos un documentoXML utilizando el DocumentBuilder
            XdmNode documentoXML = builder.build(file);
            // creamos un compilador de XPath
            XPathCompiler xpath = proc.newXPathCompiler();
            // evaluamos la consulta con el selector
            XPathSelector selector = xpath.compile(stringXPath).load();
            selector.setContextItem(documentoXML);
            XdmValue evaluate = selector.evaluate();
            // en este objeto evaluate tenemos todos los resultados del XPath
            // ahora lo pasamos a String
            for (XdmItem item : evaluate) {
                // y cada item lo vamos añadiendo al resultado
                resultado += item.getStringValue() + "\n";
            }
        } catch (SaxonApiException ex) {
            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
            // resultado +=  ex.getLocalizedMessage();
        }
        return resultado;
    }

    /*
    public String validar() {
        String resultado = "Procesando fichero " + file.getPath().toString();
        try {
            DomUtil.parse(file.getPath(), true);
            resultado += "\nFichero Procesado";
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
            // resultado += ex.getLocalizedMessage()+"\n";
        }
        return resultado;
    }
    
    public String validar() {
        String resultado = "Procesando fichero " + file.getPath().toString();
        try {
            DomUtil.parse(file, true);
            resultado += "\nFichero Procesado";
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
            // resultado += ex.getLocalizedMessage()+"\n";
        }
        return resultado;
    }
     */
    public String validateDTD() {
        String resultado = "";
        try {
            DomUtil.parse(file, true);
            resultado += "Validación DTD correcta";
            Document doc = DomUtil.parse(this.file, true);
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            resultado += ex.getLocalizedMessage();
        }
        return resultado;
    }

    /*
    public String validateXSD() {
        String resultado = "";
        try {
            Document doc = DomUtil.parseXSD(this.file);
            resultado += "Validación XSD correcta";
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            resultado += ex.getLocalizedMessage();
        }
        return resultado;
    }
     */
    public String validateXSD() {
        String resultado = "";
        try {
            Document doc = DomUtil.parseXSD(this.file, this.fileXSD);
            resultado += "Validación XSD correcta";
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            resultado += ex.getLocalizedMessage();
        }
        return resultado;
    }

    public String xsltTransform() {
        String resultado = "Transformación completada correctamente";
        if (this.file != null && this.fileXSLT != null && this.fileHTML != null) {
            try {
                Processor proc = new Processor(false);
                XsltCompiler comp = proc.newXsltCompiler();
                XsltExecutable exp = comp.compile(new StreamSource(this.fileXSLT));
                XdmNode source = proc.newDocumentBuilder().build(new StreamSource(this.file));
                Serializer out = proc.newSerializer(this.fileHTML);
                out.setOutputProperty(Serializer.Property.METHOD, "html");
                out.setOutputProperty(Serializer.Property.INDENT, "yes");
                XsltTransformer trans = exp.load();
                trans.setInitialContextNode(source);
                trans.setDestination(out);
                trans.transform();
                resultado = new String(Files.readAllBytes(this.fileHTML.toPath()));
            } catch (IOException | SaxonApiException ex) {
                resultado = ex.getLocalizedMessage();
            }
        } else {
            resultado = "Error procesando ficheros";
        }
        return resultado;
    }

    private boolean save2File(File fichero, String contenido) {
        boolean resultado = true;
        if (fichero != null && fichero.isFile() && contenido != null) {
            try (FileWriter fw = new FileWriter(fichero)) {
                fw.write(contenido);
                fw.flush();
            } catch (IOException ex) {
                resultado = false;
            }
        } else {
            resultado = false;
        }
        return resultado;
    }

    public boolean save2XML(String contenido) {
        return save2File(this.file, contenido);
    }

    public boolean save2XSL(String contenido) {
        return save2File(this.fileXSLT, contenido);
    }
}
