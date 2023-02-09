package com.mai.Kindergarten.controllers.user;

import com.mai.Kindergarten.models.*;
import com.mai.Kindergarten.repo.*;
import com.mai.Kindergarten.service.Transcriptor;
import com.mai.Kindergarten.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.ByteArrayInputStream;

//Класс для обработки страниц пользователя(воспитателя) для работы с дипломами
@Controller
public class UserPanelMaterialsController {
    @Autowired
    UserRepository userRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private ManualRepository manualRepository;
    @Autowired
    private EducationDirectionRepository educationDirectionRepository;
    @Autowired
    private AgeGroupRepository ageGroupRepository;
    @Autowired
    private EducationAreaRepository educationAreaRepository;
    @Autowired
    private TalkManualRepository talkManualRepository;
    @Autowired
    private GameManualRepository gameManualRepository;

    @GetMapping("/user/allMaterials")
    public String manualsList(Model model) {
        Iterable<Manual> allManuals = manualRepository.findAll();
        model.addAttribute("allManuals", allManuals);

        Iterable<EducationDirection> allEducationDirections = educationDirectionRepository.findAll();
        model.addAttribute("allEducationDirections", allEducationDirections);

        Iterable<AgeGroup> allAgeGroups = ageGroupRepository.findAll();
        model.addAttribute("allAgeGroups", allAgeGroups);

        Iterable<EducationArea> allEducationAreas = educationAreaRepository.findAll();
        model.addAttribute("allEducationAreas", allEducationAreas);

        Iterable<TalkManual> allTalkManuals = talkManualRepository.findAll();
        model.addAttribute("allTalkManuals", allTalkManuals);

        Iterable<GameManual> allGameManuals = gameManualRepository.findAll();
        model.addAttribute("allGameManuals", allGameManuals);

        return "user/manual/allMaterials";
    }

//    @GetMapping("/user/allManuals")
//    public String manualsList(Model model) {
//        Iterable<Manual> allManuals = manualRepository.findAll();
//        model.addAttribute("allManuals", allManuals);
//
//        Iterable<EducationDirection> allEducationDirections = educationDirectionRepository.findAll();
//        model.addAttribute("allEducationDirections", allEducationDirections);
//
//        Iterable<AgeGroup> allAgeGroups = ageGroupRepository.findAll();
//        model.addAttribute("allAgeGroups", allAgeGroups);
//
//        Iterable<EducationArea> allEducationAreas = educationAreaRepository.findAll();
//        model.addAttribute("allEducationAreas", allEducationAreas);
//
//        return "user/manual/allManuals";
//    }

    @GetMapping("/user/allManuals/{id}/download")
    public ResponseEntity<Object> downloadFile(@PathVariable(value = "id") Long id, Model model) throws Exception {
        Manual manual = manualRepository.findById(id).orElseThrow();
        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(manual.getFile()));
        HttpHeaders headers = new HttpHeaders();
        String nameFileDownload = Transcriptor.transliterate(manual.getFileName());
        //Либо так оставляем либо имя файла в латиницу переводим
        headers.add("Content-Disposition", String.format("attachment; filename=\"%s\"", nameFileDownload));
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        ResponseEntity<Object>
                responseEntity = ResponseEntity.ok().headers(headers).contentLength(manual.getFile().length).contentType(
                MediaType.parseMediaType("application/all")).body(resource);
        return responseEntity;
    }

    @GetMapping("/user/allTalkManuals/{id}/download")
    public ResponseEntity<Object> downloadFile_Talk(@PathVariable(value = "id") Long id, Model model) throws Exception {
        TalkManual talkManual = talkManualRepository.findById(id).orElseThrow();
        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(talkManual.getFile()));
        HttpHeaders headers = new HttpHeaders();
        String nameFileDownload = Transcriptor.transliterate(talkManual.getFileName());
        //Либо так оставляем либо имя файла в латиницу переводим
        headers.add("Content-Disposition", String.format("attachment; filename=\"%s\"", nameFileDownload));
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        ResponseEntity<Object>
                responseEntity = ResponseEntity.ok().headers(headers).contentLength(talkManual.getFile().length).contentType(
                MediaType.parseMediaType("application/all")).body(resource);
        return responseEntity;
    }

    @GetMapping("/user/allGameManuals/{id}/download")
    public ResponseEntity<Object> downloadFile_Game(@PathVariable(value = "id") Long id, Model model) throws Exception {
        GameManual gameManual = gameManualRepository.findById(id).orElseThrow();
        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(gameManual.getFile()));
        HttpHeaders headers = new HttpHeaders();
        String nameFileDownload = Transcriptor.transliterate(gameManual.getFileName());
        //Либо так оставляем либо имя файла в латиницу переводим
        headers.add("Content-Disposition", String.format("attachment; filename=\"%s\"", nameFileDownload));
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        ResponseEntity<Object>
                responseEntity = ResponseEntity.ok().headers(headers).contentLength(gameManual.getFile().length).contentType(
                MediaType.parseMediaType("application/all")).body(resource);
        return responseEntity;
    }
}
