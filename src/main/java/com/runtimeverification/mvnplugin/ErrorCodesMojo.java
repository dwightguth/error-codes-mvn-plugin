package com.runtimeverification.mvnplugin;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;

/**
 * The array contatining the location of the K Files.
 */


@Mojo( name = "sayhi")
public class ErrorCodesMojo extends AbstractMojo
{

    @Parameter
    private File[] directories;


    public void execute() throws MojoExecutionException
    {
        System.out.println(directories[0].toString());
    }
}
