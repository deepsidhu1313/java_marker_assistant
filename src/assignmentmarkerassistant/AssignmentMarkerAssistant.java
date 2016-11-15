/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package assignmentmarkerassistant;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author nika
 */
public class AssignmentMarkerAssistant {

    ExecutorService executorService = Executors.newFixedThreadPool(2);//Runtime.getRuntime().availableProcessors()

    public AssignmentMarkerAssistant() {
        createCompilerScript();
        System.out.println("Please give path of folder to mark:");
        Scanner keyboard;
        keyboard = new Scanner(System.in);

        String folderpath = keyboard.nextLine().trim();
        File directory = new File(folderpath);
        if (!directory.exists()) {
            System.err.println("Folder Doesnot exist :" + folderpath);
            System.exit(1);
        }
        if (directory.isDirectory()) {
            File[] files = directory.listFiles((File dir, String name) -> name.toLowerCase().endsWith(".java"));

            for (File file : files) {
                System.out.println("File is " + file.getAbsolutePath());
                String actualname = file.getName().substring(file.getName().lastIndexOf("-") + 1).trim();
                String folder = file.getName().substring(file.getName().lastIndexOf("-") + 1, file.getName().lastIndexOf(".java")).trim();
                createDirectory(folderpath + "/" + folder);
                renameFile(file, new File(folderpath + "/" + folder + "/" + actualname));
                compileJavaFiles(folderpath + "/" + folder + "/");
                try {
                    copyFolder(new File("i.txt"), new File(folderpath + "/" + folder + "/i.txt"));
                    copyFolder(new File("output"), new File(folderpath + "/" + folder + "/output"));
                } catch (IOException ex) {
                    Logger.getLogger(AssignmentMarkerAssistant.class.getName()).log(Level.SEVERE, null, ex);
                }
                printInputFile(folderpath + "/" + folder + "/");
                //   runJavaFiles(folderpath + "/" + folder + "/", folder);
            }

            for (File file : files) {
                // String actualname = file.getName().substring(file.getName().lastIndexOf("-") + 1).trim();
                String folder = file.getName().substring(file.getName().lastIndexOf("-") + 1, file.getName().lastIndexOf(".java")).trim();
                runJavaFiles(folderpath + "/" + folder + "/", folder);
            }

            for (File file : files) {
                // String actualname = file.getName().substring(file.getName().lastIndexOf("-") + 1).trim();
                String folder = file.getName().substring(file.getName().lastIndexOf("-") + 1, file.getName().lastIndexOf(".java")).trim();
                calculateDiffFromOrigOutput(folderpath + "/" + folder + "/");
            }
            
        }

        executorService.shutdown();
       
    }

    /***
     * This method will create parent directories if they don't exist,
     * otherwise will create the given directory 
     * 
     * @param name : Name or path of the directory/directories to create
     * @return true if directories are created successfully
     */
    public boolean createDirectory(String name) {
        return new File(name).mkdirs();
    }

    /***
     * Renames a file
     * @param filename : Name or Path of the file to be renamed
     * @param newFilename : Name or path of the new file
     * @return true if renaming successful. 
     */
    public boolean renameFile(String filename, String newFilename) {
        return new File(filename).renameTo(new File(newFilename));
    }

    
    /***
     * Renames a file
     * @param filename : Name or Path of the file to be renamed
     * @param newFilename : Name or path of the new file
     * @return true if renaming successful. 
     */
    public boolean renameFile(File filename, File newFilename) {
        return (filename).renameTo((newFilename));
    }

    /***
     * Prints the name of the input file such as abc.txt to the file <i><b>inputfilename</b></i>
     * @param folder : Path of the directory where file <i><b>inputfilename</b></i> should be saved
     */
    void printInputFile(String folder) {
        try {
            PrintWriter fileWriter = new PrintWriter(folder + "inputfilename", "UTF-8");
            fileWriter.append("i.txt");
            fileWriter.close();

        } catch (FileNotFoundException ex) {
            Logger.getLogger(AssignmentMarkerAssistant.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(AssignmentMarkerAssistant.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /***
     * Create Scripts used to Compile and Run JAVA files,
     * Also creates a script to generate difference of output file from original output file
     */
    
    void createCompilerScript() {
        try {
            PrintWriter scriptWriter = new PrintWriter("javac.sh", "UTF-8");
            scriptWriter.append("#!/bin/bash \n"
                    + "\n"
                    + "cd \"${1}/\"\n"
                    + "javac *.java");
            scriptWriter.close();

        } catch (FileNotFoundException ex) {
            Logger.getLogger(AssignmentMarkerAssistant.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(AssignmentMarkerAssistant.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            PrintWriter scriptWriter = new PrintWriter("java.sh", "UTF-8");
            scriptWriter.append("#!/bin/bash \n"
                    + "\n"
                    + "cd \"${1}/\"\n"
                    + "java ${2} < inputfilename");
            scriptWriter.close();

        } catch (FileNotFoundException ex) {
            Logger.getLogger(AssignmentMarkerAssistant.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(AssignmentMarkerAssistant.class.getName()).log(Level.SEVERE, null, ex);
        }

                try {
            PrintWriter scriptWriter = new PrintWriter("diff.sh", "UTF-8");
            scriptWriter.append("#!/bin/bash \n"
                    + "\n"
                    + "cd \"${1}\"\n"
                    + "diff -u -B -b output run-out.log");
            scriptWriter.close();

        } catch (FileNotFoundException ex) {
            Logger.getLogger(AssignmentMarkerAssistant.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(AssignmentMarkerAssistant.class.getName()).log(Level.SEVERE, null, ex);
        }

        
    }
/**
 * Compiles all the java file in a directory
 * @param foldername : Path or name of the folder where source code reside/**
 * Compiles all the java file in a directory
 * @param foldername : Path or name of the folder where source code reside
 */
    
    public void compileJavaFiles(String foldername) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ProcessBuilder pb = new ProcessBuilder("/bin/bash", "javac.sh", foldername);
                    Process p = pb.start();
                    BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

                    PrintWriter outputWriter = new PrintWriter(foldername + "compiler-out.log", "UTF-8");
                    PrintWriter errorWriter = new PrintWriter(foldername + "compiler-error.log", "UTF-8");

                    String s = null;
                    while ((s = stdInput.readLine()) != null) {
                       // System.out.println(s);
                        outputWriter.println(s+"");
                    }
                    while ((s = stdError.readLine()) != null) {
                        //System.out.println(s);
                        errorWriter.println(s+"");
                    }

                    stdError.close();
                    stdInput.close();
                    outputWriter.close();
                    errorWriter.close();
                    p.destroy();
                    System.out.println("Compiled Files in " + foldername);
                } catch (IOException ex) {
                    Logger.getLogger(AssignmentMarkerAssistant.class.getName()).log(Level.SEVERE, null, ex);
                    System.err.println(ex);
                }

            }
        });
        executorService.submit(t);
    }

    /***
     * Generate difference of new output from original output
     * @param foldername : Path or name of the directory where output files reside
     */
        public void calculateDiffFromOrigOutput(String foldername) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ProcessBuilder pb = new ProcessBuilder("/bin/bash", "diff.sh", foldername);
                    Process p = pb.start();
                    BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

                    PrintWriter outputWriter = new PrintWriter(foldername + "diff-out.log", "UTF-8");
                    PrintWriter errorWriter = new PrintWriter(foldername + "diff-error.log", "UTF-8");

                    String s = null;
                    while ((s = stdInput.readLine()) != null) {
                       // System.out.println(s);
                        outputWriter.println(s+"");
                    }
                    while ((s = stdError.readLine()) != null) {
                        //System.out.println(s);
                        errorWriter.println(s+"");
                    }

                    stdError.close();
                    stdInput.close();
                    outputWriter.close();
                    errorWriter.close();
                    p.destroy();
                    System.out.println("Diff generated Files in " + foldername);
                } catch (IOException ex) {
                    Logger.getLogger(AssignmentMarkerAssistant.class.getName()).log(Level.SEVERE, null, ex);
                    System.err.println(ex);
                }

            }
        });
        executorService.submit(t);
    }

        /***
         * Run java files under the directory
         * @param foldername : path to the directory where all the compiled classes reside
         * @param main : name of the main class
         */
    
    public void runJavaFiles(String foldername, String main) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ProcessBuilder pb = new ProcessBuilder("/bin/bash", "java.sh", foldername, main);
                    Process p = pb.start();
                    BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

                    PrintWriter outputWriter = new PrintWriter(foldername + "run-out.log", "UTF-8");
                    PrintWriter errorWriter = new PrintWriter(foldername + "run-error.log", "UTF-8");

                    String s = null;
                    while ((s = stdInput.readLine()) != null) {
                      //  System.out.println(s);
                        outputWriter.println(s+"");
                    }
                    while ((s = stdError.readLine()) != null) {
                        //System.out.println(s);
                        errorWriter.println(s+"");
                    }

                    stdError.close();
                    stdInput.close();
                    outputWriter.close();
                    errorWriter.close();
                    //calculateDiffFromOrigOutput(foldername);
                    p.destroy();
                    System.out.println("Ran Files in " + foldername);
                    //generateDiffbetFiles("output", foldername + "run-out.log", foldername);
                 
                } catch (IOException ex) {
                    Logger.getLogger(AssignmentMarkerAssistant.class.getName()).log(Level.SEVERE, null, ex);
                    System.err.println(ex);
                }

            }
        });
        executorService.submit(t);
    }

    /***
     * Copy/ folder/file from source to destination
     * @param src : Path to source folder/file
     * @param dest : Path to destination folder/file
     * @throws IOException 
     */
    
    public void copyFolder(File src, File dest) throws IOException {

        if (src.isDirectory()) {

            //if directory not exists, create it
            if (!dest.exists()) {
                dest.mkdir();
                System.out.println("Directory copied from "
                        + src + "  to " + dest);
            }

            //list all the directory contents
            String files[] = src.list();

            for (String file : files) {
                //construct the src and dest file structure
                File srcFile = new File(src, file);
                File destFile = new File(dest, file);
                //recursive copy
                copyFolder(srcFile, destFile);
            }

        } else {
            //if file, then copy it
            //Use bytes stream to support all file types
            InputStream in = new FileInputStream(src);
            OutputStream out = new FileOutputStream(dest);

            byte[] buffer = new byte[1024];

            int length;
            //copy the file content in bytes 
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }

            in.close();
            out.close();
            System.out.println("File copied from " + src + " to " + dest);
        }
    }


    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
       AssignmentMarkerAssistant a2marker= new AssignmentMarkerAssistant();
    }

}
