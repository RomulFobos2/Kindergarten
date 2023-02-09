package com.mai.Kindergarten.service;

import com.mai.Kindergarten.models.*;
import org.docx4j.XmlUtils;
import org.docx4j.dml.CTBlip;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.*;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import java.io.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class TemplateCreator {
    public final static String DIR_IN = System.getProperty("user.dir") + "/";
    public final static String DIR_OUT = System.getProperty("user.dir") + "/";

    public static List<String> listAllHighLvlStart = new ArrayList<>();
    public static List<String> listAllMidLvlStart = new ArrayList<>();
    public static List<String> listAllLowLvlStart = new ArrayList<>();

    public static List<String> listAllHighLvlEnd = new ArrayList<>();
    public static List<String> listAllMidLvlEnd = new ArrayList<>();
    public static List<String> listAllLowLvlEnd = new ArrayList<>();

    public static WordprocessingMLPackage getTemplate(String name) throws Docx4JException, FileNotFoundException {
        WordprocessingMLPackage template = WordprocessingMLPackage.load(new FileInputStream(new File(name)));
        return template;
    }

    public static List<Object> getAllElementFromObject(Object obj, Class<?> toSearch) {
        List<Object> result = new ArrayList<Object>();
        if (obj instanceof JAXBElement) obj = ((JAXBElement<?>) obj).getValue();

        if (obj.getClass().equals(toSearch))
            result.add(obj);
        else if (obj instanceof ContentAccessor) {
            List<?> children = ((ContentAccessor) obj).getContent();
            for (Object child : children) {
                result.addAll(getAllElementFromObject(child, toSearch));
            }
        }
        return result;
    }

    public static void replacePlaceholder(WordprocessingMLPackage template, String name, String placeholder) {
        List<Object> texts = getAllElementFromObject(template.getMainDocumentPart(), Text.class);
        for (Object text : texts) {
            Text textElement = (Text) text;
            if (textElement.getValue().equals(placeholder)) {
                System.out.println("Поменяли placeholder - " + placeholder + " на  - " + name);
                textElement.setValue(name);
                break; //Не забываем
            }
        }
    }

    public static void replacePlaceholderInTR(Tr tr, String name, String placeholder) {
        List<Object> texts = getAllElementFromObject(tr, Text.class);
        for (Object text : texts) {
            Text textElement = (Text) text;
            if (textElement.getValue().equals(placeholder)) {
                textElement.setValue(name);
                break;
            }
        }
    }

//    public static void replacePlaceholderInTR(Tr tr, String name, String time, String placeholder) {
//        List<Object> texts = getAllElementFromObject(tr, Text.class);
//        for (Object text : texts) {
//            Text textElement = (Text) text;
//            if (textElement.getValue().equals(placeholder)) {
//                textElement.setValue(name);
//                break;
//            }
//        }
//        for (Object text : texts) {
//            Text textElement = (Text) text;
//            if (textElement.getValue().equals("time")) {
//                textElement.setValue(time);
//                break;
//            }
//        }
//    }


    public static void addChildRow(WordprocessingMLPackage template, List<Child> allChildren, String placeholder, EducationDirection educationDirection, boolean isStarEducationYear) throws JAXBException, Docx4JException {
        List<Object> tables = getAllElementFromObject(template.getMainDocumentPart(), Tbl.class);
        //Поиск таблицы
        Tbl tempTable = getTemplateTable(tables, placeholder);
        List<Object> rows = getAllElementFromObject(tempTable, Tr.class);
        Tr templateRow = (Tr) rows.get(2);
        for (Child child : allChildren) {
            //addRowToTable(tempTable, templateRow, child.getFullName());
            if (isStarEducationYear) {
                addRowToTable_1(tempTable, templateRow, child.getFullName(), child, educationDirection);
            } else {
                addRowToTable_2(tempTable, templateRow, child.getFullName(), child, educationDirection);
            }
        }
        //Удаляем шаблонную строку
        tempTable.getContent().remove(templateRow);
    }

    public static void addQuestionRow(WordprocessingMLPackage template, List<EducationIndicator> allEducationIndicators, String placeholder) throws JAXBException, Docx4JException {
        List<Object> texts = getAllElementFromObject(template.getMainDocumentPart(), Text.class);
        for (EducationIndicator educationIndicator : allEducationIndicators) {
            for (Object text : texts) {
                Text textElement = (Text) text;
                if (textElement.getValue().equals(placeholder)) {
                    textElement.setValue(educationIndicator.getQuestion());
                    break;
                }
            }
        }
    }

    public static void addQuestionColumn(WordprocessingMLPackage template, List<EducationIndicator> allQuestions, String placeholder) throws JAXBException, Docx4JException, CloneNotSupportedException {
        List<Object> tables = getAllElementFromObject(template.getMainDocumentPart(), Tbl.class);
        //Поиск таблицы
        Tbl tempTable = getTemplateTable(tables, placeholder);
        List<Object> rows = getAllElementFromObject(tempTable, Tr.class);

        //Получаем все ячейки (элемент Tc) и добавляем их в строки - таким образом формируем столбцы.
        //Повторяем это такое кол-во раз, какое у нас кол0во вопросов
        List<Object> allTC = getAllElementFromObject(template.getMainDocumentPart(), Tc.class);
        for (int i = 1; i < allQuestions.size(); i++) {
            ((Tr) rows.get(0)).getContent().add(Foo.createTC_question()); //вставляем question
            ((Tr) rows.get(1)).getContent().add(Foo.createTC_startYear()); //вставляем Начало года
            ((Tr) rows.get(1)).getContent().add(Foo.createTC_endYear()); //вставляем Конец года
            ((Tr) rows.get(2)).getContent().add(Foo.createTC_startRating()); //вставляем rating
            ((Tr) rows.get(2)).getContent().add(Foo.createTC_endRating()); //вставляем rating
        }
        //Столбец Средний рейтинг
        ((Tr) rows.get(0)).getContent().add(Foo.createTC_MidTitle()); //вставляем question
        ((Tr) rows.get(1)).getContent().add(Foo.createTC_MidStartYear()); //вставляем Начало года
        ((Tr) rows.get(1)).getContent().add(Foo.createTC_MidEndYear()); //вставляем Конец года
        ((Tr) rows.get(2)).getContent().add(Foo.createTC_MidStartRating()); //вставляем rating
        ((Tr) rows.get(2)).getContent().add(Foo.createTC_MidEndRating()); //вставляем rating

        List<Object> texts = getAllElementFromObject(template.getMainDocumentPart(), Text.class);
        for (int i = 0; i < texts.size(); i++) {
            Text textElement = (Text) texts.get(i);
            System.out.println(i + " ; " + textElement.getValue() + " ; " + textElement);
        }
    }

    public static Tbl getTemplateTable(List<Object> tables, String templateKey) throws Docx4JException, JAXBException {
        for (Iterator<Object> iterator = tables.iterator(); iterator.hasNext(); ) {
            Object tbl = iterator.next();
            List<?> textElements = getAllElementFromObject(tbl, Text.class);
            for (Object text : textElements) {
                Text textElement = (Text) text;
                if (textElement.getValue() != null && textElement.getValue().equals(templateKey))
                    return (Tbl) tbl;
            }
        }
        return null;
    }

//    public static void addRowToTable(Tbl reviewTable, Tr templateRow, String childFullName) {
//        Tr workingRow = (Tr) XmlUtils.deepCopy(templateRow);
//        List textElements = getAllElementFromObject(workingRow, Text.class);
//        for (Object object : textElements) {
//            Text text = (Text) object;
//            if (text.getValue().equals("childFullName")) {
//                text.setValue(childFullName);
//            }
//        }
//        reviewTable.getContent().add(workingRow);
//    }

    public static void addRowToTable_1(Tbl reviewTable, Tr templateRow, String childFullName, Child child, EducationDirection educationDirection) {
        Tr workingRow = (Tr) XmlUtils.deepCopy(templateRow);
        List textElements = getAllElementFromObject(workingRow, Text.class);
        for (Object object : textElements) {
            Text text = (Text) object;
            if (text.getValue().equals("childFullName")) {
                text.setValue(childFullName);
            }
        }
        reviewTable.getContent().add(workingRow);
        List<ResultMonitoring> allResultMonitoring = child.getResultMonitors().stream().filter(x -> x.getEducationDirection().getId() == educationDirection.getId()).filter(x -> x.isStarEducationYear()).collect(Collectors.toList());
        int indexResultMonitoring = 0;
        textElements = getAllElementFromObject(workingRow, Text.class);
        for (Object object : textElements) {
            Text text = (Text) object;
            if (text.getValue().equals("startRating")) {
                if (allResultMonitoring.get(indexResultMonitoring).getRating() == 0) {
                    text.setValue("Отсут.");
                } else {
                    text.setValue(String.valueOf(allResultMonitoring.get(indexResultMonitoring).getRating()));
                }
                indexResultMonitoring++;
            }
        }

        DecimalFormat f = new DecimalFormat("###.##");
        float summa = 0;
        for (ResultMonitoring resultMonitoring : allResultMonitoring) {
            System.out.println("resultMonitoring.isStarEducationYear() - " + resultMonitoring.isStarEducationYear());
            System.out.println("resultMonitoring.getId() - " + resultMonitoring.getId());
            if (resultMonitoring.isStarEducationYear()) {
                //Считаем среднею оценку
                summa += resultMonitoring.getRating();
                System.out.println("summa = " + summa);
            }
        }
        summa = summa / allResultMonitoring.size();
        String strSumma = f.format(summa);
        System.out.println("allResultMonitoring.size = " + allResultMonitoring.size());
        System.out.println("На выходе средняя оценка = " + strSumma);
        for (Object object : textElements) {
            Text text = (Text) object;
            if (text.getValue().equals("startMidRating")) {
                if (summa == 0) {
                    text.setValue("Отсут.");
                } else {
                    text.setValue(String.valueOf(strSumma));
                }
                break;
            }
        }
        for (Object object : textElements) {
            Text text = (Text) object;
            if (text.getValue().equals("endRating") | text.getValue().equals("endMidRating")) {
                text.setValue(String.valueOf("---"));
            }
        }
    }


    public static void addRowToTable_2(Tbl reviewTable, Tr templateRow, String childFullName, Child child, EducationDirection educationDirection) {
        Tr workingRow = (Tr) XmlUtils.deepCopy(templateRow);
        List textElements = getAllElementFromObject(workingRow, Text.class);
        for (Object object : textElements) {
            Text text = (Text) object;
            if (text.getValue().equals("childFullName")) {
                text.setValue(childFullName);
            }
        }
        reviewTable.getContent().add(workingRow);
        List<ResultMonitoring> allResultMonitoring = child.getResultMonitors().stream().filter(x -> x.getEducationDirection().getId() == educationDirection.getId()).filter(x -> x.isStarEducationYear()).collect(Collectors.toList());
        int indexResultMonitoring = 0;
        textElements = getAllElementFromObject(workingRow, Text.class);
        for (Object object : textElements) {
            Text text = (Text) object;
            if (text.getValue().equals("startRating")) {
                if (allResultMonitoring.get(indexResultMonitoring).getRating() == 0) {
                    text.setValue("Отсут.");
                } else {
                    text.setValue(String.valueOf(allResultMonitoring.get(indexResultMonitoring).getRating()));
                }
                indexResultMonitoring++;
            }
        }

        DecimalFormat f = new DecimalFormat("###.##");
        float summa = 0;
        for (ResultMonitoring resultMonitoring : allResultMonitoring) {
            summa += resultMonitoring.getRating();
        }
        summa = summa / allResultMonitoring.size();
        String strSumma = f.format(summa);
        for (Object object : textElements) {
            Text text = (Text) object;
            if (text.getValue().equals("startMidRating")) {
                if (summa == 0) {
                    text.setValue("Отсут.");
                } else {
                    text.setValue(String.valueOf(strSumma));
                }
                break;
            }
        }

        allResultMonitoring = child.getResultMonitors().stream().filter(x -> x.getEducationDirection().getId() == educationDirection.getId()).filter(x -> !x.isStarEducationYear()).collect(Collectors.toList());
        indexResultMonitoring = 0;
        for (Object object : textElements) {
            Text text = (Text) object;
            if (text.getValue().equals("endRating")) {
                if (allResultMonitoring.get(indexResultMonitoring).getRating() == 0) {
                    text.setValue("Отсут.");
                } else {
                    text.setValue(String.valueOf(allResultMonitoring.get(indexResultMonitoring).getRating()));
                }
                indexResultMonitoring++;
            }
        }
        summa = 0;
        for (ResultMonitoring resultMonitoring : allResultMonitoring) {
            summa += resultMonitoring.getRating();
        }
        summa = summa / allResultMonitoring.size();
        strSumma = f.format(summa);
        for (Object object : textElements) {
            Text text = (Text) object;
            if (text.getValue().equals("endMidRating")) {
                if (summa == 0) {
                    text.setValue("Отсут.");
                } else {
                    text.setValue(String.valueOf(strSumma));
                }
                break;
            }
        }
    }

    //
    public static void addGroupResultRow(WordprocessingMLPackage template, List<GroupResultMonitoring> allGroupResultMonitorings) throws JAXBException, Docx4JException {
        List<Object> tables = getAllElementFromObject(template.getMainDocumentPart(), Tbl.class);
        //Поиск таблицы
        Tbl tempTable = getTemplateTable(tables, "educationArea");
        List<Object> rows = getAllElementFromObject(tempTable, Tr.class);
        Tr templateRow = (Tr) rows.get(1);
        for (GroupResultMonitoring resultMonitoring : allGroupResultMonitorings) {
            addRowToTableGroupMonitoring(tempTable, templateRow, resultMonitoring);
        }
        //Удаляем шаблонную строку
        tempTable.getContent().remove(templateRow);
    }

    public static void addRowToTableGroupMonitoring(Tbl reviewTable, Tr templateRow, GroupResultMonitoring resultMonitoring) {
        Tr workingRow = (Tr) XmlUtils.deepCopy(templateRow);
        List textElements = getAllElementFromObject(workingRow, Text.class);
        for (Object object : textElements) {
            Text text = (Text) object;
            if (text.getValue().equals("educationArea")) {
                text.setValue(resultMonitoring.getEducationDirection().getEducationArea().getName());
            }
            if (text.getValue().equals("educationDirection")) {
                text.setValue(resultMonitoring.getEducationDirection().getName());
            }
            if (text.getValue().equals("highLevel")) {
                text.setValue(resultMonitoring.getHighLevel());
            }
            if (text.getValue().equals("midLevel")) {
                text.setValue(resultMonitoring.getMidLevel());
            }
            if (text.getValue().equals("lowLevel")) {
                text.setValue(resultMonitoring.getLowLevel());
            }
        }
        reviewTable.getContent().add(workingRow);
    }


    public static ResponseEntity<Object> downloadReport(WordprocessingMLPackage template, String target) throws IOException, Docx4JException {
        Date date = new Date();
        SimpleDateFormat simpleDateFormat_out = new SimpleDateFormat("ddMMyyyy-HHmmss", Locale.getDefault());
        String strDate = simpleDateFormat_out.format(date);
        String fileName = target + "-" + strDate + ".docx";
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        template.save(outputStream);
        byte[] bytes = outputStream.toByteArray();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        InputStreamResource resource = new InputStreamResource(inputStream);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", String.format("attachment; filename=\"%s\"", fileName));
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        ResponseEntity<Object>
                responseEntity = ResponseEntity.ok().headers(headers).contentLength(outputStream.size()).contentType(
                MediaType.parseMediaType("application/txt")).body(resource);
        return responseEntity;
    }

    public static WordprocessingMLPackage merge(WordprocessingMLPackage f, WordprocessingMLPackage s) throws JAXBException, Docx4JException, IOException {
        List body = s.getMainDocumentPart().getJAXBNodesViaXPath("//w:body", false);
        for (Object b : body) {
            List filhos = ((org.docx4j.wml.Body) b).getContent();
            for (Object k : filhos)
                f.getMainDocumentPart().addObject(k);
        }

        List blips = s.getMainDocumentPart().getJAXBNodesViaXPath("//a:blip", false);
        for (Object el : blips) {
            try {
                CTBlip blip = (CTBlip) el;

                RelationshipsPart parts = s.getMainDocumentPart().getRelationshipsPart();
                Relationship rel = parts.getRelationshipByID(blip.getEmbed());

                RelationshipsPart docRels = f.getMainDocumentPart().getRelationshipsPart();

                rel.setId(null);
                docRels.addRelationship(rel);
                blip.setEmbed(rel.getId());

                f.getMainDocumentPart().addTargetPart(s.getParts().getParts().get(new PartName("/word/" + rel.getTarget())));

            } catch (Exception ex) {
            }
        }
        return f;
//        File saved = new File("saved.docx");
//        f.save(saved);
    }


    public static void addGroupColumn(WordprocessingMLPackage template, List<Group> allGroups, String placeholder) throws JAXBException, Docx4JException, CloneNotSupportedException {
        List<Object> tables = getAllElementFromObject(template.getMainDocumentPart(), Tbl.class);
        //Поиск таблицы
        Tbl tempTable = getTemplateTable(tables, placeholder);
        List<Object> rows = getAllElementFromObject(tempTable, Tr.class);

        List<Object> allTC = getAllElementFromObject(template.getMainDocumentPart(), Tc.class);
        for (int i = 1; i < allGroups.size(); i++) {
            int currentTR_0 = ((Tr) rows.get(0)).getContent().size() - 1;
            int currentTR_1 = ((Tr) rows.get(1)).getContent().size() - 2;
            int currentTR_2 = ((Tr) rows.get(2)).getContent().size() - 2;
            int currentTR_3 = ((Tr) rows.get(3)).getContent().size() - 2;
            int currentTR_4 = ((Tr) rows.get(4)).getContent().size() - 2;
            int currentTR_5 = ((Tr) rows.get(5)).getContent().size() - 2;
            int currentTR_6 = ((Tr) rows.get(6)).getContent().size() - 2;

            ((Tr) rows.get(0)).getContent().add(currentTR_0, XmlUtils.deepCopy(allTC.get(1))); //вставляем groupFullName
            ((Tr) rows.get(1)).getContent().add(currentTR_1, XmlUtils.deepCopy(allTC.get(6))); //вставляем Конец года
            ((Tr) rows.get(1)).getContent().add(currentTR_1, XmlUtils.deepCopy(allTC.get(5))); //вставляем Начало года
            ((Tr) rows.get(2)).getContent().add(currentTR_2, XmlUtils.deepCopy(allTC.get(13))); //вставляем highLevlEnd
            ((Tr) rows.get(2)).getContent().add(currentTR_2, XmlUtils.deepCopy(allTC.get(12))); //вставляем highLevlStart
            ((Tr) rows.get(3)).getContent().add(currentTR_3, XmlUtils.deepCopy(allTC.get(20))); //вставляем midLvlEnd
            ((Tr) rows.get(3)).getContent().add(currentTR_3, XmlUtils.deepCopy(allTC.get(19))); //вставляем midLvlStart
            ((Tr) rows.get(4)).getContent().add(currentTR_4, XmlUtils.deepCopy(allTC.get(27))); //вставляем lowLvlEnd
            ((Tr) rows.get(4)).getContent().add(currentTR_4, XmlUtils.deepCopy(allTC.get(26))); //вставляем lowLvlStart
            ((Tr) rows.get(5)).getContent().add(currentTR_5, XmlUtils.deepCopy(allTC.get(33))); //вставляем countFullEnd
            ((Tr) rows.get(5)).getContent().add(currentTR_5, XmlUtils.deepCopy(allTC.get(32))); //вставляем countFullStart
            ((Tr) rows.get(6)).getContent().add(currentTR_6, XmlUtils.deepCopy(allTC.get(39))); //вставляем countSizeEnd
            ((Tr) rows.get(6)).getContent().add(currentTR_6, XmlUtils.deepCopy(allTC.get(38))); //вставляем countSizeStart

        }
    }

    public static void addEducationAreaRow(WordprocessingMLPackage template, List<EducationArea> allEducationAreas, List<Group> allGroups, List<EducationDirection> allEducationDirections, String placeholder, boolean isStarEducationYear) throws JAXBException, Docx4JException {
        //Обнуляем списки м итоговыми резудьтатми по областям, чтобы там были актуальные данные и без предыдущих, т.к. колекции все эти static
        listAllHighLvlStart.clear();
        listAllMidLvlStart.clear();
        listAllLowLvlStart.clear();

        listAllHighLvlEnd.clear();
        listAllMidLvlEnd.clear();
        listAllLowLvlEnd.clear();


        List<Object> tables = getAllElementFromObject(template.getMainDocumentPart(), Tbl.class);
        //Поиск таблицы
        Tbl tempTable = getTemplateTable(tables, placeholder);
        List<Object> rows = getAllElementFromObject(tempTable, Tr.class);
        List<Tr> templateRows = new ArrayList<>();
        templateRows.add((Tr) rows.get(2));
        templateRows.add((Tr) rows.get(3));
        templateRows.add((Tr) rows.get(4));
        int countEdArea = 1;
        for (EducationArea educationArea : allEducationAreas) {
            addRowToTableGeneralReport(template, tempTable, templateRows, educationArea, allGroups, allEducationDirections, countEdArea, isStarEducationYear);
            countEdArea++;
        }
        for (Tr tr : templateRows) {
            tempTable.getContent().remove(tr);
        }
    }


    public static void addRowToTableGeneralReport(WordprocessingMLPackage template, Tbl reviewTable, List<Tr> templateRows, EducationArea educationArea, List<Group> allGroups, List<EducationDirection> allEducationDirections, int countEdArea, boolean isStarEducationYear) {
        System.out.println("educationArea - " + educationArea.getName());
        List<Tr> workingRows = new ArrayList<>();
        //Строки таблицы, в нашем случае 2,3,4 из шаблона
        for (Tr tr : templateRows) {
            workingRows.add((Tr) XmlUtils.deepCopy(tr));
        }

        DecimalFormat f = new DecimalFormat("###.#");
        List textElements = getAllElementFromObject(workingRows.get(0), Text.class);
        for (Object object : textElements) {
            Text text = (Text) object;
            if (text.getValue().equals("numbEdArea")) {
                text.setValue(String.valueOf(countEdArea));
            }
            if (text.getValue().equals("edArea")) {
                text.setValue(educationArea.getName());
            }
        }

        float allHighLvlStart = 0;
        float allMidLvlStart = 0;
        float allLowLvlStart = 0;

        float allHighLvlEnd = 0;
        float allMidLvlEnd = 0;
        float allLowLvlEnd = 0;

        for (Group group : allGroups) {
            System.out.println("****************************************************");
            System.out.println("Группа - " + group.getGroupName());
            String groupFullName = group.getGroupName() + "\n(" + group.getAgeGroup().getName() + ")";
            replacePlaceholder(template, groupFullName, "groupFullName");
            float highLvlStart = 0;
            float midLvlStart = 0;
            float lowLvlStart = 0;
            //КОл-во направлений развития у Образовательной области
            int countEdDirection = allEducationDirections.stream().filter(x -> x.getEducationArea().getId() == educationArea.getId()).filter(x -> x.getAgeGroup().getId() == group.getAgeGroup().getId()).collect(Collectors.toList()).size();
            //ПОолучили все результаты мониторинга для Образовательной области, для старта года!
            List<GroupResultMonitoring> groupResultMonitorings = group.getGroupResultMonitorings().stream().filter(x -> x.isStarEducationYear()).filter(x -> x.getEducationDirection().getEducationArea().getId() == educationArea.getId()).collect(Collectors.toList());
            for (GroupResultMonitoring resultMonitoring : groupResultMonitorings) {
                highLvlStart += Float.parseFloat(resultMonitoring.getHighLevel());
                midLvlStart += Float.parseFloat(resultMonitoring.getMidLevel());
                lowLvlStart += Float.parseFloat(resultMonitoring.getLowLevel());
            }
            System.out.println("countEdDirection = " + countEdDirection);

            System.out.println("highLvlStart = " + highLvlStart);
            System.out.println("midLvlStart = " + midLvlStart);
            System.out.println("lowLvlStart = " + lowLvlStart);

            highLvlStart = highLvlStart / countEdDirection;
            midLvlStart = midLvlStart / countEdDirection;
            lowLvlStart = lowLvlStart / countEdDirection;

            allHighLvlStart += highLvlStart;
            allMidLvlStart += midLvlStart;
            allLowLvlStart += lowLvlStart;

            System.out.println("highLvlStart = " + highLvlStart);
            System.out.println("midLvlStart = " + midLvlStart);
            System.out.println("lowLvlStart = " + lowLvlStart);

            replacePlaceholderInTR(workingRows.get(0), f.format(highLvlStart), "highLvlStart");
            replacePlaceholderInTR(workingRows.get(1), f.format(midLvlStart), "midLvlStart");
            replacePlaceholderInTR(workingRows.get(2), f.format(lowLvlStart), "lowLvlStart");

            //Теперь смотрим конец года или начало
            //Еслм начало, то места для конца года заполняем черточками
            if (isStarEducationYear) {
                replacePlaceholderInTR(workingRows.get(0), "--", "highLvlEnd");
                replacePlaceholderInTR(workingRows.get(1), "--", "midLvlEnd");
                replacePlaceholderInTR(workingRows.get(2), "--", "lowLvlEnd");
            } else {
                float highLvlEnd = 0;
                float midLvlEnd = 0;
                float lowLvlEnd = 0;
                //ПОолучили все результаты мониторинга для Образовательной области, для КОНЦА года!
                List<GroupResultMonitoring> groupResultMonitoringsEND = group.getGroupResultMonitorings().stream().filter(x -> !x.isStarEducationYear()).filter(x -> x.getEducationDirection().getEducationArea().getId() == educationArea.getId()).collect(Collectors.toList());
                for (GroupResultMonitoring resultMonitoring : groupResultMonitoringsEND) {
                    highLvlEnd += Float.parseFloat(resultMonitoring.getHighLevel());
                    midLvlEnd += Float.parseFloat(resultMonitoring.getMidLevel());
                    lowLvlEnd += Float.parseFloat(resultMonitoring.getLowLevel());
                }
                highLvlEnd = highLvlEnd / countEdDirection;
                midLvlEnd = midLvlEnd / countEdDirection;
                lowLvlEnd = lowLvlEnd / countEdDirection;

                allHighLvlEnd += highLvlEnd;
                allMidLvlEnd += midLvlEnd;
                allLowLvlEnd += lowLvlEnd;

                replacePlaceholderInTR(workingRows.get(0), f.format(highLvlEnd), "highLvlEnd");
                replacePlaceholderInTR(workingRows.get(1), f.format(midLvlEnd), "midLvlEnd");
                replacePlaceholderInTR(workingRows.get(2), f.format(lowLvlEnd), "lowLvlEnd");
            }
            System.out.println("****************************************************");
        }

        System.out.println("allHighLvlStart = " + allHighLvlStart);
        System.out.println("allMidLvlStart = " + allMidLvlStart);
        System.out.println("allLowLvlStart = " + allLowLvlStart);

        allHighLvlStart = allHighLvlStart / allGroups.size();
        allMidLvlStart = allMidLvlStart / allGroups.size();
        allLowLvlStart = allLowLvlStart / allGroups.size();

        System.out.println("полсе деления allHighLvlStart = " + allHighLvlStart);
        System.out.println("полсе деления allMidLvlStart = " + allMidLvlStart);
        System.out.println("полсе деления allLowLvlStart = " + allLowLvlStart);


        replacePlaceholderInTR(workingRows.get(0), f.format(allHighLvlStart), "allHighLvlStart");
        replacePlaceholderInTR(workingRows.get(1), f.format(allMidLvlStart), "allMidLvlStart");
        replacePlaceholderInTR(workingRows.get(2), f.format(allLowLvlStart), "allLowLvlStart");
        listAllHighLvlStart.add(f.format(allHighLvlStart));
        listAllMidLvlStart.add(f.format(allMidLvlStart));
        listAllLowLvlStart.add(f.format(allLowLvlStart));

        if (isStarEducationYear) {
            replacePlaceholderInTR(workingRows.get(0), "--", "allHighLvlEnd");
            replacePlaceholderInTR(workingRows.get(1), "--", "allMidLvlEnd");
            replacePlaceholderInTR(workingRows.get(2), "--", "allLowLvlEnd");
        } else {
            allHighLvlEnd = allHighLvlEnd / allGroups.size();
            allMidLvlEnd = allMidLvlEnd / allGroups.size();
            allLowLvlEnd = allLowLvlEnd / allGroups.size();
            replacePlaceholderInTR(workingRows.get(0), f.format(allHighLvlEnd), "allHighLvlEnd");
            replacePlaceholderInTR(workingRows.get(1), f.format(allMidLvlEnd), "allMidLvlEnd");
            replacePlaceholderInTR(workingRows.get(2), f.format(allLowLvlEnd), "allLowLvlEnd");
            listAllHighLvlEnd.add(f.format(allHighLvlEnd));
            listAllMidLvlEnd.add(f.format(allMidLvlEnd));
            listAllLowLvlEnd.add(f.format(allLowLvlEnd));
        }

        int allCountFullStart = 0;
        int allCountSizeStart = 0;
        int allCountFullEnd = 0;
        int allCountSizeEnd = 0;
        float allPercentStart = 0.0f;
        float allPercentEnd = 0.0f;

        System.out.println("Кол-во всех групп - " + allGroups.size());
        //Заполняю кол-во детей прошедших мониторинг
        for (Group group : allGroups) {

            System.out.println("group.getGroupName() - " + group.getGroupName());
            //Берую любой мониторинг группы по первому попавшемуся направлению (Отфильтровываю только начало или конец года)
            // и смотрю у него поля countAvailableChild
            //Исходим из того, что если в группе из 20 детей, 15 прошло мониторинг по направлению развития, то
            //для всех остальных направлений развития и областей развития, это число будет одинаково!
            int allGroupSizeStartYear = Integer.parseInt(group.getGroupResultMonitorings().stream().filter(x -> x.isStarEducationYear()).collect(Collectors.toList()).get(0).getCountAllChild());
            int countAvailableChildStartYear = Integer.parseInt(group.getGroupResultMonitorings().stream().filter(x -> x.isStarEducationYear()).collect(Collectors.toList()).get(0).getCountAvailableChild());

            float percent = (float) (100.0 / allGroupSizeStartYear) * countAvailableChildStartYear;
            String forAvailableChild = countAvailableChildStartYear + "\n(" + f.format(percent) + "%)";

            replacePlaceholder(template, String.valueOf(allGroupSizeStartYear), "countFullStart");
            replacePlaceholder(template, forAvailableChild, "countSizeStart");

            allCountFullStart += allGroupSizeStartYear;
            allCountSizeStart += countAvailableChildStartYear;

            if (isStarEducationYear) {
                replacePlaceholder(template, "--", "countFullEnd");
                replacePlaceholder(template, "--", "countSizeEnd");
                replacePlaceholder(template, "--", "allCountFullEnd");
                replacePlaceholder(template, "--", "allCountSizeEnd");
            } else {
                int allGroupSizeEndYear = Integer.parseInt(group.getGroupResultMonitorings().stream().filter(x -> !x.isStarEducationYear()).collect(Collectors.toList()).get(0).getCountAllChild());
                int countAvailableChildEndYear = Integer.parseInt(group.getGroupResultMonitorings().stream().filter(x -> !x.isStarEducationYear()).collect(Collectors.toList()).get(0).getCountAvailableChild());
                percent = (float) (100.0 / allGroupSizeEndYear) * countAvailableChildEndYear;
                forAvailableChild = countAvailableChildEndYear + "\n(" + f.format(percent) + "%)";
                replacePlaceholder(template, String.valueOf(allGroupSizeEndYear), "countFullEnd");
                replacePlaceholder(template, forAvailableChild, "countSizeEnd");
                allCountFullEnd += allGroupSizeEndYear;
                allCountSizeEnd += countAvailableChildEndYear;
            }
        }

        //Заполняю кол-во детей прошедших мониторинг
        allPercentStart = (float) (100.0 / allCountFullStart * allCountSizeStart);
        String forAvailableChild = allCountSizeStart + "\n(" + f.format(allPercentStart) + "%)";
        if (isStarEducationYear) {
            replacePlaceholder(template, String.valueOf(allCountFullStart), "allCountFullStart");
            replacePlaceholder(template, forAvailableChild, "allCountSizeStart");
            replacePlaceholder(template, "--", "allCountFullEnd");
            replacePlaceholder(template, "--", "allCountSizeEnd");
        } else {
            allPercentEnd = (float) (100.0 / allCountFullEnd * allCountSizeEnd);
            replacePlaceholder(template, String.valueOf(allCountFullStart), "allCountFullStart");
            replacePlaceholder(template, forAvailableChild, "allCountSizeStart");

            forAvailableChild = allCountSizeEnd + "\n(" + f.format(allPercentEnd) + "%)";

            replacePlaceholder(template, String.valueOf(allCountFullEnd), "allCountFullEnd");
            replacePlaceholder(template, forAvailableChild, "allCountSizeEnd");
        }

        //ДОбавляем модифицированные строки в таблицу
        for (Tr workingRow : workingRows) {
            reviewTable.getContent().add(reviewTable.getContent().size() - 2, workingRow);
        }


    }

    public static void addEducationAreaRowSecondTable(WordprocessingMLPackage template, List<EducationArea> allEducationAreas, String placeholder, boolean isStarEducationYear) throws JAXBException, Docx4JException {
        List<Object> tables = getAllElementFromObject(template.getMainDocumentPart(), Tbl.class);
        //Поиск таблицы
        Tbl tempTable = getTemplateTable(tables, placeholder);
        List<Object> rows = getAllElementFromObject(tempTable, Tr.class);
        List<Tr> templateRows = new ArrayList<>();
        templateRows.add((Tr) rows.get(1));
        templateRows.add((Tr) rows.get(2));
        templateRows.add((Tr) rows.get(3));
        int countEdArea = 0;
        for (EducationArea educationArea : allEducationAreas) {
            addRowToSecondTableGeneralReport(template, tempTable, templateRows, educationArea, isStarEducationYear, countEdArea);
            countEdArea++;
        }


        //Данные ИТОГО во 2 таблице в отчете
        DecimalFormat f = new DecimalFormat("###.#");
        float twoAllHighStart = 0.0f;
        float twoAllMidStart = 0.0f;
        float twoAllLowStart = 0.0f;
        for (String s : listAllHighLvlStart) {
            twoAllHighStart += Float.parseFloat(s.replace(",", "."));
        }
        for (String s : listAllMidLvlStart) {
            twoAllMidStart += Float.parseFloat(s.replace(",", "."));
        }
        for (String s : listAllLowLvlStart) {
            twoAllLowStart += Float.parseFloat(s.replace(",", "."));
        }
        twoAllHighStart /= allEducationAreas.size();
        twoAllMidStart /= allEducationAreas.size();
        twoAllLowStart /= allEducationAreas.size();
        replacePlaceholder(template, f.format(twoAllHighStart), "twoAllHighStart");
        replacePlaceholder(template, f.format(twoAllMidStart), "twoAllMidStart");
        replacePlaceholder(template, f.format(twoAllLowStart), "twoAllLowStart");

        if (isStarEducationYear) {
            replacePlaceholder(template, "--", "twoAllHighEnd");
            replacePlaceholder(template, "--", "twoAllMidEnd");
            replacePlaceholder(template, "--", "twoAllLowEnd");
        } else {
            float twoAllHighEnd = 0.0f;
            float twoAllMidEnd = 0.0f;
            float twoAllLowEnd = 0.0f;
            for (String s : listAllHighLvlEnd) {
                twoAllHighEnd += Float.parseFloat(s.replace(",", "."));
            }
            for (String s : listAllMidLvlEnd) {
                twoAllMidEnd += Float.parseFloat(s.replace(",", "."));
            }
            for (String s : listAllLowLvlEnd) {
                twoAllLowEnd += Float.parseFloat(s.replace(",", "."));
            }
            twoAllHighEnd /= allEducationAreas.size();
            twoAllMidEnd /= allEducationAreas.size();
            twoAllLowEnd /= allEducationAreas.size();
            replacePlaceholder(template, f.format(twoAllHighEnd), "twoAllHighEnd");
            replacePlaceholder(template, f.format(twoAllMidEnd), "twoAllMidEnd");
            replacePlaceholder(template, f.format(twoAllLowEnd), "twoAllLowEnd");

        }


        for (Tr tr : templateRows) {
            tempTable.getContent().remove(tr);
        }
    }


    public static void addRowToSecondTableGeneralReport(WordprocessingMLPackage template, Tbl reviewTable, List<Tr> templateRows, EducationArea educationArea, boolean isStarEducationYear, int countEdArea) {
        System.out.println("educationArea - " + educationArea.getName());
        List<Tr> workingRows = new ArrayList<>();
        //Строки таблицы, в нашем случае 2,3,4 из шаблона
        for (Tr tr : templateRows) {
            workingRows.add((Tr) XmlUtils.deepCopy(tr));
        }
        List textElements = getAllElementFromObject(workingRows.get(0), Text.class);
        for (Object object : textElements) {
            Text text = (Text) object;
            if (text.getValue().equals("edArea")) {
                text.setValue(educationArea.getName());
            }
        }

        replacePlaceholderInTR(workingRows.get(0), listAllHighLvlStart.get(countEdArea), "twoHighStart");
        replacePlaceholderInTR(workingRows.get(1), listAllMidLvlStart.get(countEdArea), "twoMidStart");
        replacePlaceholderInTR(workingRows.get(2), listAllLowLvlStart.get(countEdArea), "twoLowStart");

        if (isStarEducationYear) {
            replacePlaceholderInTR(workingRows.get(0), "--", "twoHighEnd");
            replacePlaceholderInTR(workingRows.get(1), "--", "twoMidEnd");
            replacePlaceholderInTR(workingRows.get(2), "--", "twoLowEnd");
        } else {
            replacePlaceholderInTR(workingRows.get(0), listAllHighLvlEnd.get(countEdArea), "twoHighEnd");
            replacePlaceholderInTR(workingRows.get(1), listAllMidLvlEnd.get(countEdArea), "twoMidEnd");
            replacePlaceholderInTR(workingRows.get(2), listAllLowLvlEnd.get(countEdArea), "twoLowEnd");
        }

        //ДОбавляем модифицированные строки в таблицу
        for (Tr workingRow : workingRows) {
            reviewTable.getContent().add(reviewTable.getContent().size() - 4, workingRow);
        }
    }

    //Формирования Портфолио Ребенка
    public static void addChildDiplomaRow(WordprocessingMLPackage template, List<Diploma> allDiplomas, String placeholder) throws JAXBException, Docx4JException {
        List<Object> tables = getAllElementFromObject(template.getMainDocumentPart(), Tbl.class);
        //Поиск таблицы
        Tbl tempTable = getTemplateTable(tables, placeholder);
        List<Object> rows = getAllElementFromObject(tempTable, Tr.class);
        Tr templateRow = (Tr) rows.get(1);
        int num = 1;
        for (Diploma diploma : allDiplomas) {
            addRowToTableChildDiploma(tempTable, templateRow, diploma, num);
            num++;
        }
        //Удаляем шаблонную строку
        tempTable.getContent().remove(templateRow);
    }

    public static void addRowToTableChildDiploma(Tbl reviewTable, Tr templateRow, Diploma diploma, int num) {
        Tr workingRow = (Tr) XmlUtils.deepCopy(templateRow);
        List textElements = getAllElementFromObject(workingRow, Text.class);
        for (Object object : textElements) {
            Text text = (Text) object;
            if (text.getValue().equals("num")) {
                text.setValue(String.valueOf(num));
            }
            if (text.getValue().equals("masterFio")) {
                text.setValue(diploma.getLastName() + " " + diploma.getFirstName() + " " + diploma.getPatronymicName());
            }
            if (text.getValue().equals("chalangeName")) {
                text.setValue(diploma.getText());
            }
            if (text.getValue().equals("place")) {
                if (diploma.getPlace().equals("")) {
                    text.setValue("--");
                } else {
                    text.setValue(diploma.getPlace());
                }

            }
            if (text.getValue().equals("dateDiplom")) {
                SimpleDateFormat simpleDateFormat_out = new SimpleDateFormat("dd.MM.yyyy");
                String strDate = simpleDateFormat_out.format(diploma.getDate());
                text.setValue(strDate);
            }
        }
        reviewTable.getContent().add(workingRow);
    }


    //Формирования Портфолио Воспитателя - Дипломы воспитателя
    public static void addUserDiplomaRow(WordprocessingMLPackage template, List<Diploma> allDiplomas, String placeholder) throws JAXBException, Docx4JException {
        List<Object> tables = getAllElementFromObject(template.getMainDocumentPart(), Tbl.class);
        //Поиск таблицы
        Tbl tempTable = getTemplateTable(tables, placeholder);
        List<Object> rows = getAllElementFromObject(tempTable, Tr.class);
        Tr templateRow = (Tr) rows.get(1);
        int num = 1;
        for (Diploma diploma : allDiplomas) {
            addRowToTableUserDiploma(tempTable, templateRow, diploma, num);
            num++;
        }
        //Удаляем шаблонную строку
        tempTable.getContent().remove(templateRow);
    }

    public static void addRowToTableUserDiploma(Tbl reviewTable, Tr templateRow, Diploma diploma, int num) {
        Tr workingRow = (Tr) XmlUtils.deepCopy(templateRow);
        List textElements = getAllElementFromObject(workingRow, Text.class);
        for (Object object : textElements) {
            Text text = (Text) object;
            if (text.getValue().equals("num")) {
                text.setValue(String.valueOf(num));
            }
            if (text.getValue().equals("chalangeName")) {
                text.setValue(diploma.getText());
            }
            if (text.getValue().equals("place")) {
                if (diploma.getPlace().equals("")) {
                    text.setValue("--");
                } else {
                    text.setValue(diploma.getPlace());
                }

            }
            if (text.getValue().equals("dateDiplom")) {
                SimpleDateFormat simpleDateFormat_out = new SimpleDateFormat("dd.MM.yyyy");
                String strDate = simpleDateFormat_out.format(diploma.getDate());
                text.setValue(strDate);
            }
        }
        reviewTable.getContent().add(workingRow);
    }

    //Формирования Портфолио Воспитателя - Дипломы детей воспитателя
    public static void addUserChildDiplomaRow(WordprocessingMLPackage template, List<Diploma> allDiplomas, String placeholder) throws JAXBException, Docx4JException {
        List<Object> tables = getAllElementFromObject(template.getMainDocumentPart(), Tbl.class);
        //Поиск таблицы
        Tbl tempTable = getTemplateTable(tables, placeholder);
        List<Object> rows = getAllElementFromObject(tempTable, Tr.class);
        Tr templateRow = (Tr) rows.get(1);
        int num = 1;
        for (Diploma diploma : allDiplomas) {
            addRowToTableUserChildDiploma(tempTable, templateRow, diploma, num);
            num++;
        }
        //Удаляем шаблонную строку
        tempTable.getContent().remove(templateRow);
    }

    public static void addRowToTableUserChildDiploma(Tbl reviewTable, Tr templateRow, Diploma diploma, int num) {
        Tr workingRow = (Tr) XmlUtils.deepCopy(templateRow);
        List textElements = getAllElementFromObject(workingRow, Text.class);
        for (Object object : textElements) {
            Text text = (Text) object;
            if (text.getValue().equals("numChild")) {
                text.setValue(String.valueOf(num));
            }
            if (text.getValue().equals("chalangeChild")) {
                text.setValue(diploma.getText());
            }
            if (text.getValue().equals("childFIO")) {
                text.setValue(diploma.getChild().getFullName());
            }
            if (text.getValue().equals("placeChild")) {
                System.out.println("diploma.getPlace() = " + diploma.getPlace() + ".");
                if (diploma.getPlace().equals("")) {
                    text.setValue("--");
                } else {
                    text.setValue(diploma.getPlace());
                }

            }
            if (text.getValue().equals("dateChild")) {
                SimpleDateFormat simpleDateFormat_out = new SimpleDateFormat("dd.MM.yyyy");
                String strDate = simpleDateFormat_out.format(diploma.getDate());
                text.setValue(strDate);
            }
        }
        reviewTable.getContent().add(workingRow);
    }


    //Формирования Плана индивидуальных занятий
    public static void addIndividualSessionRow(WordprocessingMLPackage template, List<IndividualSession> newIndividualSessions, String placeholder) throws JAXBException, Docx4JException {
        List<Object> tables = getAllElementFromObject(template.getMainDocumentPart(), Tbl.class);
        //Поиск таблицы
        Tbl tempTable = getTemplateTable(tables, placeholder);
        List<Object> rows = getAllElementFromObject(tempTable, Tr.class);
        Tr templateRow = (Tr) rows.get(1);
        int num = 1;
        for (IndividualSession individualSession : newIndividualSessions) {
            addRowToTableIndividualSession(tempTable, templateRow, individualSession, num);
            num++;
        }
        //Удаляем шаблонную строку
        tempTable.getContent().remove(templateRow);
    }

    public static void addRowToTableIndividualSession(Tbl reviewTable, Tr templateRow, IndividualSession individualSession, int num) {
        Tr workingRow = (Tr) XmlUtils.deepCopy(templateRow);
        List textElements = getAllElementFromObject(workingRow, Text.class);
        for (Object object : textElements) {
            Text text = (Text) object;
            if (text.getValue().equals("num")) {
                text.setValue(String.valueOf(num));
            }
            if (text.getValue().equals("childFIO")) {
                text.setValue(individualSession.getChild().getFullName());
            }
            if (text.getValue().equals("resultMonitoring")) {
                String reason = "Проблема с: " + individualSession.getResultMonitoring().getQuestion();
                text.setValue(reason);
            }
            if (text.getValue().equals("edArea")) {
                text.setValue(individualSession.getEducationArea().getName());
            }
            if (text.getValue().equals("manual")) {
                String strManual = individualSession.getTypeMaterial() + ": " + individualSession.getNameFileMaterials();
                text.setValue(strManual);
            }
            if (text.getValue().equals("dateIS")) {
                SimpleDateFormat simpleDateFormat_out = new SimpleDateFormat("dd.MM.yyyy");
                String strDate = simpleDateFormat_out.format(individualSession.getDate());
                text.setValue(strDate);
            }
        }
        reviewTable.getContent().add(workingRow);
    }


    //Формирования расписания
    public static void addForScheduleGroupRow(WordprocessingMLPackage template, List<Group> allGroups, String placeholder) throws JAXBException, Docx4JException {
        List<Object> tables = getAllElementFromObject(template.getMainDocumentPart(), Tbl.class);
        //Поиск таблицы
        Tbl tempTable = getTemplateTable(tables, placeholder);
        List<Object> rows = getAllElementFromObject(tempTable, Tr.class);

        List<Tr> templateRows = new ArrayList<>();
        templateRows.add((Tr) rows.get(1));
        templateRows.add((Tr) rows.get(2));
        templateRows.add((Tr) rows.get(3));
        for (Group group : allGroups) {
            addRowToTableSheduleGroup(template, tempTable, templateRows, group);
        }
        //Удаляем шаблонную строку
        for (Tr tr : templateRows) {
            tempTable.getContent().remove(tr);
        }

        List<Object> texts = getAllElementFromObject(tempTable, Text.class);
        for (Object object : texts) {
            Text text = (Text) object;
            System.out.println("text.getValue() - " + text.getValue());
            if (text.getValue().contains("T")) {
                text.setValue("");
            }
            if (text.getValue().contains("L")) {
                text.setValue("");
            }
        }



    }


    public static void addRowToTableSheduleGroup(WordprocessingMLPackage template, Tbl reviewTable, List<Tr> templateRows, Group group) {
        List<Tr> workingRows = new ArrayList<>();
        SimpleDateFormat simpleDateFormat_out = new SimpleDateFormat("HH:mm", Locale.getDefault());
        //Строки таблицы, в нашем случае 2,3,4 из шаблона
        for (Tr tr : templateRows) {
            workingRows.add((Tr) XmlUtils.deepCopy(tr));
        }

        List textElements = getAllElementFromObject(workingRows.get(0), Text.class);
        for (Object object : textElements) {
            Text text = (Text) object;
            if (text.getValue().equals("groupName")) {
                text.setValue(String.valueOf(group.getGroupName() + "(" + group.getAgeGroup().getName() + ")"));
            }
        }
        //Для каждого дня неделе
        for (int i = 0; i < 5; i++) {
            String day = MyService.getDayOfWeek(i);
            //берем уроки только для понедельника, потов вторника. среды и т.д.
            List<Lesson> lessonForDay = group.getLessons().stream().filter(x -> x.getDayWeek().equals(day)).collect(Collectors.toList());
            System.out.println("Кол-во урокв в  - " + day + " у группы " + group.getGroupName() + " = " + lessonForDay.size());
            for (int j = 0; j < lessonForDay.size(); j++) {
                String nameLesson = lessonForDay.get(j).getName();
                String timeStartLesson = simpleDateFormat_out.format(lessonForDay.get(j).getTimeOfStartLesson());
                String timeEndLesson = simpleDateFormat_out.format(lessonForDay.get(j).getTimeOfEndLesson());
                String time = timeStartLesson + " - " + timeEndLesson;
                String placeholder = "" + i + "L" + (j+1);
                String placeholderTime = "" + i + "T" + (j+1);
                System.out.println("Софрмирован плейсхолдер - " + placeholder);
                System.out.println("Софрмирован плейсхолдер time - " + placeholderTime);
                replacePlaceholderInTR(workingRows.get(j), nameLesson, placeholder);
                replacePlaceholderInTR(workingRows.get(j), time, placeholderTime);
            }
        }
        //ДОбавляем модифицированные строки в таблицу
        for (Tr workingRow : workingRows) {
            reviewTable.getContent().add(reviewTable.getContent().size() - 2, workingRow);
        }



    }
}