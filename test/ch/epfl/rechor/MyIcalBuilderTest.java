package ch.epfl.rechor;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MyIcalBuilderTest {
    @Test
    void testBasicEventCreation() {
        IcalBuilder builder = new IcalBuilder();
        String result = builder
                .begin(IcalBuilder.Component.VCALENDAR)
                .add(IcalBuilder.Name.PRODID, "-//EPFL//NONSGML Event//EN")
                .add(IcalBuilder.Name.VERSION, "2.0")
                .begin(IcalBuilder.Component.VEVENT)
                .add(IcalBuilder.Name.UID, "123456789")
                .add(IcalBuilder.Name.DTSTAMP, LocalDateTime.of(2025, 3, 1, 12, 0))
                .add(IcalBuilder.Name.DTSTART, LocalDateTime.of(2025, 3, 5, 14, 0))
                .add(IcalBuilder.Name.DTEND, LocalDateTime.of(2025, 3, 5, 16, 0))
                .add(IcalBuilder.Name.SUMMARY, "Meeting")
                .end() // VEVENT
                .end() // VCALENDAR
                .build();

        assertTrue(result.contains("BEGIN:VCALENDAR"));
        assertTrue(result.contains("BEGIN:VEVENT"));
        assertTrue(result.contains("SUMMARY:Meeting"));
        assertTrue(result.contains("END:VEVENT"));
        assertTrue(result.contains("END:VCALENDAR"));
    }

    @Test
    void testAddStringValue() {
        IcalBuilder builder = new IcalBuilder();
        String result = builder.add(IcalBuilder.Name.SUMMARY, "A simple test event").build();

        assertTrue(result.contains("SUMMARY:A simple test event"));
    }


    @Test
    void testBeginAndEndComponent() {
        IcalBuilder builder = new IcalBuilder();
        String result = builder.begin(IcalBuilder.Component.VCALENDAR)
                .begin(IcalBuilder.Component.VEVENT)
                .end()
                .end()
                .build();

        assertTrue(result.contains("BEGIN:VCALENDAR"));
        assertTrue(result.contains("BEGIN:VEVENT"));
        assertTrue(result.contains("END:VEVENT"));
        assertTrue(result.contains("END:VCALENDAR"));
    }

    @Test
    void testEndWithoutBeginThrowsException() {
        IcalBuilder builder = new IcalBuilder();
        assertThrows(IllegalArgumentException.class, builder::end);
    }

    @Test
    void testUnclosedComponentThrowsException() {
        IcalBuilder builder = new IcalBuilder();
        builder.begin(IcalBuilder.Component.VCALENDAR);
        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    void testLongLineFolding() {
        IcalBuilder builder = new IcalBuilder();
        String longText = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.";
        String result = builder.add(IcalBuilder.Name.DESCRIPTION, longText).build();

        assertTrue(result.contains("\r\n "));
    }
    @Test
    void testMultipleFields() {
        IcalBuilder builder = new IcalBuilder();
        String result = builder
                .add(IcalBuilder.Name.SUMMARY, "Meeting")
                .add(IcalBuilder.Name.DESCRIPTION, "Discuss project updates")
                .build();

        assertTrue(result.contains("SUMMARY:Meeting"));
        assertTrue(result.contains("DESCRIPTION:Discuss project updates"));
    }
    @Test
    void testEventWithoutStartDate() {
        IcalBuilder builder = new IcalBuilder();
        String result = builder
                .begin(IcalBuilder.Component.VEVENT)
                .add(IcalBuilder.Name.SUMMARY, "No start date")
                .end()
                .build();

        assertTrue(result.contains("BEGIN:VEVENT"));
        assertTrue(result.contains("SUMMARY:No start date"));
        assertTrue(result.contains("END:VEVENT"));
    }
    @Test
    void addThrowsExceptionWhenNameOrValueIsNull() {
        IcalBuilder builder = new IcalBuilder();
        assertThrows(NullPointerException.class, () -> builder.add(null, "value"));
        assertThrows(NullPointerException.class, () -> builder.add(IcalBuilder.Name.SUMMARY, (String) null));
    }

    @Test
    void addAddsLinesCorrectly() {
        IcalBuilder builder = new IcalBuilder();
        builder.add(IcalBuilder.Name.SUMMARY, "Test Event");
        String result = builder.build();
        assertTrue(result.contains("SUMMARY:Test Event"));
    }

    @Test
    void addDateTimeFormatsCorrectly() {
        IcalBuilder builder = new IcalBuilder();
        LocalDateTime time = LocalDateTime.of(2025, 2, 28, 14, 30, 0);
        builder.add(IcalBuilder.Name.DTSTART, time);
        String result = builder.build();
        System.out.println(result);
        assertTrue(result.contains("DTSTART:20250228T143000"));

    }

    @Test
    void beginAndEndWorkCorrectly() {
        IcalBuilder builder = new IcalBuilder();
        builder.begin(IcalBuilder.Component.VCALENDAR);
        builder.begin(IcalBuilder.Component.VEVENT);
        builder.end();
        builder.end();
        String result = builder.build();
        assertTrue(result.contains("BEGIN:VCALENDAR"));
        assertTrue(result.contains("END:VCALENDAR"));
        assertTrue(result.contains("BEGIN:VEVENT"));
        assertTrue(result.contains("END:VEVENT"));
    }

    @Test
    void endThrowsExceptionWhenNoBeginCalled() {
        IcalBuilder builder = new IcalBuilder();
        assertThrows(IllegalArgumentException.class, builder::end);
    }

    @Test
    void buildThrowsExceptionWhenUnmatchedBeginExists() {
        IcalBuilder builder = new IcalBuilder();
        builder.begin(IcalBuilder.Component.VCALENDAR);
        assertThrows(IllegalArgumentException.class, builder::build);
    }






}
