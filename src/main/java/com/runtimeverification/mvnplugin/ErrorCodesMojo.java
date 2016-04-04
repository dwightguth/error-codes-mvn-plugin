package com.runtimeverification.mvnplugin;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Mojo( name = "sayhi")
public class ErrorCodesMojo extends AbstractMojo
{

    /**
     * The array contatining the location of the K Files.
     */
    @Parameter(defaultValue = "${project.basedir}" + "../")
    private File semanticsDir;


    public void execute() throws MojoExecutionException
    {
        System.out.println(getCodesFromCSV(semanticsDir).size());

    }

    private Set<String> getCodesFromKFiles(File baseDir) {
        Collection<File> kFiles = FileUtils.listFiles(baseDir, new String[]{"k"}, true);
        return getErrorCodesFromFiles(kFiles);
    }

    private Set<String> getCodesFromCSV(File baseDir) {
        Collection<File> csvFiles = FileUtils.listFiles(baseDir, FileFilterUtils.nameFileFilter("Error_Codes.csv"), TrueFileFilter.INSTANCE);
        return getErrorCodesFromFiles(csvFiles);
    }

    private Set<String> getErrorCodesFromFiles(Collection<File> files) {
        Pattern pattern = Pattern.compile("[A-Z]{2,}[0-9]+");
        Set<String> codesSet = new HashSet<>();

        files.forEach(f -> {
            try {
                Matcher matcher = pattern.matcher(FileUtils.readFileToString(f));
                while(matcher.find()) {
                    codesSet.add(matcher.group());
                }
            } catch (IOException e) {
                System.out.println("OOPS!");
            }
        });
        return codesSet;
    }
}
