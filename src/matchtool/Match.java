package matchtool;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author eduardo
 */
public class Match {
    private static final int NO_OVERLAP = 0;
    private static final int CONTAINS = 1;
    private static final int IS_CONTAINED = 2;
    private static final int OVERLAPS_BEGIN = 3;
    private static final int OVERLAPS_END = 4;
    
    public String sample;
    public Long sampleBegin;
    public Integer sampleDuration;
    public String reference;
    public Long referenceBegin;
    public Integer referenceDuration;
    private boolean matched;
    
    public Match(String sample, long sampleMatchBegin, int sampleDuration, String reference, long referenceMatchBegin, int referenceDuration) {
        this.sample = sample;
        this.sampleBegin = sampleMatchBegin;
        this.sampleDuration = sampleDuration;
        this.reference = reference;
        this.referenceBegin = referenceMatchBegin;
        this.referenceDuration = referenceDuration;
    }

    @Override
    public String toString() {
        return "Match{" + "sBegin=" + sampleBegin + ", sDur=" + sampleDuration + ", dBegin=" + referenceBegin + ", dDur=" + referenceDuration + ", extRef=" + reference + '}';
    }
    
    public String toStringDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");

        Date sBeginDate = new Date(sampleBegin);
        Date sEndDate = new Date(sampleBegin + sampleDuration);
        Date dBeginDate = new Date(referenceBegin);
        Date dEndDate = new Date(referenceBegin + referenceDuration);
        
        return sample + " " + dateFormat.format(sBeginDate) + " --> " + dateFormat.format(sEndDate) + "\t" +
            reference + " " + dateFormat.format(dBeginDate) + " --> " + dateFormat.format(dEndDate);
            
    }
    
    public void markAsMatched(boolean overlaps) {
        this.matched |= overlaps;
    }
    
    public boolean isMatched() {
        return this.matched;
    }
    
    public static boolean compareMatch(Match searchMatch, Match templateMatch) {
        if(!searchMatch.reference.equals(templateMatch.reference)) {
            return false;
        }
        
        int result = compareLimits(searchMatch.sampleBegin, searchMatch.sampleDuration, templateMatch.sampleBegin, templateMatch.sampleDuration);
        if(result == NO_OVERLAP) {
            return false;
        }
        
        int coveredTime, uncoveredTime, exceedingTime;
        long m1Begin = searchMatch.referenceBegin;
        int m1Dur = searchMatch.referenceDuration;
        long m2Begin = templateMatch.referenceBegin;
        int m2Dur = templateMatch.referenceDuration;
        long m1End = m1Begin + m1Dur;
        long m2End = m2Begin + m2Dur;
        
        switch(result) {
            case CONTAINS:
                uncoveredTime = 0;
                coveredTime = m2Dur;
                exceedingTime = (int) (m2Begin - m1Begin) + (int) (m1End - m2End);
                break;
            case IS_CONTAINED:
                coveredTime = m1Dur;
                uncoveredTime = (int) (m1Begin - m2Begin) + (int) (m2End - m1End);
                exceedingTime = 0;
                break;
            case OVERLAPS_BEGIN:
                uncoveredTime = (int) (m1Begin - m2Begin);
                coveredTime = (int) (m2End - m1Begin);
                exceedingTime = (int) (m1End - m2End);
                break;
            case OVERLAPS_END:
                exceedingTime = (int) (m1Begin - m2Begin);
                coveredTime = (int) (m1End - m2Begin);
                uncoveredTime = (int) (m2End - m1End);
                break;
            default:
                return false;
        }
        
        System.out.println("----------------------------");
        System.out.println(templateMatch.toStringDate() + "\n" + searchMatch.toStringDate());
        System.out.println(String.format("coveredTime: %dms (%.2f)", coveredTime, (double) coveredTime / (double) m2Dur));
        System.out.println(String.format("uncoveredTime: %dms (%.2f)", uncoveredTime, (double) uncoveredTime / (double) m2Dur));
        System.out.println(String.format("exceedingTime: %dms (%.2f)", exceedingTime, (double) exceedingTime / (double) m2Dur));
        
        return true;
    }
        
    private static int compareLimits(long m1Begin, int m1Dur, long m2Begin, int m2Dur) {
        long m1End = m1Begin + m1Dur;
        long m2End = m2Begin + m2Dur;

        //Origem
        //searchMatch contém templateMatch
        if(m1Begin <= m2Begin && m1End >= m2End) {
            return CONTAINS;
        }
        //searchMatch está contido em templateMatch
        else if(m1Begin >= m2Begin && m1End <= m2End) {
            return IS_CONTAINED;
        }
        //searchMatch compartilha início com o final de templateMatch
        else if(m1Begin >= m2Begin && m1Begin <= m2End && m1End >= m2End) {
            return OVERLAPS_BEGIN;
        }
        //searchMatch compartilha final com o início de templateMatch
        else if(m1Begin <= m2Begin && m1End >= m2Begin && m1End <= m2End) {
            return OVERLAPS_END;
        }
        //matches não se sobrepõem
        else {
            return NO_OVERLAP;
        }
    }
}