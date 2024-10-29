package com.automwrite.assessment.controller;

import com.automwrite.assessment.Application;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
class ControllerTest {
    @Autowired
    private MockMvc mockMvc;


    @Test
    public void convertTone_ShouldReturnSuccessResponse_WhenFilesAreValid() throws Exception {
        // Load original files from resources
        MockMultipartFile toneFile = createMockMultipartFile("testFiles/automwrite - A - Casual tone.docx");
        MockMultipartFile contentFile = createMockMultipartFile("testFiles/automwrite - B - Formal tone.docx");

        // Perform the request
        // Perform the request with files as key-value pairs
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/convert-tone")
                        .file("toneFile", toneFile.getBytes()) // Setting the key as "toneFile"
                        .file("contentFile", contentFile.getBytes()) // Setting the key as "contentFile"
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("File successfully uploaded, processing completed (or check in target folder)."))
                .andExpect(jsonPath("$.downloadLink").exists());
    }

    @Test
    public void convertTone_ShouldReturnBadRequest_WhenToneFileIsMissing() throws Exception {
        MockMultipartFile contentFile = createMockMultipartFile("testFiles/automwrite - B - Formal tone.docx");

        // Perform the request without toneFile
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/convert-tone")
                        .file("contentFile", contentFile.getBytes())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());

    }

    @Test
    public void convertTone_ShouldReturnBadRequest_WhenContentFileIsMissing() throws Exception {
        MockMultipartFile toneFile = createMockMultipartFile("testFiles/automwrite - A - Casual tone.docx");

        // Perform the request without contentFile
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/convert-tone")
                        .file("toneFile", toneFile.getBytes())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());

    }

    private MockMultipartFile createMockMultipartFile(String filePath) throws IOException {
        // Load the file from resources
        File file = new ClassPathResource(filePath).getFile();
        return new MockMultipartFile(
                file.getName(),
                file.getName(),
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                new FileInputStream(file)
        );
    }

}