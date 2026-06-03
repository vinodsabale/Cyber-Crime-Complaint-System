package com.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import lombok.extern.slf4j.Slf4j;

@ControllerAdvice(basePackages = "com.controller.mvc")
@Slf4j
public class MvcExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ModelAndView handleError(Exception ex) {
        log.error("MVC Error: ", ex);
        ModelAndView mav = new ModelAndView("error/500");
        mav.addObject("message", ex.getMessage());
        return mav;
    }
}