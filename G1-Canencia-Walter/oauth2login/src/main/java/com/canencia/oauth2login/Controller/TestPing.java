package com.canencia.oauth2login.Controller;

import com.canencia.oauth2login.Service.GoogleContactsService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



@RestController
@RequestMapping("/test")
public class TestPing{

    private final GoogleContactsService googleContactsService;

    public TestPing(GoogleContactsService googleContactsService) {
        this.googleContactsService = googleContactsService;
    }


    @GetMapping("/ping")
    public String ping() {
        System.out.println("Ping endpoint hit!");
        return "pong";
    }

    @GetMapping("/contacts")
    public String listContacts(Model model, OAuth2AuthenticationToken authentication) {
        List<Map<String, Object>> connections = googleContactsService.getAllContacts(authentication);
        List<Map<String, String>> contacts = new ArrayList<>();

        for (Map<String, Object> person : connections) {
            String resourceName = person.get("resourceName").toString();

            // ✅ Skip Google Profiles (only user-created contacts are deletable)
            if (!resourceName.startsWith("people/c")) {
                continue;
            }

            Map<String, String> contact = new HashMap<>();
            contact.put("id", resourceName);
            contact.put("name", ((List<Map<String, String>>) person.get("names")).get(0).get("displayName"));
            contact.put("email", person.containsKey("emailAddresses") ? ((List<Map<String, String>>) person.get("emailAddresses")).get(0).get("value") : "N/A");
            contact.put("phone", person.containsKey("phoneNumbers") ? ((List<Map<String, String>>) person.get("phoneNumbers")).get(0).get("value") : "N/A");
            contacts.add(contact);
        }


        model.addAttribute("contacts", contacts);
        return "contacts";  // Ensure you have contacts.html
    }

    @GetMapping("/contacts/edit-contact/{id}")
    public String editContact(@PathVariable String id, Model model, OAuth2AuthenticationToken authentication) {
        System.out.println("📌 Editing Contact ID: " + id);

        Map<String, Object> contactData = googleContactsService.getContactById(authentication, id);
        if (contactData == null) {
            System.out.println("❌ Contact Not Found!");
            return "redirect:/contacts"; // Redirect to contacts list if not found
        }

        // Extract data safely
        String firstName = contactData.containsKey("names")
                ? ((Map<String, String>) ((List<?>) contactData.get("names")).get(0)).get("givenName")
                : "";
        String lastName = contactData.containsKey("names")
                ? ((Map<String, String>) ((List<?>) contactData.get("names")).get(0)).getOrDefault("familyName", "")
                : "";

        String email = contactData.containsKey("emailAddresses")
                ? ((Map<String, String>) ((List<?>) contactData.get("emailAddresses")).get(0)).get("value")
                : "";

        String phone = contactData.containsKey("phoneNumbers")
                ? ((Map<String, String>) ((List<?>) contactData.get("phoneNumbers")).get(0)).get("value")
                : "";

        // Add contact data to the model
        model.addAttribute("contact", Map.of(
                "id", id,
                "firstName", firstName,
                "lastName", lastName,
                "email", email,
                "phone", phone
        ));

        return "edit-contact"; // Ensure edit-contact.html exists
    }


}

