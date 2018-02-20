package matchtool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author eduardo
 */
public class MatchTool {
    public static void run(String templatePath) {
        HashMap<String, LinkedList<Match>> templateMatchListMap = new HashMap<>();
        HashSet<String> referenceSet = new HashSet<>();
        
        try(BufferedReader br = new BufferedReader(new FileReader(new File(templatePath)))) {
            String line;
            
            //Leitura do gabarito, linha por linha
            while((line = br.readLine()) != null) {
                System.out.println(line);
                String[] split = line.split(" ");
                
                String sample = split[0];
                long matchBeginSample = Long.parseLong(split[1]);
                String reference = split[2];
                long matchBeginReference = Long.parseLong(split[3]);
                int duration = Integer.parseInt(split[4]);
                
                //Mantém um conjunto com todos os extRefs de referência
                referenceSet.add(reference);
                
                LinkedList<Match> templateMatchList = templateMatchListMap.get(sample);
                if(templateMatchList == null) {
                    templateMatchList = new LinkedList<>();
                    templateMatchListMap.put(sample, templateMatchList);
                }
                
                //Mantém uma lista de todos os matches esperados
                templateMatchList.add(
                    new Match(
                        sample, matchBeginSample, duration,
                        reference, matchBeginReference, duration
                    )
                );
            }
            
            Set<String> sampleSet = templateMatchListMap.keySet();
            for(String sample : sampleSet) {
                List<Match> templateMatchList = templateMatchListMap.get(sample);

                for(Match templateMatch : templateMatchList) {
                    List<Match> searchMatchList = Searcher.run(sample, templateMatch.sampleBegin, templateMatch.sampleBegin + templateMatch.sampleDuration, referenceSet, 1);

                    for(Match searchMatch : searchMatchList) {
                        boolean overlaps = Match.compareMatch(searchMatch, templateMatch);
                        searchMatch.markAsMatched(overlaps);
                        templateMatch.markAsMatched(overlaps);

                        //Falso positivo: match encontrado não está no gabarito
                        if(!searchMatch.isMatched()) {
                            System.out.println("----------------------------");
                            System.out.println("Falso positivo: " + sample + " " + searchMatch.toStringDate());
                        }
                    }

                    //Falso Negativo: match do gabarito não foi encontrado
                    if(!templateMatch.isMatched()) {
                        System.out.println("----------------------------");
                        System.out.println("Falso negativo: " + sample + " " + templateMatch.toStringDate());
                    }
                }
            }
        }
        catch(FileNotFoundException ex) {
            Logger.getLogger(MatchTool.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (IOException ex) {
            Logger.getLogger(MatchTool.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (InterruptedException ex) {
            Logger.getLogger(MatchTool.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}