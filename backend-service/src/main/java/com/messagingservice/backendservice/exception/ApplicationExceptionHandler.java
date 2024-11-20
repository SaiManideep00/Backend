package com.messagingservice.backendservice.exception;

import com.microsoft.sqlserver.jdbc.SQLServerException;
import org.hibernate.PropertyValueException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ApplicationExceptionHandler extends ResponseEntityExceptionHandler
{
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public Map<String, String> handleSqlUniqueConstraintException(SQLIntegrityConstraintViolationException ex){
        Map<String, String> errorMap = new HashMap<String, String>();
        errorMap.put("error", ex.getMessage());
        return errorMap;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(SQLServerException.class)
    public Map<String, String> handleSqlServerException(SQLServerException ex){
        Map<String, String> errorMap = new HashMap<String, String>();
        errorMap.put("error", ex.getMessage());
        return errorMap;
    }

    //DataIntegrityViolationException
//    @ResponseStatus(HttpStatus.BAD_REQUEST)
//    @ExceptionHandler(DataIntegrityViolationException.class)
//    public Map<String, String> handleDataIntegrityViolationException(DataIntegrityViolationException ex){
//        Map<String, String> errorMap = new HashMap<String, String>();
//        errorMap.put("error", ex.getMessage().split(";")[0]);
//        return errorMap;
//    }

//    @ResponseStatus(HttpStatus.BAD_REQUEST)
//    @ExceptionHandler(SqlExceptionHelper.class)
//    public Map<String, String> handleDataIntegrityViolationException(DataIntegrityViolationException ex){
//        Map<String, String> errorMap = new HashMap<String, String>();
//        errorMap.put("error", ex.getMessage().split(";")[0]);
//        return errorMap;
//    }


    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(PropertyValueException.class)
    public Map<String, String> handlePropertyValueException(PropertyValueException ex){
        Map<String, String> errorMap = new HashMap<String, String>();
        errorMap.put("error", ex.getMessage());
        return errorMap;
    }

//    @ExceptionHandler(Exception.class)
//    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
//    public ErrorMessage globalExceptionHandler(Exception ex, WebRequest request) {
//        ErrorMessage message = new ErrorMessage(
//                HttpStatus.INTERNAL_SERVER_ERROR.value(),
//                new Date(),
//                ex.getMessage(),
//                request.getDescription(true));
//        return message;
//    }


}
