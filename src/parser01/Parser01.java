/*
 * Имеется список HTTP ссылок на файлы произвольного типа, список состоит из 500 000 записей. 
 * Необходимо реализовать наиболее эффективную загрузку файлов из этого списка в папку downloads, 
 * с последующим занесением следующей информации в БД MySQL:
 * - имя файла
 * - удаленный путь
 * - размер файла
 * - дата начала загрузки
 * - дата окончания загрузки
 */
package parser01;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Anri Started: 2019-06-08 13:28
 */
public class Parser01 {

    static String inPath = "c:\\temp\\parser\\list.txt";
    static String outPath = "c:\\temp\\parser\\downloads\\";

    // JDBC URL, username and password of MySQL server
    private static final String CONNECTION = "jdbc:mysql://localhost:3306/test"
            + "?useUnicode=true&serverTimezone=UTC&useSSL=true&verifyServerCertificate=false";
    private static final String USER = "test";
    private static final String PASSWORD = "1234";

    // JDBC variables for opening and managing connection
    private static Connection con;
    private static Statement stmt;
    private static ResultSet rs;
    
    private static int id = 0;
    private static SimpleDateFormat sdf;

    /**
     * @param args the command line arguments: first argument - PATH to file
     * with list of HTTP links, second argument - PATH to output folder
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("first argument - PATH to file with list of HTTP links");
            System.out.println("second argument - PATH to output folder");
        } else {
            inPath = args[0];
            outPath = args[1];
        }
        System.out.println("inPath = " + inPath);
        System.out.println("outPath = " + outPath);
        if (sqlConnect()) {
            clearLogTable();
            sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            System.out.println("The download of files has started. Please wait.");
            loadAll();
            sqlDisonnect();
        }
        System.out.println("Loaded " + id + " file(s)");
    }

    private static void loadAll() {
        try (Stream<String> lines = Files.lines(Paths.get(inPath))) {
            lines.forEach(x -> load(x));
        } catch (Exception e) {
            System.out.println("File not found: " + inPath);
        }
    }

    private static void load(String url) {
        try {
            loadOne(url);
        } catch (IOException e) {
            System.out.println("Error loading file " + url);
        }
    }

    private static void loadOne(String urlStr)
            throws IOException {
        long dt_begin = System.currentTimeMillis();
        URL url = new URL(urlStr);
        String path = url.getFile();
        String fileNameOnly = path.substring(path.lastIndexOf('/') + 1, path.length());
        String fileName = outPath + fileNameOnly;
        try (ReadableByteChannel rbc = Channels.newChannel(url.openStream());
                FileOutputStream fos = new FileOutputStream(fileName)) {
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        }
        long size = new File(fileName).length();
        addLogTable(fileNameOnly, urlStr, size, dt_begin);
    }

    private static boolean sqlConnect() {
        boolean connect = false;
        try {
            con = DriverManager.getConnection(CONNECTION, USER, PASSWORD);
            connect = true;
        } catch (SQLException sqlEx) {
            System.out.println("Error connecting to MySql");
        }
        return connect;
    }

    private static void sqlDisonnect() {
        try {
            con.close();
        } catch (SQLException e) {
//            e.printStackTrace();
        }
    }

    private static void clearLogTable() {
        String query = "delete from log";
        try {
            stmt = con.createStatement();
            stmt.executeUpdate(query);
        } catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
        } finally {
            try {
                stmt.close();
            } catch (SQLException e) {
//            e.printStackTrace();
            }
        }
    }

    private static void addLogTable(String name, String path, long size, 
            long dt_begin) {
        /*
            CREATE TABLE `log` (
              `id` int(10) NOT NULL,
              `name` varchar(254) NOT NULL,
              `path` varchar(50) NOT NULL,
              `size` int(10) NOT NULL,
              `dt_begin` TIMESTAMP NOT NULL,
              `dt_end` TIMESTAMP NOT NULL,
              PRIMARY KEY (`id`)
            );
        */   
        Date resultdate = new Date(dt_begin);
        String date_begin = sdf.format(resultdate);

        String query = "insert into log " 
                + "(id, name, path, size, dt_begin, dt_end) "
                + "values \n(" + id
                + ", '" + name + "'"
                + ", '" + path + "'"
                + ", " + size + ""
                + ", '" + date_begin + "'"
                + ", " + "NOW()" + ")";
        try {
            stmt = con.createStatement();
            stmt.executeUpdate(query);
            id++;
        } catch (SQLException e) {
//            e.printStackTrace();
        } finally {
            try {
                stmt.close();
            } catch (SQLException e) {
//            e.printStackTrace();
            }
        }
    }
}
