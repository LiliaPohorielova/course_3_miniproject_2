package edu.coursera.distributed;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class FileServer {

    public void run(final ServerSocket socket, final PCDPFilesystem fs)
            throws IOException {
        while (true) {
            Socket s = socket.accept();
            InputStream input = s.getInputStream();
            OutputStream output = s.getOutputStream();
            String fileName = getFileName(input);
            if (fileName == null) printNotFound(output);
            else printFileContent(fileName, fs, output);
            output.flush();
            output.close();
            s.close();
        }
    }

    private String getFileName(InputStream input) {
        Scanner scanner = new Scanner(input).useDelimiter("\\r\\n");
        String line = scanner.next();
        Pattern pattern = Pattern.compile("GET (.+) HTTP/\\d.\\d");
        Matcher matcher = pattern.matcher(line);
        if (!matcher.find()) {
            return null;
        }
        return matcher.group(1);
    }

    private void printNotFound(OutputStream output) {
        PrintStream printer = new PrintStream(output);
        printer.print("HTTP/1.0 404 Not Found\r\n");
        printer.print("Server: FileServer\r\n");
        printer.print("\r\n");
        printer.close();
    }

    private void printFileContent(String filename, PCDPFilesystem fs, OutputStream output) {
        String contents = fs.readFile(new PCDPPath(filename));
        if (null == contents) {
            printNotFound(output);
        } else {
            PrintStream printer = new PrintStream(output);
            printer.print("HTTP/1.0 200 OK\r\n");
            printer.print("Server: FileServer\r\n");
            printer.print("Content-Length: " + contents.length() + "\r\n");
            printer.print("\r\n");
            printer.print(contents);
            printer.print("\r\n");
            printer.flush();
            printer.close();
        }
    }
}
