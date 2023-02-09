package com.mai.Kindergarten.controllers.manager;


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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Optional;

//Класс для обработки страниц пользователя(воспитателя) для работы с дипломами
@Controller
public class ManagerPanelManualController {
    @Autowired
    UserRepository userRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private ManualRepository manualRepository;
    @Autowired
    private TalkManualRepository talkManualRepository;
    @Autowired
    private GameManualRepository gameManualRepository;
    @Autowired
    private EducationDirectionRepository educationDirectionRepository;

    @GetMapping("/manager/typeMaterial")
    public String typeMaterialList(Model model) {
        return "manager/manual/typeMaterial";
    }

    @GetMapping("/manager/allManuals")
    public String manualsList(Model model) {
        Iterable<Manual> allManuals = manualRepository.findAll();
        model.addAttribute("allManuals", allManuals);
        return "manager/manual/allManuals";
    }

    @GetMapping("/manager/addManual")
    public String addManual(Model model) {
        Iterable<EducationDirection> allEducationDirections = educationDirectionRepository.findAll();
        model.addAttribute("allEducationDirections", allEducationDirections);
        return "manager/manual/addManual";
    }


    @PostMapping("/manager/addManual")
    public String addManual(@RequestParam int inputTypeMaterial,@RequestParam String inputName,
                            @RequestParam(required = false) String inputType, @RequestParam(required = false) String inputForm,
                            @RequestParam(required = false) String inputTypeGame,
                            @RequestParam Long inputEducationDirection,
                            @RequestParam("inputFileField") MultipartFile file, Model model) throws IOException {
        EducationDirection educationDirection = educationDirectionRepository.findById(inputEducationDirection).orElseThrow();
        switch (inputTypeMaterial){
            case 0 :
                System.out.println("Сохраняем лекцию");
                Manual manual = new Manual(inputName, inputType, inputForm, file.getBytes(), educationDirection, file.getOriginalFilename());
                manualRepository.save(manual);
                break;
            case 1 :
                System.out.println("Сохраняем беседу");
                TalkManual talkManual = new TalkManual(inputName, file.getBytes(), file.getOriginalFilename(), educationDirection);
                talkManualRepository.save(talkManual);
                break;
            case 2 :
                System.out.println("Сохраняем игру");
                GameManual gameManual = new GameManual(inputName, inputTypeGame, file.getBytes(), file.getOriginalFilename(), educationDirection);
                gameManualRepository.save(gameManual);
                break;
        }
        return "redirect:/manager/typeMaterial";
    }

    @GetMapping("/manager/allManuals/manual-details/{id}")
    public String manualDetails(@PathVariable(value = "id") long id, Model model) throws UnsupportedEncodingException {
        if (!manualRepository.existsById(id)) {
            return "redirect:/manager/allManuals";
        }
        Optional<Manual> manual = manualRepository.findById(id);
        ArrayList<Manual> res = new ArrayList<>();
        manual.ifPresent(res::add);
        model.addAttribute("manual", res);
        return "manager/manual/manual-details";
    }

    @PostMapping("/manager/allManuals/manual-details/{id}/remove")
    public String delete_manual(@PathVariable(value = "id") long id, Model model) {
        Manual manual = manualRepository.findById(id).orElseThrow();
        manualRepository.delete(manual);
        return "redirect:/manager/allManuals";
    }

    @GetMapping("/manager/allManuals/manual-details/{id}/download")
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

    @GetMapping("/manager/editManual/{id}")
    public String manualEdit(@PathVariable(value = "id") long id, Model model) {
        if (!manualRepository.existsById(id)) {
            return "redirect:/manual/allManuals";
        }
        Manual manual = manualRepository.findById(id).orElseThrow();
        model.addAttribute("manual", manual);
        Iterable<EducationDirection> allEducationDirections = educationDirectionRepository.findAll();
        model.addAttribute("allEducationDirections", allEducationDirections);
        return "manager/manual/editManual";
    }

    @PostMapping("/manager/editManual/{id}")
    public String editManual(@PathVariable(value = "id") long id,
                             @RequestParam String inputName, @RequestParam String inputType,
                             @RequestParam String inputForm, @RequestParam Long inputEducationDirection,
                             @RequestParam("inputFileField") MultipartFile file, Model model) throws IOException {
        Manual manual = manualRepository.findById(id).orElseThrow();
        manual.setName(inputName);
        manual.setType(inputType);
        manual.setForm(inputForm);
        EducationDirection educationDirection = educationDirectionRepository.findById(inputEducationDirection).orElseThrow();
        manual.setEducationDirection(educationDirection);
        if (!file.isEmpty()) {
            manual.setFile(file.getBytes());
            manual.setFileName(file.getOriginalFilename());
        }
        manualRepository.save(manual);
        return "redirect:/manager/allManuals/manual-details/{id}";
    }
}
