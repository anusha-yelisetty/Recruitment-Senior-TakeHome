package com.automwrite.assessment.customAnnotations;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

public class FileValidator implements ConstraintValidator<ValidFile, MultipartFile> {

    private long minSize;
    private String[] allowedTypes;

    @Override
    public void initialize(ValidFile constraintAnnotation) {
        this.minSize = constraintAnnotation.minSize();
        this.allowedTypes = constraintAnnotation.allowedTypes();
    }

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        // Check if file is null or empty
        if (file == null || file.isEmpty()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("File must not be empty").addConstraintViolation();
            return false;
        }

        // Check file size
        if (file.getSize() < minSize) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("File is too small").addConstraintViolation();
            return false;
        }

        // Check allowed file types
        if (allowedTypes.length > 0) {
            String fileType = file.getContentType();
            boolean isValidType = false;

            for (String allowedType : allowedTypes) {
                if (allowedType.equalsIgnoreCase(fileType)) {
                    isValidType = true;
                    break;
                }
            }

            if (!isValidType) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Invalid file type").addConstraintViolation();
                return false;
            }
        }

        return true;
    }
}

