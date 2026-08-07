package org.glyspace.glytablemaker.composition;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.glycoinfo.GlycanCompositionConverter.conversion.CompositionConverter;
import org.glycoinfo.GlycanCompositionConverter.structure.Composition;
import org.glycoinfo.GlycanCompositionConverter.utils.CompositionUtils;

public class Composition2WURCS
{

    public static void main(String[] args) throws IOException
    {
        // build the composition format
        CompositionFormat t_format = Composition2WURCS.buildFormat();
        // read all lines from the input file
        List<String> t_allLines = Files.readAllLines(Paths.get("data/input.txt"));
        // create the parser and parse the input string with the defined format
        CompositionParser t_parser = new CompositionParser();
        BufferedWriter t_outputFile = new BufferedWriter(new FileWriter("data/output.txt"));
        Integer t_lineNumber = 0;
        for (String t_line : t_allLines)
        {
            t_lineNumber++;
            t_line = t_line.trim();
            if (t_line.length() > 0)
            {
                Map<ResidueType, Integer> t_result = t_parser.parse(t_line, t_format);
                // was parsing successful?
                if (t_result != null)
                {
                    String t_wurcs = Composition2WURCS.exportWURCS(t_result, t_lineNumber, t_line);
                    if (t_wurcs != null)
                    {
                        t_outputFile.write(t_wurcs);
                        t_outputFile.newLine();
                    }
                }
                else
                {
                    // parsing failed
                    System.err.println(
                            "Line " + t_lineNumber.toString() + ": Error - Unable to parse "
                                    + t_line + " with the specified format instructions.");
                }
            }
        }
        t_outputFile.close();

    }

    public static String exportWURCS(Map<ResidueType, Integer> a_composition, Integer a_lineNumber,
            String a_oringalString)
    {
        // build the WURCS composition string (e.g. Hex:5|HexNAc:3|S:1)
        StringBuffer t_wurcsComposition = new StringBuffer("");
        for (ResidueType t_residue : a_composition.keySet())
        {
            if (!t_wurcsComposition.isEmpty())
            {
                t_wurcsComposition.append("|");
            }
            t_wurcsComposition
                    .append(t_residue.getWurcsName() + ":" + a_composition.get(t_residue));
        }
        try
        {
            // convert to WURCS
            Composition t_compo = CompositionUtils.parse(t_wurcsComposition.toString());
            String t_wurcsSequence = CompositionConverter.toWURCS(t_compo);
            // need to fix the PMe problem "?}*OC_" >>> "?}*OPOC/3O/3=O_"
            t_wurcsSequence = t_wurcsSequence.replaceAll(Pattern.quote("?}*OC_"),
                    "?}*OPOC/3O/3=O_");
            System.out.println("Line " + a_lineNumber.toString() + ": " + a_oringalString + " >>> "
                    + t_wurcsComposition.toString() + " >>> " + t_wurcsSequence);
            return t_wurcsSequence;
        }
        catch (Exception e)
        {
            // conversion failed
            System.err.println("Line " + a_lineNumber.toString() + ": " + a_oringalString + " >>> "
                    + t_wurcsComposition.toString() + " : WURCS composition parsing error: "
                    + e.getMessage());
            return null;
        }
    }

    public static CompositionFormat buildFormat()
    {
        // define the composition format
        CompositionFormat t_format = new CompositionFormat();
        // which Strings in the composition language map to which
        // GlyTableMakerResidue
        Map<String, ResidueType> t_stringToResidue = new HashMap<String, ResidueType>();
        t_stringToResidue.put("H", ResidueType.HEX);
        t_stringToResidue.put("F", ResidueType.FUC);
        t_stringToResidue.put("N", ResidueType.HEXNAC);
        t_stringToResidue.put("S", ResidueType.SULFATE);
        t_stringToResidue.put("P", ResidueType.PHOSPHATE);
        t_stringToResidue.put("(PMe)", ResidueType.PME);
        t_format.setStringToResidue(t_stringToResidue);
        // what is the allowed order for residue strings, if no order specified
        // put null
        List<String> t_order = new ArrayList<String>();
        t_order.add("H");
        t_order.add("N");
        t_order.add("F");
        t_order.add("S");
        t_order.add("P");
        t_order.add("(PMe)");
        t_format.setAllowedOrder(t_order);
        // is the omission of "1" as residue count allowed?
        t_format.setAllowNumberOmission(true);
        // are there delimiters around numbers? null if not
        t_format.setOpeningDelimiter(null);
        t_format.setClosingDelimiter(null);
        return t_format;
    }
}