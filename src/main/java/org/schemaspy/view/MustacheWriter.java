package org.schemaspy.view;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import org.apache.commons.io.FileUtils;
import org.schemaspy.Config;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

/**
 * Created by rkasa on 2016-03-22.
 */
public class MustacheWriter {
    private File outputDir;
    private HashMap<String, Object> scopes;
    private String rootPath;
    private String rootPathtoHome;
    private String databaseName;
    private boolean isMultipleSchemas;
    private String templateDirectory = Config.getInstance().getTemplateDirectory();
    private static ResourceFinder resourceFinder = new ResourceFinder();

    public MustacheWriter(File outputDir, HashMap<String, Object> scopes, String rootPath, String databaseName, boolean isMultipleSchemas) {
        this.outputDir = outputDir;
        this.scopes = scopes;
        this.rootPath = rootPath;
        this.isMultipleSchemas = isMultipleSchemas;
        boolean isOneOfMultipleSchemas = Config.getInstance().isOneOfMultipleSchemas();

        if(isOneOfMultipleSchemas){
            this.rootPathtoHome = "../"+rootPath;
        }else{
            this.rootPathtoHome = rootPath;
        }
        this.databaseName = databaseName;
    }

    public void write(String templatePath, String destination, String scriptFileName) {
        MustacheFactory mf = new DefaultMustacheFactory();
        MustacheFactory contentMf = new DefaultMustacheFactory();
        StringWriter content = new StringWriter();
        StringWriter result = new StringWriter();

        HashMap<String, Object> mainScope = new HashMap<>();

        try {
            Mustache mustache = mf.compile(getReader(templatePath),"template");
            mustache.execute(result, scopes).flush();

            mainScope.put("databaseName", databaseName);
            mainScope.put("content", result);
            mainScope.put("pageScript",scriptFileName);
            mainScope.put("rootPath", rootPath);
            mainScope.put("rootPathtoHome", rootPathtoHome);
            mainScope.put("isMultipleSchemas", isMultipleSchemas);
            mainScope.putAll(scopes);

            Mustache mustacheContent = contentMf.compile(getReader("container.html"), "container");
            mustacheContent.execute(content, mainScope).flush();

            File destinationFile = new File(outputDir, destination);

            FileUtils.writeStringToFile(destinationFile, content.toString(), "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Reader getReader(String fileName) throws IOException {
        String parent = templateDirectory;
        return getReader(parent, fileName);
    }

    /**
     * Returns a {@link Reader} that can be used to read the contents
     * of the specified file in the parent directory.<p>
     * Search order is
     * <ol>
     * <li><code>fileName</code> as an explicitly-defined file in the parent directory</li>
     * <li><code>fileName</code> as a file in the user's working directory</li>
     * <li><code>fileName</code> as a file in the user's home directory</li>
     * <li><code>fileName</code> as a resource from the class path</li>
     * </ol>
     * @param parent
     * @param fileName
     *
     * @return
     * @throws IOException
     */
    public static Reader getReader(String parent, String fileName) throws IOException {
        InputStream inputStream = resourceFinder.find(parent, fileName);
        return new InputStreamReader(inputStream, StandardCharsets.UTF_8);
    }

}
