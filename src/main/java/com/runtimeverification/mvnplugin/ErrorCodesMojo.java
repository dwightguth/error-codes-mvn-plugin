package com.runtimeverification.mvnplugin;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.*;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Mojo( name = "sayhi")
public class ErrorCodesMojo extends AbstractMojo
{

    /**
     * The array contatining the location of the K Files.
     */
    @Parameter(defaultValue = "${project.basedir}" + "../")
    private File semanticsDir;


    public void execute() throws MojoExecutionException, MojoFailureException
    {
        Set<String> codesFromKFiles = getCodesFromKFiles(semanticsDir);
        Set<String> codesFromCSV = getCodesFromCSV(semanticsDir);
        codesFromKFiles.removeAll(codesFromCSV);
        if (codesFromKFiles.size() > 0) {
            StringBuffer message = new StringBuffer("");
            codesFromKFiles.iterator().forEachRemaining(x -> message.append(x + " does not have a CSV entry \n"));
            throw new MojoFailureException(message.toString());
        }

    }

    private Set<String> getCodesFromKFiles(File baseDir) throws MojoExecutionException{
        Collection<File> kFiles = FileUtils.listFiles(baseDir, new String[]{"k"}, true);
        return getErrorCodesFromFiles(kFiles);
    }

    private Set<String> getCodesFromCSV(File baseDir) throws MojoExecutionException{
        Collection<File> csvFiles = FileUtils.listFiles(baseDir,
                FileFilterUtils.nameFileFilter("Error_Codes.csv"), TrueFileFilter.INSTANCE);
        return getErrorCodesFromFiles(csvFiles);
    }

    private Set<String> getCodesFromFiles(File baseDir) {
        Collection<File> exampleFiles = FileUtils.listFiles(baseDir,
                new RegexFileFilter("(UB|CV|USP|IMPL|L)\\-([A-Z]{2,}[0-9]+)\\-bad.*\\.c"), TrueFileFilter.INSTANCE);
        return exampleFiles.stream().map(x -> {
            String s = x.toString();
            return s.substring(s.lastIndexOf("/") + 1).split("\\-bad.*\\.c")[0];
        }).collect(Collectors.toSet());
    }

    private Set<String> getErrorCodesFromFiles(Collection<File> files) throws MojoExecutionException{
        Pattern pattern = Pattern.compile("[A-Z]{2,}[0-9]+");
        Set<String> codesSet = new HashSet<>();

        for(File f : files) {
            try {
                Matcher matcher = pattern.matcher(FileUtils.readFileToString(f));
                while (matcher.find()) {
                    codesSet.add(matcher.group());
                }
            } catch (IOException e) {
                throw new MojoExecutionException(e.getMessage());
            }
        }
        return codesSet;
    }
}
