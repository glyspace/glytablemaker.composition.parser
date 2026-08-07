package org.glyspace.glytablemaker.composition;

public enum ResidueType
{

    HEX("Hex"), GLC("Glc"), GAL("Gal"), MAN("Man"), HEXNAC("HexNAc"), GLCNAC("GlcNAc"), GALNAC(
            "GalNAc"), DHEX("dHex"), FUC("Fuc"), NONUA3D("NulO"), KDN("Kdn"), NEUAC(
                    "Neu5Ac"), NEUGC("Neu5Gc"), NEU("Neu"), KDO("Kdo"), HEXN("HexN"), GLCN(
                            "GlcN"), HEXA("HexA"), GLCA("GlcA"), IDOA("IdoA"), PEN("Pen"), ARA(
                                    "Ara"), XYL("Xyl"), RIB("Rib"), SULFATE(
                                            "S"), PHOSPHATE("P"), Ac("Ac"), PME("Me");

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