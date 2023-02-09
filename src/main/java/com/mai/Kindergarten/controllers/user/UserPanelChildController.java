package com.mai.Kindergarten.controllers.user;

import com.mai.Kindergarten.models.*;
import com.mai.Kindergarten.repo.*;
import com.mai.Kindergarten.service.TemplateCreator;
import com.mai.Kindergarten.service.Transcriptor;
import com.mai.Kindergarten.service.UserService;
import net.lingala.zip4j.io.outputstream.ZipOutputStream;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.AesKeyStrength;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.CompressionMethod;
import net.lingala.zip4j.model.enums.EncryptionMethod;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

//Класс для обработки страниц пользователя(воспитателя) для работы с детьми
@Controller
public class UserPanelChildController {
    @Autowired
    UserRepository userRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private ChildRepository childRepository;
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private DiplomaRepository diplomaRepository;

    @Autowired
    private IndividualSessionRepository individualSessionRepository;
    @Autowired
    private ResultMonitoringRepository resultMonitoringRepository;

    @GetMapping("/user/allChildren")
    public String childrenList(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepository.findByUsername(auth.getName());
        List<Child> allChildren = new ArrayList<>();
        if (currentUser.getGroup() != null) {
            allChildren = currentUser.getGroup().getChildren();
        }
        model.addAttribute("allChildren", allChildren);
        return "user/child/allChildren";
    }

    @GetMapping("/user/addChild")
    public String addChild(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepository.findByUsername(auth.getName());
        Group group = currentUser.getGroup();
        Date minDate = new Date();
        minDate.setMonth(0);
        minDate.setDate(1);
        minDate.setHours(0);
        minDate.setMinutes(0);
        minDate.setSeconds(0);

        Date maxDate = new Date();
        maxDate.setMonth(11);
        maxDate.setDate(31);
        maxDate.setHours(23);
        maxDate.setMinutes(59);
        maxDate.setSeconds(59);
        if (currentUser.getGroup() != null) {
            AgeGroup ageGroup = group.getAgeGroup();
            int maxAge = Integer.parseInt(ageGroup.getChildrenMaxAge());
            int minAge = Integer.parseInt(ageGroup.getChildrenMinAge());
            minDate.setYear(minDate.getYear() - maxAge);
            maxDate.setYear(maxDate.getYear() - minAge);
        } else {
            AgeGroup ageGroup = group.getAgeGroup();
            int maxAge = Integer.parseInt(ageGroup.getChildrenMaxAge());
            minDate.setYear(minDate.getYear() - 8);
            maxDate.setYear(maxDate.getYear() - 1);
        }
        System.out.println("minDate = " + minDate);
        System.out.println("maxDate = " + maxDate);
        model.addAttribute("minDate", minDate);
        model.addAttribute("maxDate", maxDate);
        return "user/child/addChild";
    }

    @PostMapping("/user/addChild")
    public String addChild(@RequestParam String inputLastName, @RequestParam String inputFirstName,
                           @RequestParam String inputPatronymicName, @RequestParam String StrBirthday, Model model) {
        SimpleDateFormat simpleDateFormat_inner = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date date = null;
        try {
            date = simpleDateFormat_inner.parse(StrBirthday);
        } catch (ParseException e) {

        }
        Child child = new Child(inputLastName, inputFirstName, inputPatronymicName, date);
        childRepository.save(child);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepository.findByUsername(auth.getName());
        Group currentGroup = currentUser.getGroup();
        currentGroup.getChildren().add(child);
        groupRepository.save(currentGroup);

        return "redirect:/user/allChildren";
    }

    @GetMapping("/user/allChildren/child-details/{id}")
    public String childDetails(@PathVariable(value = "id") long id, Model model) {
        if (!childRepository.existsById(id)) {
            return "redirect:/user/allChildren";
        }
//        Optional<Child> child = childRepository.findById(id);
//        ArrayList<Child> res = new ArrayList<>();
//        child.ifPresent(res::add);
//        model.addAttribute("child", res);
        Child child = childRepository.findById(id).orElseThrow();
        model.addAttribute("child", child);

        Set<EducationArea> allEducationAreas = new HashSet<>();
        for (ResultMonitoring resultMonitoring : child.getResultMonitors()) {
            allEducationAreas.add(resultMonitoring.getEducationDirection().getEducationArea());
        }
        model.addAttribute("allEducationAreas", allEducationAreas);
        System.out.println(allEducationAreas);

        List<Diploma> allDiplomas = diplomaRepository.findAll().stream().filter(x -> x.getChild() != null).filter(x -> x.getChild().getId() == child.getId()).collect(Collectors.toList());
        model.addAttribute("allDiplomas", allDiplomas);
        return "user/child/child-details";
    }

    @PostMapping("/user/allChildren/child-details/{id}/remove")
    public String delete_child(@PathVariable(value = "id") long id, Model model) {
        Child child = childRepository.findById(id).orElseThrow();
        List<IndividualSession> allIndividualSessions = individualSessionRepository.findByChild(child);
        List<ResultMonitoring> allResultMonitorings = resultMonitoringRepository.findByChild(child);
        List<Diploma> allDiplomas = diplomaRepository.findByChild(child);
        System.out.println("Для удаления!!!");
        for(IndividualSession individualSession : allIndividualSessions){
            individualSessionRepository.delete(individualSession);
        }
        for(ResultMonitoring resultMonitoring : allResultMonitorings){
            resultMonitoringRepository.delete(resultMonitoring);
        }
        for(Diploma diploma : allDiplomas){
            diplomaRepository.delete(diploma);
        }

        childRepository.delete(child);
        return "redirect:/user/allChildren";
    }

    @GetMapping("/user/editChild/{id}")
    public String childEdit(@PathVariable(value = "id") long id, Model model) {
        if (!childRepository.existsById(id)) {
            return "redirect:/user/allChildren";
        }
        Child child = childRepository.findById(id).orElseThrow();
        model.addAttribute("child", child);
        return "user/child/editChild";
    }

    @PostMapping("/user/editChild/{id}")
    public String childEdit(@PathVariable(value = "id") long id, @RequestParam String inputLastName, @RequestParam String inputFirstName,
                            @RequestParam String inputPatronymicName, @RequestParam String StrBirthday, Model model) {
        Child child = childRepository.findById(id).orElseThrow();
        child.setLastName(inputLastName);
        child.setFirstName(inputFirstName);
        child.setPatronymicName(inputPatronymicName);
        SimpleDateFormat simpleDateFormat_inner = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date date = null;
        try {
            date = simpleDateFormat_inner.parse(StrBirthday);
        } catch (ParseException e) {

        }
        child.setDateOfBirth(date);
        childRepository.save(child);
        return "redirect:/user/allChildren/child-details/{id}";
    }


    @GetMapping("/user/allChildren/{child_id}/report")
    public ResponseEntity<Object> report(@PathVariable(value = "child_id") long child_id,
                                         Model model, HttpServletRequest request) throws IOException, Docx4JException, JAXBException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepository.findByUsername(auth.getName());
        Child child = childRepository.findById(child_id).orElseThrow();
        List<Diploma> allDiplomas = diplomaRepository.findAll().stream().filter(x -> x.getChild() != null).filter(x -> x.getChild().getId() == child.getId()).collect(Collectors.toList());
        Date date = new Date();
        Group group = groupRepository.findById(childRepository.findByChildId(child_id)).orElseThrow();
        String groupFullName = group.getGroupName() + " (" + group.getAgeGroup().getName() + ")";

        //Формируем документ
        WordprocessingMLPackage wordDocument = TemplateCreator.getTemplate("Templates reports\\Report_6.docx");
        SimpleDateFormat simpleDateFormat_out = new SimpleDateFormat("«dd» MMMM yyyy");
        String strDate = simpleDateFormat_out.format(date);
        TemplateCreator.replacePlaceholder(wordDocument, strDate, "date");
        TemplateCreator.replacePlaceholder(wordDocument, currentUser.getFullName(), "userFullName");
        TemplateCreator.replacePlaceholder(wordDocument, child.getFullName(), "childFullName");
        TemplateCreator.replacePlaceholder(wordDocument, groupFullName, "groupName");
        TemplateCreator.addChildDiplomaRow(wordDocument, allDiplomas, "num");


        ZipParameters zipParameters = new ZipParameters();
        zipParameters.setCompressionMethod(CompressionMethod.DEFLATE);
        zipParameters.setCompressionLevel(CompressionLevel.NORMAL);
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        ZipOutputStream zout = new ZipOutputStream(bo);
        for(Diploma diploma : allDiplomas){
                zipParameters.setFileNameInZip(diploma.getFileName());
                zout.putNextEntry(zipParameters);
                zout.write(diploma.getFile());
                zout.closeEntry();
        }

        simpleDateFormat_out = new SimpleDateFormat("ddMMyyyy-HHmmss", Locale.getDefault());
        strDate = simpleDateFormat_out.format(date);
        String fileName = "report" + "-" + strDate + ".docx";
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        wordDocument.save(outputStream);

        zipParameters.setFileNameInZip(fileName);
        zout.putNextEntry(zipParameters);
        zout.write(outputStream.toByteArray());
        zout.closeEntry();
        zout.close();

        InputStreamResource resource  = new InputStreamResource(new ByteArrayInputStream(bo.toByteArray()));
        HttpHeaders headers = new HttpHeaders();
        String nameFileDownload = Transcriptor.transliterate(child.getFullName());
        nameFileDownload += ".zip";
        headers.add("Content-Disposition", String.format("attachment; filename=\"%s\"", nameFileDownload));
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        ResponseEntity<Object>
                responseEntity = ResponseEntity.ok().headers(headers).contentLength(bo.toByteArray().length).contentType(
                MediaType.parseMediaType("application/txt")).body(resource);

//        return TemplateCreator.downloadReport(wordDocument, "Report");
        return responseEntity;
    }


}
