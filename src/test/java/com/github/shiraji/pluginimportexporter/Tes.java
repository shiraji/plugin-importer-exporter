package com.github.shiraji.pluginimportexporter;

import com.google.common.io.Resources;
import org.apache.http.client.fluent.Request;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jsfr.json.Collector;
import org.jsfr.json.GsonParser;
import org.jsfr.json.JsonSurfer;
import org.jsfr.json.JsonSurferGson;
import org.jsfr.json.ValueBox;
import org.jsfr.json.compiler.JsonPathCompiler;
import org.jsfr.json.path.JsonPath;
import org.jsfr.json.provider.GsonProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Tes {
    public static void main(String[] args) throws IOException {
        JsonSurfer surfer = JsonSurferGson.INSTANCE;
        final URL resource = Resources.getResource("t.json");

        final Collector collector = surfer.collector(new FileInputStream(new File(resource.getFile())));
        String pluginName = "2020.2.2";
        JsonPath compiledPath = JsonPathCompiler.compile("$[?(@.version=='" + pluginName + "')].file");
        final ValueBox<String> id = collector.collectOne(compiledPath,
                String.class);
        collector.exec();
        System.out.println(id.get());

        //CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        final File file = Paths.get(id.get()).getFileName().toFile();
        System.out.println(file.getAbsolutePath());
        Request.Get("https://plugins.jetbrains.com/files/"+id.get()).execute().saveContent(file);

        System.out.println("GenerateSerialVersionUID");
    }
}
