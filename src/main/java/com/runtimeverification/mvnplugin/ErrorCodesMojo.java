package com.runtimeverification.mvnplugin;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.io.filefilter.*;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Mojo(name = "sayhi")
public class ErrorCodesMojo extends AbstractMojo {

    /**
     * The array contatining the location of the K Files.
     */
    @Parameter(defaultValue = "${project.basedir}" + "../")
    private File semanticsDir;


    @Parameter(defaultValue = "")
    private String ignore;

    public void execute() throws MojoExecutionException, MojoFailureException {
        System.out.println(ignore);
        Set<String> ignoreCodes = new ArrayList<>(Arrays.asList(ignore.split(","))).stream()
                .map(String::trim).collect(Collectors.toSet());
        System.out.println(ignoreCodes);
//        Set<String> codesFromKFiles = getCodesFromKFiles(semanticsDir);
//        Set<String> codesFromCSV = getCodesFromCSV(semanticsDir);
//        Set<String> codesFromExamples = getCodesFromFiles(semanticsDir);
////        System.out.println(codesFromKFiles);
////        System.out.println(codesFromCSV);
//        codesFromKFiles.removeAll(codesFromCSV);
//        if (codesFromKFiles.size() > 0) {
//            StringBuffer message = new StringBuffer("\n");
//            codesFromKFiles.iterator().forEachRemaining(x -> message.append(x + " does not have a CSV entry \n"));
//            throw new MojoFailureException(message.toString());
//        }
////        codesFromCSV.removeAll(codesFromExamples);
////       if (codesFromCSV.size() > 0) {
////            StringBuffer message = new StringBuffer("\n");
////            codesFromCSV.iterator().forEachRemaining(x -> message.append(x + " does not have corresponding examples\n"));
////            throw new MojoFailureException(message.toString());
////        }
        System.out.println(getCodesFromCSV(semanticsDir).size());

    }

    private Set<String> getCodesFromKFiles(File baseDir) throws MojoExecutionException {
        Collection<File> kFiles = FileUtils.listFiles(baseDir, new String[]{"k"}, true);
        Pattern pattern = Pattern.compile("([A-Z]{2,}[0-9]+)\"");
        return getErrorCodesFromFiles(kFiles, pattern);
    }

    private Map<String, String> getCodesFromCSV(File baseDir) throws MojoExecutionException {
        Collection<File> csvFiles = FileUtils.listFiles(baseDir,
                FileFilterUtils.nameFileFilter("Error_Codes.csv"), TrueFileFilter.INSTANCE);

        return getCSVMap(csvFiles);
    }

    private Set<String> getCodesFromFiles(File baseDir) {
        Collection<File> exampleFiles = FileUtils.listFiles(baseDir,
                new RegexFileFilter("(UB|CV|USP|IMPL|L)\\-([A-Z]{2,}[0-9]+)\\-bad.*\\.c"), TrueFileFilter.INSTANCE);
        return exampleFiles.stream().map(x -> {
            String s = x.toString();
            return s.substring(s.lastIndexOf("/") + 1).split("\\-bad.*\\.c")[0].split("\\-")[1];
        }).collect(Collectors.toSet());
    }

    private Set<String> getErrorCodesFromFiles(Collection<File> files, Pattern pattern) throws MojoExecutionException {
        Set<String> codesSet = new HashSet<>();
        for (File f : files) {
            try {
                Matcher matcher = pattern.matcher(FileUtils.readFileToString(f));
                while (matcher.find()) {
                    codesSet.add(matcher.group(1));
                }
            } catch (IOException e) {
                throw new MojoExecutionException(e.getMessage());
            }
        }
        return codesSet;
    }

    private Map<String, String> getCSVMap(Collection<File> files) throws MojoExecutionException{
        Map<String, String> csvMap = new HashMap<>();
        for (File f : files) {
            try {
                LineIterator lineIterator = FileUtils.lineIterator(f);
                while (lineIterator.hasNext()) {
                    String line = lineIterator.nextLine();
                    System.out.println(line);
                    if (Pattern.matches("(UB|L|IMPL|CV|USP)\\-[A-Z]{2,}[0-9]+.*", line)) {
                        String[] splitArray = line.split(",");
                        csvMap.put(splitArray[0].split("-")[1], splitArray[1]);
                    }
                }
            } catch (IOException e) {
                throw new MojoExecutionException(e.getMessage());
            }
        }
        return csvMap;
    }

}
