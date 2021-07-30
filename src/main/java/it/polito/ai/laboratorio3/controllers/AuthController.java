package it.polito.ai.laboratorio3.controllers;

import it.polito.ai.laboratorio3.dtos.UserDTO;
import it.polito.ai.laboratorio3.entities.User;
import it.polito.ai.laboratorio3.repositories.UserRepository;
import it.polito.ai.laboratorio3.security.AuthenticationRequest;
import it.polito.ai.laboratorio3.security.JwtTokenProvider;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    JwtTokenProvider jwtTokenProvider;
    @Autowired
    UserRepository users;
    @Autowired
    ModelMapper modelMapper;
    @PostMapping("/signin")
    public ResponseEntity signin(@RequestBody AuthenticationRequest data) {
        try {
            String[] matricola = data.getUsername().split("@");
            String username = matricola[0];
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, data.getPassword()));
            String token = jwtTokenProvider.createToken(username, this.users.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("Username " + username + "not found")).getRoles());
            Map<Object, Object> model = new HashMap<>();
            User user = users.getOne(username);
            //UserDTO dto = modelMapper.map(user, UserDTO.class);
            model.put("username", user.getUsername());
            model.put("role",user.getRoles());
            model.put("token", token);
            return ok(model);
        } catch (AuthenticationException e) {
            throw new BadCredentialsException("Invalid username/password supplied");
        }
    }
}
