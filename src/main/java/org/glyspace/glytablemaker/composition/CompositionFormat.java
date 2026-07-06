package org.glyspace.glytablemaker.composition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompositionFormat
{

    private Map<String, ResidueType> m_stringToResidue = new HashMap<String, ResidueType>();
    private List<String> m_allowedOrder = null;
    private String m_openingDelimiter = null;
    private String m_closingDelimiter = null;
    private boolean m_allowNumberOmission = false;

    public CompositionFormat()
    {
        super();
    }

    public Map<String, ResidueType> getStringToResidue()
    {
        return this.m_stringToResidue;
    }

    public void setStringToResidue(Map<String, ResidueType> a_stringToResidue)
    {
        this.m_stringToResidue = a_stringToResidue;
    }

    public List<String> getAllowedOrder()
    {
        return this.m_allowedOrder;
    }

    public void setAllowedOrder(List<String> a_allowedOrder)
    {
        this.m_allowedOrder = a_allowedOrder;
    }

    public String getOpeningDelimiter()
    {
        return this.m_openingDelimiter;
    }

    public void setOpeningDelimiter(String a_openingDelimiter)
    {
        this.m_openingDelimiter = a_openingDelimiter;
    }

    public String getClosingDelimiter()
    {
        return this.m_closingDelimiter;
    }

    public void setClosingDelimiter(String a_closingDelimiter)
    {
        this.m_closingDelimiter = a_closingDelimiter;
    }

    public boolean isAllowNumberOmission()
    {
        return this.m_allowNumberOmission;
    }

    public void setAllowNumberOmission(boolean a_allowNumberOmission)
    {
        this.m_allowNumberOmission = a_allowNumberOmission;
    }
}