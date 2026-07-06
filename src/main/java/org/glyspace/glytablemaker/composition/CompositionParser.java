package org.glyspace.glytablemaker.composition;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CompositionParser
{

    // help class to store results from parsing the composition count. It stores
    // the actual count and the next position to continue parsing after the
    // count
    private class CountParseResult
    {
        private Integer m_count;
        private Integer m_nextPosistion;

        CountParseResult(Integer a_count, Integer a_nextPos)
        {
            this.setCount(a_count);
            this.setNextPosition(a_nextPos);
        }

        public Integer getCount()
        {
            return m_count;
        }

        public void setCount(Integer a_count)
        {
            m_count = a_count;
        }

        public Integer getNextPosition()
        {
            return m_nextPosistion;
        }

        public void setNextPosition(Integer a_nextPos)
        {
            m_nextPosistion = a_nextPos;
        }
    }

    private String m_sequence = null;
    private CompositionFormat m_format = null;
    private List<String> m_possibleResidues = null;

    public Map<ResidueType, Integer> parse(String a_input, CompositionFormat a_format)
    {
        this.m_sequence = a_input;
        this.m_format = a_format;
        this.m_possibleResidues = new ArrayList<String>(
                this.m_format.getStringToResidue().keySet());
        // parse result object
        Map<ResidueType, Integer> t_result = new LinkedHashMap<ResidueType, Integer>();
        // parse the input recursively start at indix 0
        boolean t_success = this.parseRecursive(0, t_result, new ArrayList<String>());
        // was there a path the was successful?
        if (!t_success)
        {
            return null;
        }
        return t_result;
    }

    private boolean parseRecursive(int a_currentPosition, Map<ResidueType, Integer> a_resultList,
            List<String> a_usedResidues)
    {

        // success condition
        if (a_currentPosition == this.m_sequence.length())
        {
            return true;
        }
        // try ALL possible matches
        for (String t_residueText : this.m_possibleResidues)
        {
            // does the residue text match the text at the current position?
            if (this.m_sequence.startsWith(t_residueText, a_currentPosition))
            {
                // if the residue was already used it can not appear again
                if (!a_usedResidues.contains(t_residueText))
                {
                    // find the position after the residue and try to extract
                    // the count
                    int t_newPosition = a_currentPosition + t_residueText.length();
                    CountParseResult t_countResult = this.parseCount(t_newPosition);

                    // was a count found?
                    if (t_countResult != null)
                    {
                        Integer t_residueCount = t_countResult.getCount();
                        t_newPosition = t_countResult.getNextPosition();

                        // check if residue fits the specified order
                        if (this.isOrderValid(a_usedResidues, t_residueText))
                        {
                            // apply choice
                            ResidueType t_residueType = this.m_format.getStringToResidue()
                                    .get(t_residueText);
                            a_resultList.put(t_residueType, t_residueCount);
                            a_usedResidues.add(t_residueText);

                            // continue path recursively
                            if (this.parseRecursive(t_newPosition, a_resultList, a_usedResidues))
                            {
                                // this path finished successful
                                return true;
                            }
                            else
                            {
                                // none of the possible paths from here finished
                                // successful --> BACKTRACK, remove the residue
                                // from the result list and the used residues
                                a_resultList.remove(t_residueType);
                                a_usedResidues.remove(a_usedResidues.size() - 1);
                            }
                        }
                    }
                }
            }
        }
        // no valid parsing path from this step
        return false;
    }

    // parse the residue count. Depending on the format settings its possible
    // that 1 can be omitted and the number can be encapsulated by delimiters
    private CountParseResult parseCount(int a_currentPosition)
    {

        if (a_currentPosition >= this.m_sequence.length())
        {
            // we reached end of input. If omission is allowed this means count
            // = 1
            if (this.m_format.isAllowNumberOmission())
            {
                return new CountParseResult(1, a_currentPosition);
            }
            else
            {
                // number is required but was not found, not a valid parse path
                return null;
            }
        }
        int t_newPosition = a_currentPosition;
        // do we expect a delimiter?
        if (this.m_format.getOpeningDelimiter() != null)
        {
            // yes, can we find delimiter?
            if (this.m_sequence.startsWith(this.m_format.getOpeningDelimiter(), t_newPosition))
            {
                // find the position after the delimiter
                t_newPosition = t_newPosition + this.m_format.getOpeningDelimiter().length();
                int t_endPosition = t_newPosition;
                // parse till we reach last character or till current character
                // is not a digit
                while (t_endPosition < this.m_sequence.length()
                        && Character.isDigit(this.m_sequence.charAt(t_endPosition)))
                {
                    t_endPosition++;
                }
                // was there at least one digit?
                if (t_endPosition == t_newPosition)
                {
                    // invalid parse path we expect a number after the opening
                    // delimiter
                    return null;
                }
                // do we expect to see a closing delimiter?
                if (this.m_format.getClosingDelimiter() != null)
                {
                    // did we reach end of sequence without finding the closing
                    // delimiter?
                    if (t_endPosition >= this.m_sequence.length())
                    {
                        // invalid parse path
                        return null;
                    }
                    // can we find the closing delimiter now?
                    if (this.m_sequence.startsWith(this.m_format.getClosingDelimiter(),
                            t_endPosition))
                    {
                        // found delimiter, return count and set next position
                        // after delimiter
                        int t_residueCount = Integer
                                .parseInt(this.m_sequence.substring(t_newPosition, t_endPosition));
                        return new CountParseResult(t_residueCount,
                                t_endPosition + this.m_format.getClosingDelimiter().length());
                    }
                    else
                    {
                        // closing delimiter not found, invalid parse path
                        return null;
                    }
                }
                else
                {
                    // no, return the count
                    int t_residueCount = Integer
                            .parseInt(this.m_sequence.substring(t_newPosition, t_endPosition));
                    return new CountParseResult(t_residueCount, t_endPosition);
                }
            }
            else
            {
                // delimiter not found, is 1 omission allowed?
                if (this.m_format.isAllowNumberOmission())
                {
                    // yes, count = 1
                    return new CountParseResult(1, a_currentPosition);
                }
                else
                {
                    // no, invalid parse path
                    return null;
                }
            }
        }
        // no delimiters expected
        int t_endPosition = t_newPosition;
        // parse till we reach last character or till current character
        // is not a digit
        while (t_endPosition < this.m_sequence.length()
                && Character.isDigit(this.m_sequence.charAt(t_endPosition)))
        {
            t_endPosition++;
        }
        // was there at least one digit?
        if (t_endPosition == t_newPosition)
        {
            // no, is 1 omission allowed?
            if (this.m_format.isAllowNumberOmission())
            {
                return new CountParseResult(1, t_endPosition);
            }
            else
            {
                // number is required but was not found, not a valid parse path
                return null;
            }
        }
        // return the count
        int t_residueCount = Integer
                .parseInt(this.m_sequence.substring(t_newPosition, t_endPosition));
        return new CountParseResult(t_residueCount, t_endPosition);
    }

    private boolean isOrderValid(List<String> a_usedResidues, String a_currentResidue)
    {
        // if there is no order specified all paths are valid
        if (this.m_format.getAllowedOrder() == null)
        {
            return true;
        }
        // if its the first residue the path is valid
        if (a_usedResidues.isEmpty())
        {
            return true;
        }
        // get the last residue that we added to the used list
        String t_lastResidue = a_usedResidues.get(a_usedResidues.size() - 1);
        // get the positions of the last residue and the current residue in the
        // order list
        int t_lastIndex = this.m_format.getAllowedOrder().indexOf(t_lastResidue);
        int t_currentIndex = this.m_format.getAllowedOrder().indexOf(a_currentResidue);
        // if the index of the current residue is after the last residue => all
        // good
        return t_currentIndex >= t_lastIndex;
    }
}