package lexerJava;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class Main {

    private static void writeToFile(String content, String fileName) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
            writer.write(content);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {

        Path path = Paths.get("file.cs");
        File file = path.toFile();
        try
        {
            Lexer lexer = new Lexer(file);
            System.out.println(lexer.toString());
            writeToFile(lexer.toString(), "lexemes2.txt");
        }
        catch (FileNotFoundException e)
        {
            System.out.println("File not found!");
        }
    }
}