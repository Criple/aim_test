package com.company;

import java.io.*;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;

class parseFiles implements Runnable
{

    public final ArrayList<String> resLinesList;
    public String filePath;

    public parseFiles(ArrayList<String> resLinesListParam, String filePathParam){
        this.resLinesList = resLinesListParam;
        this.filePath = filePathParam;
    }

    public void run()
    {
        synchronized(this.resLinesList) {
            ArrayList<String> fileLinesList = new ArrayList<>();
            try {
                File file = new File(this.filePath);
                FileReader fr = new FileReader(file);
                BufferedReader reader = new BufferedReader(fr);
                String line;
                line = reader.readLine();
                while (line != null) {
                    fileLinesList.add(line);
                    line = reader.readLine();
                }
                if(fileLinesList.size() > 0){
                    String firstLine = fileLinesList.get(0);
                    int c = 0;
                    int labelIndex = 0;
                    for(String splitPart : firstLine.split(";")) {
                        if (!this.resLinesList.contains(splitPart + ":")) {
                            this.resLinesList.add(splitPart + ":");
                            labelIndex = this.resLinesList.size();
                        }else{
                            labelIndex = this.resLinesList.indexOf(splitPart + ":") + 1;
                        }
                        for (int j = 1; j <= fileLinesList.size() - 1; j++) {
                            if (labelIndex >= this.resLinesList.size()) {
                                this.resLinesList.add(fileLinesList.get(j).split(";")[c] + ";");
                            } else {
                                String splitPartLine = fileLinesList.get(j).split(";")[c];
                                if (!Arrays.asList(this.resLinesList.get(labelIndex).split(";")).contains(splitPartLine)) {
                                    this.resLinesList.set(labelIndex, this.resLinesList.get(labelIndex) + splitPartLine + ";");
                                }
                            }
                        }
                        c++;
                    }
                }
                fileLinesList.clear();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}

public class Main {

    public static void main(String[] args) {

        ArrayList<String> resLinesList = new ArrayList<>();
        parseFiles parseThread;
        ArrayList<Thread> massThread = new ArrayList<>();

        String mainPath = applicationPath(Main.class);

        for (int i = 1; i <= findFiles(mainPath, ".csv"); i++){
            parseThread = new parseFiles(resLinesList, mainPath + "input" + i + ".csv");
            Thread Thread = new Thread(parseThread);
            massThread.add(Thread);
        }

        for (Thread subThread : massThread){
            subThread.start();
        }

        for (int t = 0; t < massThread.size(); t++){
            try{
                massThread.get(t).join();
                massThread.remove(t);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        File resDir = new File("result");
        if (!resDir.exists()){
            resDir.mkdir();
        }
        File file = new File("result/result.csv");
        try{
            file.createNewFile();
        }catch (IOException e){
            e.printStackTrace();
        }
        try(FileWriter writer = new FileWriter(file, false))
        {
            writer.write("");
            for(String resLine : resLinesList){
                writer.append(resLine);
                writer.append('\n');
            }
            writer.flush();
        }
        catch(IOException ex){
            System.out.println(ex.getMessage());
        }
        System.out.println("Готово!");
    }


    public static String applicationPath(Class mainStartupClassName) {
        try {
            String path = mainStartupClassName.getProtectionDomain().getCodeSource().getLocation().getPath();
            File f = new File(path);

            String pathDecoded = URLDecoder.decode(path, "UTF-8");
            pathDecoded = pathDecoded.trim().replace("/", File.separator);
            if (pathDecoded.startsWith(File.separator)) {
                pathDecoded = pathDecoded.substring(1);
            }
            if (pathDecoded.endsWith(".jar")){
                pathDecoded = pathDecoded.split(Pattern.quote(f.getName()))[0];
            }
            return pathDecoded;
        }
        catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static int findFiles(String path, String ext) {
        File dir = new File(path);

        int i = 0;
        if(!dir.exists()) return i;
        File[] listFiles = dir.listFiles(new FileFilter(ext));
        if(listFiles.length != 0){
            for(File f : listFiles)
                i++;
        }
        return i;
    }

    public static class FileFilter implements FilenameFilter{

        private String ext;

        public FileFilter(String ext){
            this.ext = ext.toLowerCase();
        }
        @Override
        public boolean accept(File dir, String name) {
            return name.toLowerCase().endsWith(this.ext);
        }
    }


}
