package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.UCSBDiningCommons;
import edu.ucsb.cs156.example.entities.UCSBOrgs;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.UCSBDiningCommonsRepository;
import edu.ucsb.cs156.example.repositories.UCSBOrgsRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
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

/**
 * This is a REST controller for UCSBOrganization
 */

 @Tag(name = "UCSBOrganization")
 @RequestMapping("/api/ucsborganization")
 @RestController
 @Slf4j

public class UCSBOrganizationController extends ApiController  {
    @Autowired
    UCSBOrgsRepository ucsbOrgsRepository;

    /**
     * THis method returns a list of all ucsborgs.
     * @return a list of all ucsborgs
     */
    @Operation(summary= "List all ucsb organizations")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<UCSBOrgs> allOrganizations() {
        Iterable<UCSBOrgs> orgs = ucsbOrgsRepository.findAll();
        return orgs;
    }

    /**
     * This method creates a new organization. Accessible only to users with the role "ROLE_ADMIN".
     * @param orgCode 
     * @param orgTranslationShort 
     * @param orgTranslation 
     * @param inactive 
     * @return the save ucsb orgs
     */
    @Operation(summary= "Create a new organization")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public UCSBOrgs postOrganizations(
        @Parameter(name="orgCode") @RequestParam String orgCode,
        @Parameter(name="orgTranslationShort") @RequestParam String orgTranslationShort,
        @Parameter(name="orgTranslation") @RequestParam String orgTranslation,
        @Parameter(name="inactive") @RequestParam boolean inactive
        )
        {

        UCSBOrgs orgs = new UCSBOrgs();
        orgs.setOrgCode(orgCode);
        orgs.setOrgTranslationShort(orgTranslationShort);
        orgs.setOrgTranslation(orgTranslation);
        orgs.setInactive(inactive);

        UCSBOrgs savedOrgs = ucsbOrgsRepository.save(orgs);

        return savedOrgs;
    }

    /**
     * This method returns a single ucsborganization.
     * @param orgCode code of the ucsborganization
     * @return a single ucsborganization
     */
    @Operation(summary= "Get a single organization")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public UCSBOrgs getById( @Parameter(name="orgCode") @RequestParam String orgCode) {
        UCSBOrgs organization = ucsbOrgsRepository.findById(orgCode)
        .orElseThrow(() -> new EntityNotFoundException(UCSBOrgs.class, orgCode));
        return organization;
    }

    /**
     * Delete an organization. Accessible only to users with the role "ROLE_ADMIN".
     * 
     * @param orgCode code of the organization
     * @return a message indiciating the organization was deleted
     */
    @Operation(summary = "Delete a UCSBOrg")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteOrganization(
            @Parameter(name = "orgCode") @RequestParam String orgCode) {

        UCSBOrgs organization = ucsbOrgsRepository.findById(orgCode)
        .orElseThrow(() -> new EntityNotFoundException(UCSBOrgs.class, orgCode));

        ucsbOrgsRepository.delete(organization);
        return genericMessage("UCSBOrgs with id %s deleted".formatted(orgCode));
    }
  
     * Update a single ucsborg. Accessible only to users with the role "ROLE_ADMIN".
     * @param orgCode code of the diningcommons
     * @param incoming the new commons contents
     * @return the updated commons object
     */
    @Operation(summary= "Update a single ucsb organization")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public UCSBOrgs updateOrgs(
            @Parameter(name="orgCode") @RequestParam String orgCode,
            @RequestBody @Valid UCSBOrgs incoming) {

        UCSBOrgs org = ucsbOrgsRepository.findById(orgCode)
                .orElseThrow(() -> new EntityNotFoundException(UCSBOrgs.class, orgCode));


        org.setOrgTranslationShort(incoming.getOrgTranslationShort());
        org.setOrgTranslation(incoming.getOrgTranslation());
        org.setInactive(incoming.getInactive());

        ucsbOrgsRepository.save(org);

        return org;
    }
}
