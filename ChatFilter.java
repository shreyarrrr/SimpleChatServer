/**
 * Chat Filter
 *
 * @author Shreya Roy
 * @version 11/27/18
 */
import java.io.*;

public class ChatFilter {
    String badWordsFileName;
    public ChatFilter(String badWordsFileName) {
        this.badWordsFileName = badWordsFileName;
    }

    public String filter(String msg) {
        try {
            File file = new File("/Users/shreyaroy/IdeaProjects/CS180Proj/SimpleChatServer/src/" + badWordsFileName);
            BufferedReader br = new BufferedReader(new FileReader(file));
            String word;
            String message = msg.toLowerCase();
            while (true) {
                word = br.readLine();
                if (word == null)
                    break;
                word = word.toLowerCase();
                String replace = "";
                for (int i = 0; i < word.length(); i++) {
                    replace += "*";
                }
                message = message.replaceAll(word, replace);
                String [] censored = message.split(" ");
                String [] uncensored = msg.split(" ");
                String end = "";
                for (int i = 0; i < censored.length; i++) {
                    if (censored[i].contains("*"))
                        end += censored[i];
                    else
                        end += uncensored[i];
                    if (i != censored.length - 1)
                        end += " ";
                }
                msg = end;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return msg;
    }
}
