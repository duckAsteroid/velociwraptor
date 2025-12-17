package com.asteroid.duck.velociwraptor.template.section;

import junit.framework.TestCase;

import java.io.StringReader;
import java.util.List;
import java.util.stream.Stream;

public class SectionParserTest extends TestCase {

    public static final String TEST1 = """
            Hello ${Greeting} - this is an ordinary template section.
            #no-template
            This is neither passed to the template engine - nor the output. It's like a comment...
            ${Greeting} has no special meaning here.
            #end-template
            ${Greeting} has no special meaning here, but this string is copied to the output.
            """;

    public void testStream() {
        StringReader reader = new StringReader(TEST1);
        SectionParser parser = new SectionParser(reader);
        List<TemplateSection> sectionList;
        try(Stream<TemplateSection> sections = SectionParser.stream(parser)) {
            sectionList = sections.toList();
        }

        assertNotNull(sectionList);
        assertEquals(2, sectionList.size());

        assertTrue(parser.isClosed());

        var first = sectionList.get(0);
        assertTrue(first.template());
        var second = sectionList.get(1);
        assertFalse(second.template());
    }
}