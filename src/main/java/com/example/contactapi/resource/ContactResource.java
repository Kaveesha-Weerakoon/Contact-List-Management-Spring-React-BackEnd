package com.example.contactapi.resource;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;
import com.example.contactapi.domain.Contact;
import com.example.contactapi.service.ContactService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.example.contactapi.constant.Constant.PHOTO_DIRECTORY;
import static org.springframework.util.MimeTypeUtils.IMAGE_GIF_VALUE;
import static org.springframework.util.MimeTypeUtils.IMAGE_PNG_VALUE;

@RestController
@RequestMapping("/contacts")
@RequiredArgsConstructor
@CrossOrigin("http://localhost:3000")
public class ContactResource {
    private final ContactService contactService;
    Logger logger;
    @PostMapping
    public ResponseEntity<Contact> createContact(@RequestBody Contact contact){
        return ResponseEntity.created(URI.create("contact/userID")).body(contactService.createContact(contact));
    }

    @GetMapping
    public ResponseEntity<Page<Contact>> getContacts(@RequestParam(value="page",defaultValue = "0") int page,
                                                     @RequestParam(value="size",defaultValue = "10") int size){
        return ResponseEntity.ok().body(contactService.getAllContacts(page,size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Contact> getContacts(@PathVariable(value="id") String id){
        return ResponseEntity.ok().body(contactService.getContact(id));
    }

    @PutMapping("/photo")
    public ResponseEntity<String> uploadPhoto(@RequestParam("id") String id, @RequestParam("file")MultipartFile file){
        
        return ResponseEntity.ok().body(contactService.uploadPhoto(id,file));
    }

    @GetMapping(value = "/image/{filename}",produces = {IMAGE_PNG_VALUE,IMAGE_GIF_VALUE})
    public byte[] getphoto(@PathVariable("filename") String filename) throws IOException{
        return Files.readAllBytes(Paths.get(PHOTO_DIRECTORY +filename));
    }

    @DeleteMapping
    public ResponseEntity<String> deleteContact(@RequestParam("id") String id){
        return  contactService.deleteContact(id);
    }




}
