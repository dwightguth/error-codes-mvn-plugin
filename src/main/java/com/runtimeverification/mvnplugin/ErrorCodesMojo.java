package com.runtimeverification.mvnplugin;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Mojo(name = "checkCodes")
public class ErrorCodesMojo extends AbstractMojo {

    /**
     * The array contatining the location of the K Files.
     */
    @Parameter(defaultValue = "${project.basedir}" + "../")
    private File semanticsDir;

    @Parameter(defaultValue = "${project.basedir}" + "../")
    private File dataDir;

    @Parameter(defaultValue = "")
    private String ignore;

    @Parameter(defaultValue = "")
    private String exclude;

    public void execute() throws MojoExecutionException, MojoFailureException {
        Set<String> ignoreCodes;
        if (ignore != null) {
            ignoreCodes = new ArrayList<>(Arrays.asList(ignore.split(","))).stream()
                    .map(String::trim).collect(Collectors.toSet());
        } else {
            ignoreCodes = new HashSet<>();
        }
        Map<String, String> csvMap = getCodesFromCSV(dataDir);
        Set<String> codesFromKFiles = getCodesFromKFiles(semanticsDir);
        Set<String> codesFromExamples = getCodesFromFiles(dataDir);
        Set<String> codesFromCSV = new HashSet<>(csvMap.keySet());
        codesFromKFiles.removeAll(codesFromCSV);
        StringBuffer message = new StringBuffer("\n");
        if (codesFromKFiles.size() > 0) {
            codesFromKFiles.iterator().forEachRemaining(x -> message.append(x + " does not have a CSV entry \n"));
            throw new MojoFailureException(message.toString());
        }
        codesFromCSV.removeAll(codesFromExamples);
        codesFromCSV.removeAll(ignoreCodes);
        if (codesFromCSV.size() > 0) {
            message.setLength(0);
            message.append("\n");
            codesFromCSV.iterator().forEachRemaining(x -> message.append(x + " does not have corresponding examples\n"));
            throw new MojoFailureException(message.toString());
        }
        message.setLength(0);
        message.append("\n");
        csvMap.entrySet().forEach(entry -> {
            if (!entry.getValue().matches("[A-Z \'].*")) {
                message.append(entry.getKey() + "'s Description - \"" +
                        entry.getValue() + "\" does not begin with an UpperCase Character.\n");
            }
        });
        if (message.length() > 1) {
            throw new MojoFailureException(message.toString());
        }
    }

    private Set<String> getCodesFromKFiles(File baseDir) throws MojoExecutionException {
        Collection<File> kFiles = FileUtils.listFiles(baseDir, new RegexFileFilter(".*\\.k"), FileFilterUtils.notFileFilter(FileFilterUtils.nameFileFilter(exclude)));
        Pattern pattern = Pattern.compile("\"([A-Z]{2,}[0-9]+)\"");
        return getErrorCodesFromFiles(kFiles, pattern);
    }

    private Map<String, String> getCodesFromCSV(File baseDir) throws MojoExecutionException, MojoFailureException {
        Collection<File> csvFiles = FileUtils.listFiles(baseDir,
                FileFilterUtils.nameFileFilter("Error_Codes.csv"), TrueFileFilter.INSTANCE);

        return getCSVMap(csvFiles);
    }

    private static final String PREFIX_PATTERN = "([A-Z]+)";

    private Set<String> getCodesFromFiles(File baseDir) {
        Collection<File> exampleFiles = FileUtils.listFiles(baseDir,
                new RegexFileFilter(PREFIX_PATTERN + "\\-([A-Z]{2,}[0-9]+)\\-bad.*\\.c"), TrueFileFilter.INSTANCE);
        return exampleFiles.stream().map(x -> {
            String s = x.toString();
            return s.substring(s.lastIndexOf(File.separator) + 1).split("\\-bad.*\\.c")[0].split("\\-")[1];
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

    private Map<String, String> getCSVMap(Collection<File> files) throws MojoExecutionException, MojoFailureException {
        Map<String, String> csvMap = new HashMap<>();
        Pattern pattern = Pattern.compile(PREFIX_PATTERN + "\\-[A-Z]{2,}[0-9]+");

        for (File f : files) {
            try {
                CSVParser parser = CSVParser.parse(f, Charset.defaultCharset(), CSVFormat.DEFAULT);
                for (CSVRecord record : parser.getRecords()) {
                    for (String value : record) {
                        if (value.startsWith("\"") || value.endsWith("\"")) {
                            throw new MojoExecutionException("Line " + record.getRecordNumber() + " of " +
                                    f.getAbsolutePath() + " contains an unmatched Quotation Mark (\")");
                        }
                    }
                    String errorCode = record.get(0);
                    if (pattern.matcher(errorCode).matches()) {
                        csvMap.put(errorCode.split("-")[1], record.get(1));
                    }
                }
            } catch (IOException e) {
                throw new MojoExecutionException(e.getMessage());
            }
        }
        return csvMap;
    }

}
