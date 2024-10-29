package com.automwrite.assessment.service.impl;

import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class DocumentProcessingService {

    @Value("${file.target-directory}")
    private String targetDirectory;

    public String extractTextFromDocument(XWPFDocument document) {
        StringBuilder text = new StringBuilder();
        for (XWPFParagraph paragraph : document.getParagraphs()) {
            text.append(paragraph.getText()).append("\n");
        }
        return text.toString();
    }

    public String buildPrompt(String toneText, String contentText) {
        return "Please adjust the tone of the following text to align with the tone of the sample text.\n\n"
                + "Sample tone:\n" + toneText + "\n\n"
                + "Rewrite this text in the sample tone:\n" + contentText + "\n\n"
                + "Ensure the rewritten text mirrors tone, style, and formality of the sample.";
    }

    public Path saveGeneratedDocument(String generatedText) throws IOException {
        // Prepare the output document with formatted text
        XWPFDocument outputDoc = new XWPFDocument();
        XWPFParagraph paragraph = outputDoc.createParagraph();
        paragraph.setAlignment(ParagraphAlignment.BOTH);
        paragraph.setSpacingBetween(1.5);

        for (String line : generatedText.split("\n")) {
            XWPFRun run = paragraph.createRun();
            run.setText(line);
            run.addCarriageReturn();
        }

        // Ensure directory exists
        Path targetDirPath = Paths.get(targetDirectory);
        Files.createDirectories(targetDirPath);

        // Generate unique file path and save document
        Path filePath = targetDirPath.resolve(UUID.randomUUID() + "_output_document.docx");
        try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
            outputDoc.write(fos);
        }
        outputDoc.close();

        return filePath;
    }

    // Construct the download link
    public String constructResponseJson(Path filePath) {
        String downloadLink = "/api/files/download/" + filePath.getFileName();
        return String.format(
                "{\"message\": \"File successfully uploaded, processing completed (or check in target folder).\", \"downloadLink\": \"%s\"} ",
                downloadLink
        );
    }
}
