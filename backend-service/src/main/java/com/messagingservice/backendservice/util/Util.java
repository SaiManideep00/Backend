package com.messagingservice.backendservice.util;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class Util {
    public static ResponseEntity prepareResponse(Object response, HttpStatus status){
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.APPLICATION_JSON);
        return new ResponseEntity(response, responseHeaders, status);
    }

    public static ResponseEntity<Object> prepareErrorResponse(String code, String msg, HttpStatus status) {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.APPLICATION_JSON);
        Map<String, String> error = new HashMap<String,String>();
        error.put("Code",code);
        error.put("message",msg);
        return new ResponseEntity(error,responseHeaders, status);
    }

    public static File getFileFromResource(String fileName) throws URISyntaxException {
        ClassLoader classLoader = Util.class.getClassLoader();
        URL resource = classLoader.getResource(fileName);
        if (resource == null) {
            throw new IllegalArgumentException("file not found! " + fileName);
        } else {
            // failed if files have whitespaces or special characters
            //return new File(resource.getFile());
            return new File(resource.toURI());
        }
    }
}
