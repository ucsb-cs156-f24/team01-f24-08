package edu.ucsb.cs156.example.repositories;

import edu.ucsb.cs156.example.entities.UCSBOrgs;

import org.springframework.beans.propertyeditors.StringArrayPropertyEditor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * The UCSBOrgsRepository is a repository for UCSBOrgs entities
 */
@Repository
public interface UCSBOrgsRepository extends CrudRepository<UCSBOrgs, String> {
 
}