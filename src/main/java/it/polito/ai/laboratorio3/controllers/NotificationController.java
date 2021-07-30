package it.polito.ai.laboratorio3.controllers;

import it.polito.ai.laboratorio3.entities.User;
import it.polito.ai.laboratorio3.security.JwtTokenProvider;
import it.polito.ai.laboratorio3.services.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.ResponseEntity.ok;


@Controller
@RequestMapping("/API/notification")
public class NotificationController {
    @Autowired
    NotificationService notificationService;
    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @GetMapping("/confirm/{token}")
    public String acceptRequest(@PathVariable String token){
       try {
           notificationService.confirm(token);
           return "tokenAcceptedTemplate.html";
       }
       catch (Exception e) {
           return "errorTemplate.html";
       }
    }

    @GetMapping("/reject/{token}")
    public String rejectRequest(@PathVariable String token){

        try {
            notificationService.reject(token);
            return "tokenRejectedTemplate.html";
        }
        catch (Exception e) {
            return "errorTemplate.html";
        }
    }

    @GetMapping("confirmRegistration/{token}")
    public String acceptRegistration(@PathVariable String token){

        try {
            notificationService.confirmRegistration(token);
            return "registrationAcceptedTemplate.html";
        }
        catch (Exception e) {
            return "errorTemplate.html";
        }
    }
}
