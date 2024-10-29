package com.automwrite.assessment.interceptor;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Objects;


@Component
public class FileUploadInterceptor implements HandlerInterceptor {

    private static final long MIN_FILE_SIZE = 1024; // 1 KB
    private static final String ALLOWED_FILE_TYPE = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Check if request is a multipart request
        if (request instanceof MultipartHttpServletRequest) {
            MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;

            // Retrieve files
            MultipartFile toneFile = multipartRequest.getFile("toneFile");
            MultipartFile contentFile = multipartRequest.getFile("contentFile");

            // Validate toneFile
            if (!isValidFile(toneFile, "Tone file", response)) {
                return false;
            }

            // Validate contentFile
            if (!isValidFile(contentFile, "Content file", response)) {
                return false;
            }
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Request must be a multipart request.");
            return false;
        }

        return true; // Proceed if valid
    }

    private boolean isValidFile(MultipartFile file, String fileName, HttpServletResponse response) throws IOException {
        if (file == null || file.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, fileName + " is missing or empty.");
            return false;
        }

        if (file.getSize() < MIN_FILE_SIZE) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, fileName + " is too small. Minimum size is " + MIN_FILE_SIZE + " bytes.");
            return false;
        }

       /* if (!Objects.equals(file.getContentType(), ALLOWED_FILE_TYPE)) {
            response.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, fileName + " must be of type " + ALLOWED_FILE_TYPE + ".");
            return false;
        }*/

        return true;
    }
}