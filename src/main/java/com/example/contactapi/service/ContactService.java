package com.example.contactapi.service;

import com.example.contactapi.domain.Contact;
import com.example.contactapi.repo.ContactRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLOutput;
import java.util.Optional;
import java.util.function.BiFunction;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.function.Function;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import static com.example.contactapi.constant.Constant.PHOTO_DIRECTORY;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@Transactional(rollbackOn = Exception.class)
@RequiredArgsConstructor
public class ContactService {
    private final ContactRepo contactRepo;


    public Page<Contact> getAllContacts(int page, int size){
        return contactRepo.findAll(PageRequest.of(page,size, Sort.by("name")));
    }


    public Contact getContact(String id) {
        return contactRepo.findContactById(id)
                .orElseThrow(() -> new RuntimeException("Contact not found"));
    }



    public Contact createContact(Contact contact){
        return contactRepo.save(contact);
    }


    public ResponseEntity<String > deleteContact(String id) {
        Contact contact= getContact(id);
        if (contact == null) {
            return ResponseEntity.notFound().build();
        }
        try {
            contactRepo.delete(contact);
            String url=contact.getPhotoUrl();
            int lastIndex = url.lastIndexOf('/');
            String filename = url.substring(lastIndex + 1);

            Path path = Paths.get(PHOTO_DIRECTORY+filename);

            Files.delete(path);

            return ResponseEntity.ok().build();
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting contact: " + e.getMessage());
        }
    }


    public String uploadPhoto(String id, MultipartFile file) {
        log.info("Saving picture for user ID: {}", id);
        System.out.println(id);
        Contact contact = getContact(id);
        String photoUrl = photoFunction.apply(id, file);
        contact.setPhotoUrl(photoUrl);
        contactRepo.save(contact);
        return photoUrl;
    }


    private final Function<String ,String> fileExtenstion= filename -> Optional.of(filename).filter(name->name.contains("."))
            .map(name->"."+name.substring(filename.lastIndexOf(".")+1)).orElse("png");


    private final BiFunction<String, MultipartFile, String> photoFunction = (id, image) -> {
        String filename=id+fileExtenstion.apply(image.getOriginalFilename());
        try {
            Path fileStorageLocation = Paths.get(PHOTO_DIRECTORY).toAbsolutePath().normalize();
            if (!Files.isDirectory(fileStorageLocation)) {
                Files.createDirectories(fileStorageLocation);
            }
            Files.copy(image.getInputStream(), fileStorageLocation.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
            return ServletUriComponentsBuilder
                    .fromCurrentContextPath()
                    .path("/contacts/image/"+id+fileExtenstion
                    .apply(image.getOriginalFilename()))
                    .toUriString();

        } catch (Exception exception) {
            throw new RuntimeException("Unable to save image", exception);
        }
    };
}
