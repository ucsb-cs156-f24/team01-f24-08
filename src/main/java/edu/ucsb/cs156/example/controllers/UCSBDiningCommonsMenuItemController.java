package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.UCSBDate;
import edu.ucsb.cs156.example.entities.UCSBDiningCommonsMenuItem;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.UCSBDateRepository;
import edu.ucsb.cs156.example.repositories.UCSBDiningCommonsMenuItemRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import java.time.LocalDateTime;


@Tag(name = "UCSBDiningCommonsMenuItem")
@RequestMapping("/api/ucsbdiningcommonsmenuitem")
@RestController
@Slf4j
public class UCSBDiningCommonsMenuItemController extends ApiController{
    @Autowired
    UCSBDiningCommonsMenuItemRepository ucsbDiningCommonsMenuItemRepository;

    /**
     * Get all records in the table and return as a JSON array
     * 
     * @return an iterable of UCSBDiningCommonsMenuItem
     */
    @Operation(summary= "Get all records in the table and return as a JSON array")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<UCSBDiningCommonsMenuItem> allUCSBDiningCommonsMenuItem() {
        Iterable<UCSBDiningCommonsMenuItem> records = ucsbDiningCommonsMenuItemRepository.findAll();
        return records;
    }

    /**
     * Use the data in the input parameters to create a new row in the table and return the data as JSON
     */
    @Operation(summary= "Create a new row in the table")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public UCSBDiningCommonsMenuItem postUcsbDiningCommonsMenuItem(
            @Parameter(name="diningCommonsCode") @RequestParam String diningCommonsCode,
            @Parameter(name="name") @RequestParam String name,
            @Parameter(name="station") @RequestParam String station) 
            throws JsonProcessingException {

        UCSBDiningCommonsMenuItem ucsbDiningCommonsMenuItem = new UCSBDiningCommonsMenuItem();
        ucsbDiningCommonsMenuItem.setDiningCommonsCode(diningCommonsCode);
        ucsbDiningCommonsMenuItem.setName(name);
        ucsbDiningCommonsMenuItem.setStation(station);

        UCSBDiningCommonsMenuItem savedUCSBDiningCommonsMenuItem = ucsbDiningCommonsMenuItemRepository.save(ucsbDiningCommonsMenuItem);

        return savedUCSBDiningCommonsMenuItem;
    }

    /**
     * Delete a single record from the table; use the value passed in as a @RequestParam to do a lookup by id. If a matching row is not found, throw an EntityNotFoundException.
     */
    @Operation(summary= "Delete a UCSBDiningCommonsMenuItem by id")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteUCSBDate(
            @Parameter(name="id") @RequestParam Long id) {
        UCSBDiningCommonsMenuItem ucsbDiningCommonsMenuItem = ucsbDiningCommonsMenuItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(UCSBDiningCommonsMenuItem.class, id));

        ucsbDiningCommonsMenuItemRepository.delete(ucsbDiningCommonsMenuItem);
        return genericMessage("UCSBDiningCommonsMenuItem with id %s deleted".formatted(id));
    }

    /**
     * Get a single record from the table; use the value passed in 
     * as a @RequestParam to do a lookup by id. 
     * If a matching row is found, update that row 
     * with the values passed in as a JSON object. 
     * If a matching row is not found, throw an EntityNotFoundException.
     */
    @Operation(summary= "Update a Dining Commons Menu Item")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public UCSBDiningCommonsMenuItem updateUCSBDiningCommonsMenuItem(
            @Parameter(name="id") @RequestParam Long id,
            @RequestBody @Valid UCSBDiningCommonsMenuItem incoming) {

        UCSBDiningCommonsMenuItem ucsbDCMI = ucsbDiningCommonsMenuItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(UCSBDiningCommonsMenuItem.class, id));

        ucsbDCMI.setDiningCommonsCode((incoming.getDiningCommonsCode()));
        ucsbDCMI.setName(incoming.getName());
        ucsbDCMI.setStation(incoming.getStation());  

        ucsbDiningCommonsMenuItemRepository.save(ucsbDCMI);

        return ucsbDCMI;
    }

    /**
     * Get a single record from the table; use the value passed in
     *  as a @RequestParam to do a lookup by id. 
     * If a matching row is found, return the row as a JSON object, 
     * otherwise throw an EntityNotFoundException.
     */
    @Operation(summary= "Get a single UCSBDiningCommonsMenuItem by id")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public UCSBDiningCommonsMenuItem getById(
            @Parameter(name="id") @RequestParam Long id) {
        UCSBDiningCommonsMenuItem ucsbDCMI = ucsbDiningCommonsMenuItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(UCSBDiningCommonsMenuItem.class, id));

        return ucsbDCMI;
    }
}
