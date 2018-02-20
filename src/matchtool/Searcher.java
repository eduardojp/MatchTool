package matchtool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.StringTokenizer;

/**
 *
 * @author eduardo
 */
public class Searcher {
    public static LinkedList<Match> run(String sample, long begin, long end, HashSet<String> referenceSet, int profileId) throws IOException, InterruptedException {
        LinkedList<Match> matchList = new LinkedList<>();
        
        LinkedList<String> command = new LinkedList<>();
        command.add("./vhc-searcher");
        command.add("-p");
        command.add(profileId+"");
        command.add("-s");
        command.add("-i");
        command.add(sample);
        command.add("-b");
        command.add(begin + "");
        command.add("-e");
        command.add(end + "");
        command.add("-d");
        command.add("-i");
        command.add(referenceSet.toString().replaceAll("[ \\[\\]]", ""));
        command.add("-c");
        command.add("./config");
        
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        System.out.println(processBuilder.command());
        Process process = processBuilder.start();
        System.out.println("Processo executado");
        System.out.println(processBuilder.command());

        String inputLine;
        BufferedReader processReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        while((inputLine = processReader.readLine()) != null) {
            try {
                System.out.println(">> " + inputLine);

                StringTokenizer lineTokenizer = new StringTokenizer(inputLine);

                if(lineTokenizer.hasMoreElements()) {
                    switch(lineTokenizer.nextElement().toString()) {
                        // INFO [SOURCE_COUNT] [DESTINATION_COUNT]
                        case "INFO":
                            System.out.println("Sample VHCs: " + lineTokenizer.nextElement().toString());
                            System.out.println("Reference VHCs: " + lineTokenizer.nextElement().toString());
                            break;
                        // PROGRESS [VALUE]
                        case "PROGRESS":
                            System.out.println(lineTokenizer.nextElement().toString() + "%");
                            break;
                        // MATCH [SOURCE_BEGIN] [SOURCE_DURATION] [HASHING_ID] [DESTINATION_BEGIN] [DESTINATION_DURATION] [ERRORS_COUNT]
                        case "MATCH":
                            //long sampleMatchBegin, int sampleDuration, String reference, long matchBeginReference, int referenceDuration;
                            long sampleMatchBegin = Long.parseLong(lineTokenizer.nextElement().toString());
                            int sampleDuration = Integer.parseInt(lineTokenizer.nextElement().toString());
                            String reference = lineTokenizer.nextElement().toString();
                            long referenceMatchBegin = Long.parseLong(lineTokenizer.nextElement().toString());
                            int referenceDuration = Integer.parseInt(lineTokenizer.nextElement().toString());
                            
                            matchList.add(
                                new Match(
                                    sample, sampleMatchBegin, sampleDuration,
                                    reference, referenceMatchBegin, referenceDuration
                                )
                            );
                            break;
                        // ERROR [ERROR]
                        case "ERROR":
                            System.out.println(inputLine);
                            break;
                    }
                }
            }
            catch(NumberFormatException ex) {
                ex.printStackTrace();
            }
        }

        // CHECK VHCCOMPUTER RUNNING AFTER LOOP BREAK (WHAT TO DO?)
        if(process.waitFor() != 0) {
            System.out.println("MOPOU");
        }
        
        return matchList;
    }
}