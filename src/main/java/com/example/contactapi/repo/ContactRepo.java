package com.example.contactapi.repo;

import com.example.contactapi.domain.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContactRepo extends JpaRepository<Contact,String> {


   @Query("SELECT c FROM Contact c WHERE c.id = ?1")
   Optional<Contact> findContactById(String id);
}
