package com.websiteElectronics.websiteElectronics.Controllers;

import com.websiteElectronics.websiteElectronics.Model.ManagersBehaviour;
import com.websiteElectronics.websiteElectronics.Repositories.ManagersBehaviourRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mongo/managersBehaviour")
public class ManagersBehaviourController {
    @Autowired
    ManagersBehaviourRepository managersBehaviourRepository;

    @PostMapping("/addNewBehaviour")
    public void addNewBehaviour(@RequestBody ManagersBehaviour behaviour) {
        managersBehaviourRepository.save(behaviour);
    }
}
