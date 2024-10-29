package com.automwrite.assessment.controller;

import com.automwrite.assessment.config.AsyncConfig;
import com.automwrite.assessment.customAnnotations.ValidFile;
import com.automwrite.assessment.exceptions.InvalidFileFormatException;
import com.automwrite.assessment.service.LlmService;
import com.automwrite.assessment.service.impl.DocumentProcessingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Slf4j
@RestController
@RequestMapping("/api")
@AllArgsConstructor
@Validated
public class Controller {

    private final LlmService llmService;
    private final ObjectMapper objectMapper;
    private final DocumentProcessingService documentProcessingService;
    private final AsyncConfig asyncConfig;

    /**
     * You should extract the tone from the `toneFile` and update the `contentFile` to convey the same content
     * but using the extracted tone.
     *
     * @param toneFile    File to extract the tone from
     * @param contentFile File to apply the tone to
     * @return A response indicating that the processing has completed
     */
    @PostMapping("/convert-tone")
    public ResponseEntity<String> convertTone(@RequestParam
                                              @NotNull(message = "Tone file is required")
                                              @ValidFile(minSize = 1024, allowedTypes = {"application/vnd.openxmlformats-officedocument.wordprocessingml.document"})
                                              MultipartFile toneFile,

                                              @RequestParam
                                              @NotNull(message = "Tone file is required")
                                              @ValidFile(minSize = 1024, allowedTypes = {"application/vnd.openxmlformats-officedocument.wordprocessingml.document"})
                                              MultipartFile contentFile) throws IOException {
        try {
            log.info("Received tone conversion request. Processing mode: {}", asyncConfig.getMode());

            String processingMode = asyncConfig.getMode();

            // Process the files based on the mode specified
            if ("asynchronous".equalsIgnoreCase(processingMode)) {
                // Call async processing
                processFiles(toneFile, contentFile, processingMode);
                return ResponseEntity.accepted().body("Processing started asynchronously."); // Immediate response
            } else {
                // Call sync processing
                String responseJson = processFiles(toneFile, contentFile, processingMode);
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(responseJson);
            }
        } catch (InvalidFileFormatException e) {
            log.error("Invalid file format: {}", e.getMessage());
            String errorResponse = objectMapper.writeValueAsString(Map.of("error", "Invalid file format."));
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (IOException e) {
            log.error("File processing error: {}", e.getMessage());
            String errorResponse = objectMapper.writeValueAsString(Map.of("error", "File processing error."));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);

        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage());
            String errorResponse = objectMapper.writeValueAsString(Map.of("error", "An unexpected error occurred."));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }

    }

    private String processFiles(MultipartFile toneFile, MultipartFile contentFile, String processingMode) throws IOException, ExecutionException, InterruptedException {
        // Load documents
        XWPFDocument toneDocument = new XWPFDocument(toneFile.getInputStream());
        XWPFDocument contentDocument = new XWPFDocument(contentFile.getInputStream());

        // Extract text from both documents
        String toneText = documentProcessingService.extractTextFromDocument(toneDocument);
        String contentText = documentProcessingService.extractTextFromDocument(contentDocument);

        // Build prompt for LLM
        String prompt = documentProcessingService.buildPrompt(toneText, contentText);

        // Call the appropriate LLM service method based on processing mode
        String generatedText;
        if ("asynchronous".equalsIgnoreCase(processingMode)) {
            // Use asynchronous method for processing
            generatedText = llmService.generateTextAsync(prompt).get(); // Blocking call, handle properly in production
        } else {
            // Synchronous call
            generatedText = llmService.generateText(prompt);
        }

        // Save generated document and create response
        Path outputFile = documentProcessingService.saveGeneratedDocument(generatedText);
        return documentProcessingService.constructResponseJson(outputFile);
    }

}

