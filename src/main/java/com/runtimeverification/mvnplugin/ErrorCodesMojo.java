package com.runtimeverification.mvnplugin;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;


@Mojo( name = "sayhi")
public class ErrorCodesMojo extends AbstractMojo
{

    /**
     * The array contatining the location of the K Files.
     */
    @Parameter(defaultValue = "${project.basedir}" + "../c-semantics")
    private File semanticsDir;


    public void execute() throws MojoExecutionException
    {
        getCodesFromKFiles(semanticsDir);
    }

    private Collection<File> getCodesFromKFiles(File baseDir) {
        Set<String> codesSet = new HashSet<>();
        Collection<File> kFiles = FileUtils.listFiles(baseDir, new String[]{"k"}, true);
        Pattern pattern = Pattern.compile("[A-Z]+[0-9]*");
        kFiles.forEach(f -> {

        });
    }
}
