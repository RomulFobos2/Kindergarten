package com.mai.Kindergarten.service;

import javax.xml.bind.JAXBException;

import org.docx4j.XmlUtils;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Tr;

public class Foo {

    public static Tc createTC_question() throws JAXBException {
        String openXML = "<w:tc xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
                + "<w:tcPr>"
                + "<w:tcW w:type=\"dxa\" w:w=\"3771\"/>"
                + "<w:gridSpan w:val=\"2\"/>"
                + "<w:tcBorders>"
                + "<w:left w:color=\"auto\" w:space=\"0\" w:sz=\"4\" w:val=\"single\"/>"
                + "</w:tcBorders>"
                + "</w:tcPr>"
                + "<w:p>"
                + "<w:pPr>"
                + "<w:jc w:val=\"center\"/>"
                + "<w:rPr>"
                + "<w:rFonts w:ascii=\"Times New Roman\" w:cs=\"Times New Roman\" w:hAnsi=\"Times New Roman\"/>"
                + "<w:lang w:val=\"en-US\"/>"
                + "</w:rPr>"
                + "</w:pPr>"
                + "<w:r>"
                + "<w:rPr>"
                + "<w:rFonts w:ascii=\"Times New Roman\" w:cs=\"Times New Roman\" w:hAnsi=\"Times New Roman\"/>"
                + "<w:lang w:val=\"en-US\"/>"
                + "</w:rPr>"
                + "<w:t>question</w:t>"
                + "</w:r>"
                + "</w:p>"
                + "</w:tc>";
        Tc tc = (Tc) XmlUtils.unmarshalString(openXML);
        return tc;
    }


    public static Tc createTC_startYear() throws JAXBException {
        String openXML = "<w:tc xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
                + "<w:tcPr>"
                + "<w:tcW w:type=\"dxa\" w:w=\"1902\"/>"
                + "<w:tcBorders>"
                + "<w:left w:color=\"auto\" w:space=\"0\" w:sz=\"4\" w:val=\"single\"/>"
                + "</w:tcBorders>"
                + "</w:tcPr>"
                + "<w:p>"
                + "<w:pPr>"
                + "<w:jc w:val=\"center\"/>"
                + "<w:rPr>"
                + "<w:rFonts w:ascii=\"Times New Roman\" w:cs=\"Times New Roman\" w:hAnsi=\"Times New Roman\"/>"
                + "<w:lang w:val=\"en-US\"/>"
                + "</w:rPr>"
                + "</w:pPr>"
                + "<w:r>"
                + "<w:rPr>"
                + "<w:rFonts w:ascii=\"Times New Roman\" w:cs=\"Times New Roman\" w:hAnsi=\"Times New Roman\"/>"
                + "</w:rPr>"
                + "<w:t>Начало\nгода</w:t>"
                + "</w:r>"
                + "</w:p>"
                + "</w:tc>";
        Tc tc = (Tc) XmlUtils.unmarshalString(openXML);
        return tc;
    }


    public static Tc createTC_endYear() throws JAXBException {
        String openXML = "<w:tc xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
                + "<w:tcPr>"
                + "<w:tcW w:type=\"dxa\" w:w=\"1869\"/>"
                + "</w:tcPr>"
                + "<w:p>"
                + "<w:pPr>"
                + "<w:jc w:val=\"center\"/>"
                + "<w:rPr>"
                + "<w:rFonts w:ascii=\"Times New Roman\" w:cs=\"Times New Roman\" w:hAnsi=\"Times New Roman\"/>"
                + "<w:lang w:val=\"en-US\"/>"
                + "</w:rPr>"
                + "</w:pPr>"
                + "<w:r>"
                + "<w:rPr>"
                + "<w:rFonts w:ascii=\"Times New Roman\" w:cs=\"Times New Roman\" w:hAnsi=\"Times New Roman\"/>"
                + "</w:rPr>"
                + "<w:t>Конец\nгода</w:t>"
                + "</w:r>"
                + "</w:p>"
                + "</w:tc>";
        Tc tc = (Tc) XmlUtils.unmarshalString(openXML);
        return tc;
    }


    public static Tc createTC_startRating() throws JAXBException {
        String openXML = "<w:tc xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
                + "<w:tcPr>"
                + "<w:tcW w:type=\"dxa\" w:w=\"1902\"/>"
                + "</w:tcPr>"
                + "<w:p>"
                + "<w:pPr>"
                + "<w:jc w:val=\"center\"/>"
                + "<w:rPr>"
                + "<w:rFonts w:ascii=\"Times New Roman\" w:cs=\"Times New Roman\" w:hAnsi=\"Times New Roman\"/>"
                + "<w:lang w:val=\"en-US\"/>"
                + "</w:rPr>"
                + "</w:pPr>"
                + "<w:proofErr w:type=\"spellStart\"/>"
                + "<w:r>"
                + "<w:rPr>"
                + "<w:rFonts w:ascii=\"Times New Roman\" w:cs=\"Times New Roman\" w:hAnsi=\"Times New Roman\"/>"
                + "<w:lang w:val=\"en-US\"/>"
                + "</w:rPr>"
                + "<w:t>startRating</w:t>"
                + "</w:r>"
                + "<w:proofErr w:type=\"spellEnd\"/>"
                + "</w:p>"
                + "</w:tc>";
        Tc tc = (Tc) XmlUtils.unmarshalString(openXML);
        return tc;
    }


    public static Tc createTC_endRating() throws JAXBException {
        String openXML = "<w:tc xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
                + "<w:tcPr>"
                + "<w:tcW w:type=\"dxa\" w:w=\"1869\"/>"
                + "</w:tcPr>"
                + "<w:p>"
                + "<w:pPr>"
                + "<w:jc w:val=\"center\"/>"
                + "<w:rPr>"
                + "<w:rFonts w:ascii=\"Times New Roman\" w:cs=\"Times New Roman\" w:hAnsi=\"Times New Roman\"/>"
                + "<w:lang w:val=\"en-US\"/>"
                + "</w:rPr>"
                + "</w:pPr>"
                + "<w:proofErr w:type=\"spellStart\"/>"
                + "<w:r>"
                + "<w:rPr>"
                + "<w:rFonts w:ascii=\"Times New Roman\" w:cs=\"Times New Roman\" w:hAnsi=\"Times New Roman\"/>"
                + "<w:lang w:val=\"en-US\"/>"
                + "</w:rPr>"
                + "<w:t>endRating</w:t>"
                + "</w:r>"
                + "<w:proofErr w:type=\"spellEnd\"/>"
                + "</w:p>"
                + "</w:tc>";
        Tc tc = (Tc) XmlUtils.unmarshalString(openXML);
        return tc;
    }

    public static Tr createTR_childRow() throws JAXBException {
        String openXML = "<w:tr xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
                + "<w:trPr>"
                + "<w:jc w:val=\"center\"/>"
                + "</w:trPr>"
                + "<w:tc>"
                + "<w:tcPr>"
                + "<w:tcW w:type=\"dxa\" w:w=\"2583\"/>"
                + "</w:tcPr>"
                + "<w:p>"
                + "<w:pPr>"
                + "<w:jc w:val=\"center\"/>"
                + "<w:rPr>"
                + "<w:rFonts w:ascii=\"Times New Roman\" w:cs=\"Times New Roman\" w:hAnsi=\"Times New Roman\"/>"
                + "<w:lang w:val=\"en-US\"/>"
                + "</w:rPr>"
                + "</w:pPr>"
                + "<w:proofErr w:type=\"spellStart\"/>"
                + "<w:r>"
                + "<w:rPr>"
                + "<w:rFonts w:ascii=\"Times New Roman\" w:cs=\"Times New Roman\" w:hAnsi=\"Times New Roman\"/>"
                + "<w:lang w:val=\"en-US\"/>"
                + "</w:rPr>"
                + "<w:t>childFullName</w:t>"
                + "</w:r>"
                + "<w:proofErr w:type=\"spellEnd\"/>"
                + "</w:p>"
                + "</w:tc>"
                + "<w:tc>"
                + "<w:tcPr>"
                + "<w:tcW w:type=\"dxa\" w:w=\"1902\"/>"
                + "</w:tcPr>"
                + "<w:p>"
                + "<w:pPr>"
                + "<w:jc w:val=\"center\"/>"
                + "<w:rPr>"
                + "<w:rFonts w:ascii=\"Times New Roman\" w:cs=\"Times New Roman\" w:hAnsi=\"Times New Roman\"/>"
                + "<w:lang w:val=\"en-US\"/>"
                + "</w:rPr>"
                + "</w:pPr>"
                + "<w:proofErr w:type=\"spellStart\"/>"
                + "<w:r>"
                + "<w:rPr>"
                + "<w:rFonts w:ascii=\"Times New Roman\" w:cs=\"Times New Roman\" w:hAnsi=\"Times New Roman\"/>"
                + "<w:lang w:val=\"en-US\"/>"
                + "</w:rPr>"
                + "<w:t>startRating</w:t>"
                + "</w:r>"
                + "<w:proofErr w:type=\"spellEnd\"/>"
                + "</w:p>"
                + "</w:tc>"
                + "<w:tc>"
                + "<w:tcPr>"
                + "<w:tcW w:type=\"dxa\" w:w=\"1869\"/>"
                + "</w:tcPr>"
                + "<w:p>"
                + "<w:pPr>"
                + "<w:jc w:val=\"center\"/>"
                + "<w:rPr>"
                + "<w:rFonts w:ascii=\"Times New Roman\" w:cs=\"Times New Roman\" w:hAnsi=\"Times New Roman\"/>"
                + "<w:lang w:val=\"en-US\"/>"
                + "</w:rPr>"
                + "</w:pPr>"
                + "<w:proofErr w:type=\"spellStart\"/>"
                + "<w:r>"
                + "<w:rPr>"
                + "<w:rFonts w:ascii=\"Times New Roman\" w:cs=\"Times New Roman\" w:hAnsi=\"Times New Roman\"/>"
                + "<w:lang w:val=\"en-US\"/>"
                + "</w:rPr>"
                + "<w:t>endRating</w:t>"
                + "</w:r>"
                + "<w:proofErr w:type=\"spellEnd\"/>"
                + "</w:p>"
                + "</w:tc>"
                + "</w:tr>";
        Tr tr = (Tr) XmlUtils.unmarshalString(openXML);
        return tr;
    }

    public static Tc createTC_MidTitle() throws JAXBException {
        String openXML = "<w:tc xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
                + "<w:tcPr>"
                + "<w:tcW w:type=\"dxa\" w:w=\"3771\"/>"
                + "<w:gridSpan w:val=\"2\"/>"
                + "<w:tcBorders>"
                + "<w:left w:color=\"auto\" w:space=\"0\" w:sz=\"4\" w:val=\"single\"/>"
                + "</w:tcBorders>"
                + "</w:tcPr>"
                + "<w:p>"
                + "<w:pPr>"
                + "<w:jc w:val=\"center\"/>"
                + "<w:rPr>"
                + "<w:rFonts w:ascii=\"Times New Roman\" w:cs=\"Times New Roman\" w:hAnsi=\"Times New Roman\"/>"
                + "</w:rPr>"
                + "</w:pPr>"
                + "<w:r>"
                + "<w:rPr>"
                + "<w:rFonts w:ascii=\"Times New Roman\" w:cs=\"Times New Roman\" w:hAnsi=\"Times New Roman\"/>"
                + "</w:rPr>"
                + "<w:t>Средний рейтинг</w:t>"
                + "</w:r>"
                + "</w:p>"
                + "</w:tc>";
        Tc tc = (Tc)XmlUtils.unmarshalString(openXML);
        return tc;
    }


    public static Tc createTC_MidStartYear() throws JAXBException {
        String openXML = "<w:tc xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
                + "<w:tcPr>"
                + "<w:tcW w:type=\"dxa\" w:w=\"1885\"/>"
                + "</w:tcPr>"
                + "<w:p>"
                + "<w:pPr>"
                + "<w:jc w:val=\"center\"/>"
                + "<w:rPr>"
                + "<w:rFonts w:ascii=\"Times New Roman\" w:cs=\"Times New Roman\" w:hAnsi=\"Times New Roman\"/>"
                + "</w:rPr>"
                + "</w:pPr>"
                + "<w:r>"
                + "<w:rPr>"
                + "<w:rFonts w:ascii=\"Times New Roman\" w:cs=\"Times New Roman\" w:hAnsi=\"Times New Roman\"/>"
                + "</w:rPr>"
                + "<w:t>Начало года</w:t>"
                + "</w:r>"
                + "<w:bookmarkStart w:id=\"0\" w:name=\"_GoBack\"/>"
                + "<w:bookmarkEnd w:id=\"0\"/>"
                + "</w:p>"
                + "</w:tc>";
        Tc tc = (Tc)XmlUtils.unmarshalString(openXML);
        return tc;
    }

    public static Tc createTC_MidEndYear() throws JAXBException {
        String openXML = "<w:tc xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
                + "<w:tcPr>"
                + "<w:tcW w:type=\"dxa\" w:w=\"1886\"/>"
                + "</w:tcPr>"
                + "<w:p>"
                + "<w:pPr>"
                + "<w:jc w:val=\"center\"/>"
                + "<w:rPr>"
                + "<w:rFonts w:ascii=\"Times New Roman\" w:cs=\"Times New Roman\" w:hAnsi=\"Times New Roman\"/>"
                + "</w:rPr>"
                + "</w:pPr>"
                + "<w:r>"
                + "<w:rPr>"
                + "<w:rFonts w:ascii=\"Times New Roman\" w:cs=\"Times New Roman\" w:hAnsi=\"Times New Roman\"/>"
                + "</w:rPr>"
                + "<w:t>Конец года</w:t>"
                + "</w:r>"
                + "</w:p>"
                + "</w:tc>";
        Tc tc = (Tc)XmlUtils.unmarshalString(openXML);
        return tc;
    }

    public static Tc createTC_MidStartRating() throws JAXBException {
        String openXML = "<w:tc xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
                + "<w:tcPr>"
                + "<w:tcW w:type=\"dxa\" w:w=\"1885\"/>"
                + "</w:tcPr>"
                + "<w:p>"
                + "<w:pPr>"
                + "<w:jc w:val=\"center\"/>"
                + "<w:rPr>"
                + "<w:rFonts w:ascii=\"Times New Roman\" w:cs=\"Times New Roman\" w:hAnsi=\"Times New Roman\"/>"
                + "<w:lang w:val=\"en-US\"/>"
                + "</w:rPr>"
                + "</w:pPr>"
                + "<w:proofErr w:type=\"spellStart\"/>"
                + "<w:r>"
                + "<w:rPr>"
                + "<w:rFonts w:ascii=\"Times New Roman\" w:cs=\"Times New Roman\" w:hAnsi=\"Times New Roman\"/>"
                + "<w:lang w:val=\"en-US\"/>"
                + "</w:rPr>"
                + "<w:t>startMidRating</w:t>"
                + "</w:r>"
                + "<w:proofErr w:type=\"spellEnd\"/>"
                + "</w:p>"
                + "</w:tc>";
        Tc tc = (Tc)XmlUtils.unmarshalString(openXML);
        return tc;
    }


    public static Tc createTC_MidEndRating() throws JAXBException {
        String openXML = "<w:tc xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
                + "<w:tcPr>"
                + "<w:tcW w:type=\"dxa\" w:w=\"1886\"/>"
                + "</w:tcPr>"
                + "<w:p>"
                + "<w:pPr>"
                + "<w:jc w:val=\"center\"/>"
                + "<w:rPr>"
                + "<w:rFonts w:ascii=\"Times New Roman\" w:cs=\"Times New Roman\" w:hAnsi=\"Times New Roman\"/>"
                + "<w:lang w:val=\"en-US\"/>"
                + "</w:rPr>"
                + "</w:pPr>"
                + "<w:r>"
                + "<w:rPr>"
                + "<w:rFonts w:ascii=\"Times New Roman\" w:cs=\"Times New Roman\" w:hAnsi=\"Times New Roman\"/>"
                + "<w:lang w:val=\"en-US\"/>"
                + "</w:rPr>"
                + "<w:t>endMidRating</w:t>"
                + "</w:r>"
                + "</w:p>"
                + "</w:tc>";
        Tc tc = (Tc)XmlUtils.unmarshalString(openXML);
        return tc;
    }

}
