package org.example;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDDocumentNameDictionary;
import org.apache.pdfbox.pdmodel.PDEmbeddedFilesNameTreeNode;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.common.filespecification.PDComplexFileSpecification;
import org.apache.pdfbox.pdmodel.common.filespecification.PDEmbeddedFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class Main {
    public InputStream getResourceAsStream(String resourcePath) {
        // Load the file from the resources folder
        return getClass().getClassLoader().getResourceAsStream(resourcePath);
    }

    public static void main(String[] args) {
        // Create a new PDF document
        try (PDDocument pdDocument = new PDDocument()) {
            // Add a blank page to the document (optional, but usually PDFs need at least one page)
            PDPage blankPage = new PDPage();
            pdDocument.addPage(blankPage);

            // Create the first attachment
            PDComplexFileSpecification fileSpec1 = new PDComplexFileSpecification();
            PDComplexFileSpecification fileSpec2 = new PDComplexFileSpecification();
            setEmbeddedFile(pdDocument, "file1.xml", fileSpec1);
            setEmbeddedFile(pdDocument, "file2.xml", fileSpec2);

            // Create a name tree for embedded files and add both file specifications
            PDDocumentCatalog catalog = pdDocument.getDocumentCatalog();
            PDEmbeddedFilesNameTreeNode efTree = new PDEmbeddedFilesNameTreeNode();

            Map<String, PDComplexFileSpecification> names = new HashMap<>();
            names.put("file1.xml", fileSpec1);
            names.put("file2.xml", fileSpec2);
            efTree.setNames(names);

            // Add the name tree to the document catalog's name dictionary
            PDDocumentNameDictionary namesDict = new PDDocumentNameDictionary(catalog);
            namesDict.setEmbeddedFiles(efTree);
            catalog.setNames(namesDict);

            setMetadata(pdDocument, catalog);

            // Save the document
            pdDocument.save("pdf_with_xml_attachments.pdf");

        } catch (Exception e) {
            System.out.print("Error: >>>>>> " + e.getMessage());
        }
    }
    
    private static void setEmbeddedFile(PDDocument pdDocument, String filename, PDComplexFileSpecification fileSpec) throws IOException {
        fileSpec.setFile(filename);

        Main loader = new Main();
        // Assuming your XML file is located in src/main/resources/file1.xml
        InputStream inputStream = loader.getResourceAsStream(filename);

        if (inputStream != null) {
            // Create a PDStream object to hold the file data
            PDStream pdStream = new PDStream(pdDocument, inputStream);
            // Create a PDEmbeddedFile from the PDStream
            PDEmbeddedFile embeddedFile = new PDEmbeddedFile(pdDocument, pdStream.createInputStream());
            // Set the embedded file in the file specification
            fileSpec.setEmbeddedFile(embeddedFile);
        } else {
            System.out.println("Error: File not found!");
        }
    }

    private static void setMetadata(PDDocument pdDocument, PDDocumentCatalog catalog) throws IOException {
        // Set general document metadata
        PDDocumentInformation info = pdDocument.getDocumentInformation();
        info.setTitle("Sample PDF/A-3 Document");
        info.setAuthor("sakunim");
        info.setSubject("Subject of the document");
        info.setKeywords("PDF/A-3, Sample");

        // Create XMP metadata for PDF/A-3 compliance
        String xmpMetadata = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<x:xmpmeta xmlns:x=\"adobe:ns:meta/\">" +
                "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">" +
                "<rdf:Description rdf:about=\"\">" +
                "<pdfaid:part xmlns:pdfaid=\"http://www.aiim.org/pdfa/ns/id/\">3</pdfaid:part>" +
                "<pdfaid:conformance xmlns:pdfaid=\"http://www.aiim.org/pdfa/ns/id/\">U</pdfaid:conformance>" +
                "</rdf:Description>" +
                "</rdf:RDF>" +
                "</x:xmpmeta>";

        // Add XMP metadata stream to the document catalog
        ByteArrayInputStream xmpInputStream = new ByteArrayInputStream(xmpMetadata.getBytes());
        PDMetadata metadata = new PDMetadata(pdDocument, xmpInputStream);
        catalog.setMetadata(metadata);
    }
}