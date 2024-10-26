package edu.ucsb.cs156.example.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import edu.ucsb.cs156.example.entities.UCSBDiningCommonsMenuItem;

@Repository
public interface UCSBDiningCommonsMenuItemRepository extends CrudRepository<UCSBDiningCommonsMenuItem, Long> {
    
    Iterable<UCSBDiningCommonsMenuItem> findAllByDiningCommonsCode(String diningCommonsCode);
}
