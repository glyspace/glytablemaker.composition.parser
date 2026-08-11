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

import org.eurocarbdb.MolecularFramework.sugar.Anomer;
import org.eurocarbdb.MolecularFramework.sugar.GlycoNode;
import org.eurocarbdb.MolecularFramework.sugar.GlycoconjugateException;
import org.eurocarbdb.MolecularFramework.sugar.Monosaccharide;
import org.eurocarbdb.MolecularFramework.sugar.Sugar;
import org.glycoinfo.GlycanCompositionConverter.conversion.CompositionConverter;
import org.glycoinfo.GlycanCompositionConverter.conversion.ConversionException;
import org.glycoinfo.GlycanCompositionConverter.structure.Composition;
import org.glycoinfo.GlycanCompositionConverter.utils.CompositionUtils;
import org.glycoinfo.GlycanFormatconverter.util.exchange.SugarToWURCSGraph.SugarToWURCSGraph;
import org.glycoinfo.WURCSFramework.util.WURCSException;
import org.glycoinfo.WURCSFramework.util.WURCSFactory;
import org.glycoinfo.WURCSFramework.util.exchange.WURCSExchangeException;
import org.glycoinfo.WURCSFramework.util.subsumption.WURCSSubsumptionConverter;
import org.glycoinfo.WURCSFramework.wurcs.array.LIN;
import org.glycoinfo.WURCSFramework.wurcs.array.MS;
import org.glycoinfo.WURCSFramework.wurcs.array.RES;
import org.glycoinfo.WURCSFramework.wurcs.array.UniqueRES;
import org.glycoinfo.WURCSFramework.wurcs.array.WURCSArray;

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

    /*
    public static void changeCompositionType () {
    	switch (compositionType) {
        case BASE:
        	Composition compo = CompositionUtils.parse(g.getComposition().trim());
        	strWURCS = CompositionConverter.toWURCS(compo);
        	strWURCS = toUnknownForm(strWURCS);
        	break;
        case GLYGEN:
        	compo = CompositionUtils.parse(g.getComposition().trim());
        	SugarToWURCSGraph t_s2w = new SugarToWURCSGraph();
			try {
				Sugar sugar = CompositionConverter.toSugar(compo);
				for (GlycoNode node: sugar.getNodes()) {
					if (node instanceof Monosaccharide) {
						Monosaccharide m = ((Monosaccharide)node);
						m.setRing(-1, -1);
						m.setAnomer(Anomer.Unknown);
					}
				}
				t_s2w.start(sugar);
			} catch (WURCSExchangeException | GlycoconjugateException e) {
				throw new ConversionException("Error in converting composition to Sugar object.", e);
			}
			try {
				WURCSFactory factory = new WURCSFactory(t_s2w.getGraph());
				strWURCS = factory.getWURCS();
			} catch (WURCSException e) {
				throw new ConversionException("Error in encoding the composition in WURCS.", e);
			}
        	break;
        case DEFINED:
        	compo = CompositionUtils.parse(g.getComposition().trim());
        	strWURCS = CompositionConverter.toWURCS(compo);
        	break;
    }*/
    	
	public static String toUnknownForm(String strWURCS) throws WURCSException {

        WURCSFactory factory = new WURCSFactory(strWURCS);
        WURCSArray array = factory.getArray();

        // copy array without UniqueRES
        WURCSArray arrayNew = new WURCSArray(
            array.getVersion(),
            array.getUniqueRESCount(),
            array.getRESCount(),
            array.getLINCount(),
            array.isComposition()
        );

        for (RES res : array.getRESs())
          arrayNew.addRES(res);

        for ( LIN lin : array.getLINs() )
          arrayNew.addLIN(lin);

        // add UniqueRES converted to unknown form
        WURCSSubsumptionConverter converter = new WURCSSubsumptionConverter();
        for (UniqueRES ures : array.getUniqueRESs()) {
          MS ms = converter.convertAnomericCarbonToUncertain(ures);
          UniqueRES uresNew = new UniqueRES(ures.getUniqueRESID(), ms);
          arrayNew.addUniqueRES(uresNew);
        }

        // back to WURCS
        WURCSFactory factoryNew = new WURCSFactory(arrayNew);
        return factoryNew.getWURCS();
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