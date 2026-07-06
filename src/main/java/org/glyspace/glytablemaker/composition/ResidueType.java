package org.glyspace.glytablemaker.composition;

public enum ResidueType
{

    HEX("Hexose"), HEXNAC("HexNAc"), NEUAC("Neu5Ac"), SULFATE("Sulfate");

    private final String m_wurcsName;

    private ResidueType(String a_name)
    {
        this.m_wurcsName = a_name;
    }

    public String getWurcsName()
    {
        return this.m_wurcsName;
    }
}