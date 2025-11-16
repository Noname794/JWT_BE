package com.websiteElectronics.websiteElectronics.Controllers;

import com.websiteElectronics.websiteElectronics.Dtos.Oauth2ClientsDto;
import com.websiteElectronics.websiteElectronics.Services.Oauth2ClientsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/oauth2-clients")
public class Oauth2ClientsController {

    @Autowired
    private Oauth2ClientsService oauth2ClientsService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Oauth2ClientsDto>> getAllClients() {
        List<Oauth2ClientsDto> clients = oauth2ClientsService.getAllClients();
        return ResponseEntity.ok(clients);
    }

    @GetMapping("/{clientId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Oauth2ClientsDto> getClientByClientId(@PathVariable String clientId) {
        try {
            Oauth2ClientsDto client = oauth2ClientsService.getClientByClientId(clientId);
            return ResponseEntity.ok(client);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createClient(@RequestBody Oauth2ClientsDto clientDto) {
        try {
            Oauth2ClientsDto createdClient = oauth2ClientsService.createClient(clientDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdClient);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{clientId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateClient(
            @PathVariable String clientId,
            @RequestBody Oauth2ClientsDto clientDto) {
        try {
            Oauth2ClientsDto updatedClient = oauth2ClientsService.updateClient(clientId, clientDto);
            return ResponseEntity.ok(updatedClient);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{clientId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteClient(@PathVariable String clientId) {
        try {
            oauth2ClientsService.deleteClient(clientId);
            return ResponseEntity.ok("Client deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
