package com.websiteElectronics.websiteElectronics.Controllers;

import com.websiteElectronics.websiteElectronics.Model.CustomersBehaviour;
import com.websiteElectronics.websiteElectronics.Repositories.CustomersBehaviourRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CustomersBehaviourController {

    @Autowired
    CustomersBehaviourRepository customersBehaviourRepository;

    @PostMapping("/addNewBehaviour")
    public void addNewBehaviour(@RequestBody CustomersBehaviour behaviour) {
        customersBehaviourRepository.save(behaviour);
    }

}
