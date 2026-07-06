package org.glyspace.glytablemaker.composition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestParser
{

    public static void main(String[] args) throws JsonProcessingException
    {
        // define the composition format
        CompositionFormat t_format = new CompositionFormat();
        // which Strings in the composition language map to which
        // GlyTableMakerResidue
        Map<String, ResidueType> t_stringToResidue = new HashMap<String, ResidueType>();
        t_stringToResidue.put("SA", ResidueType.HEX);
        t_stringToResidue.put("S", ResidueType.NEUAC);
        t_stringToResidue.put("A", ResidueType.HEXNAC);
        t_stringToResidue.put("s", ResidueType.SULFATE);
        t_format.setStringToResidue(t_stringToResidue);
        // what is the allowed order for residue strings, if no order specified
        // put null
        List<String> t_order = new ArrayList<String>();
        t_order.add("SA");
        t_order.add("S");
        t_order.add("A");
        t_format.setAllowedOrder(t_order);
        // is the omission of "1" as residue count allowed?
        t_format.setAllowNumberOmission(true);
        // are there delimiters around numbers? null if not
        t_format.setOpeningDelimiter("(");
        t_format.setClosingDelimiter(")");

        // write and read from JSON
        ObjectMapper t_jsonMapper = new ObjectMapper();
        String t_json = t_jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(t_format);
        System.out.println(t_json);
        CompositionFormat t_format2 = t_jsonMapper.readValue(t_json, CompositionFormat.class);

        // create the parser and parse the input string with the defined format
        String t_sequence = "SASA(2)";
        CompositionParser t_parser = new CompositionParser();
        Map<ResidueType, Integer> t_result = t_parser.parse(t_sequence, t_format2);
        // was parsing successful?
        if (t_result != null)
        {

            // print out the results using the "WURCS" name and the number
            for (ResidueType t_component : t_result.keySet())
            {
                System.out.println(t_component.getWurcsName() + " = " + t_result.get(t_component));
            }
        }
        else
        {
            // parsing failed
            System.out.println(
                    "Unable to parse " + t_sequence + " with the specified format instructions.");
        }
    }
}