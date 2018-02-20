package matchtool;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author eduardo
 */
public class Main {
    public static void main(String[] args) {
        final HashMap<String, ArrayList<String>> params = new HashMap<>();

        ArrayList<String> options = null;
        for(int i = 0; i < args.length; i++) {
            final String a = args[i];
            
            if(a.charAt(0) == '-') {
                if(a.length() < 2) {
                    System.err.println("Error at argument " + a);
                    return;
                }

                options = new ArrayList<>();
                params.put(a.substring(1), options);
            }
            else if(options != null) {
                options.add(a);
            }
            else {
                System.err.println("Illegal parameter usage");
                return;
            }
        }

        //-i TEMPLATEPATH

        String templatePath = "./template";

        try {
            options = params.get("i");
            if(options != null) {
                templatePath = options.get(0);
            }
            else {
//                throw new Exception("Especifique -i TEMPLATEPATH");
            }
        }
        catch(Exception ex) {
            System.err.println(ex.getMessage());
            return;
        }
        
        MatchTool.run(templatePath);
    }
}