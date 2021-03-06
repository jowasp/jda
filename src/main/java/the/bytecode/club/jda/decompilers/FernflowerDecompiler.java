package the.bytecode.club.jda.decompilers;

import org.apache.commons.io.FileUtils;
import org.jetbrains.java.decompiler.main.decompiler.BaseDecompiler;
import org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler;
import org.jetbrains.java.decompiler.main.decompiler.PrintStreamLogger;
import org.jetbrains.java.decompiler.main.extern.IResultSaver;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InnerClassNode;
import the.bytecode.club.jda.JDA;
import the.bytecode.club.jda.JarUtils;
import the.bytecode.club.jda.settings.DecompilerSettings;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.jar.Manifest;

/**
 * A FernFlower wrapper with all the options (except 2)
 *
 * @author Konloch
 * @author WaterWolf
 */

public final class FernflowerDecompiler extends Decompiler {
    public FernflowerDecompiler() {
        for (Settings setting : Settings.values()) {
            settings.registerSetting(setting);
        }
    }

    @Override
    public String getName() {
        return "Fernflower";
    }

    @Override
    public String decompileClassNode(String containerName, final ClassNode cn) {
        try {
            Map<String, Object> options = main(generateMainMethod());

            final AtomicReference<String> result = new AtomicReference<>();
            result.set(null);

            BaseDecompiler baseDecompiler = new BaseDecompiler((externalPath, internalPath) -> {
                ClassNode requestedCn = JDA.getClassNode(containerName, JDA.extractProxyClassName(externalPath));
                if (requestedCn == null) {
                    System.err.println("Couldn't load " + externalPath);
                    return new byte[0];
                }
                byte[] bytes = JDA.getClassBytes(containerName, requestedCn);
                byte[] clone = new byte[bytes.length];
                System.arraycopy(bytes, 0, clone, 0, bytes.length);
                return clone;
            }, new IResultSaver() {
                @Override
                public void saveFolder(String s) {

                }

                @Override
                public void copyFile(String s, String s1, String s2) {

                }

                @Override
                public void saveClassFile(String s, String s1, String s2, String decompilation, int[] ints) {
                    result.set(decompilation);
                }

                @Override
                public void createArchive(String s, String s1, Manifest manifest) {

                }

                @Override
                public void saveDirEntry(String s, String s1, String s2) {

                }

                @Override
                public void copyEntry(String s, String s1, String s2, String s3) {

                }

                @Override
                public void saveClassEntry(String s, String s1, String s2, String s3, String s4) {
                }

                @Override
                public void closeArchive(String s, String s1) {

                }
            }, options, new PrintStreamLogger(System.out));

            baseDecompiler.addSpace(JDA.getClassFileProxy(cn), true);
            for (InnerClassNode innerCn : cn.innerClasses)
                baseDecompiler.addSpace(JDA.getClassFileProxy(innerCn), true);
            baseDecompiler.decompileContext();
            while (true) {
                if (result.get() != null) {
                    break;
                }
            }
            return result.get();
        } catch (Exception e) {
            return parseException(e);
        }
    }

    @Override
    public void decompileToZip(String zipName) {
        try {
            Path outputDir = Files.createTempDirectory("fernflower_output");
            Path tempJar = Files.createTempFile("fernflower_input", ".jar");
            File output = new File(zipName);
            JarUtils.saveAsJar(JDA.getLoadedBytes(), tempJar.toAbsolutePath().toString());
            ConsoleDecompiler decompiler = new ConsoleDecompiler(outputDir.toFile(), main(generateMainMethod()));
            decompiler.addSpace(tempJar.toFile(), true);
            decompiler.decompileContext();
            Files.move(outputDir.toFile().listFiles()[0].toPath(), output.toPath());
            Files.delete(tempJar);
            FileUtils.deleteDirectory(outputDir.toFile());
        } catch (Exception e) {
            handleException(e);
        }
    }

    public Map<String, Object> main(String[] args) {
        HashMap mapOptions = new HashMap();
        boolean isOption = true;

        for (int destination = 0; destination < args.length - 1; ++destination) {
            String logger = args[destination];
            if (isOption && logger.length() > 5 && logger.charAt(0) == 45 && logger.charAt(4) == 61) {
                String decompiler = logger.substring(5);
                if ("true".equalsIgnoreCase(decompiler)) {
                    decompiler = "1";
                } else if ("false".equalsIgnoreCase(decompiler)) {
                    decompiler = "0";
                }

                mapOptions.put(logger.substring(1, 4), decompiler);
            } else {
                isOption = false;
            }
        }

        return mapOptions;
    }

    private String[] generateMainMethod() {
        String[] result = new String[getSettings().size()];
        int index = 0;
        for (Settings setting : Settings.values()) {
            result[index++] = String.format("-%s=%s", setting.getParam(),
                    getSettings().isSelected(setting) ? "1" : "0");
        }
        return result;
    }

    public enum Settings implements DecompilerSettings.SettingsEntry {
        HIDE_BRIDGE_METHODS("rbr", "Hide Bridge Methods", true),
        HIDE_SYNTHETIC_CLASS_MEMBERS("rsy", "Hide Synthetic Class Members"),
        DECOMPILE_INNER_CLASSES("din", "Decompile Inner Classes", true),
        COLLAPSE_14_CLASS_REFERENCES("dc4", "Collapse 1.4 Class References", true),
        DECOMPILE_ASSERTIONS("das", "Decompile Assertions", true),
        HIDE_EMPTY_SUPER_INVOCATION("hes", "Hide Empty Super Invocation", true),
        HIDE_EMPTY_DEFAULT_CONSTRUCTOR("hec", "Hide Empty Default Constructor", true),
        DECOMPILE_GENERIC_SIGNATURES("dgs", "Decompile Generic Signatures"),
        ASSUME_RETURN_NOT_THROWING_EXCEPTIONS("ner", "Assume return not throwing exceptions", true),
        DECOMPILE_ENUMS("den", "Decompile enumerations", true),
        REMOVE_GETCLASS("rgn", "Remove getClass()", true),
        OUTPUT_NUMBERIC_LITERALS("lit", "Output numeric literals 'as-is'"),
        ENCODE_UNICODE("asc", "Encode non-ASCII as unicode escapes"),
        INT_1_AS_BOOLEAN_TRUE("bto", "Assume int 1 is boolean true", true),
        ALLOW_NOT_SET_SYNTHETIC("nns", "Allow not set synthetic attribute", true),
        NAMELESS_TYPES_AS_OBJECT("uto", "Consider nameless types as java.lang.Object", true),
        RECOVER_VARIABLE_NAMES("udv", "Recover variable names", true),
        REMOVE_EMPTY_EXCEPTIONS("rer", "Remove empty exceptions", true),
        DEINLINE_FINALLY("fdi", "De-inline finally", true),
        RENAME_AMBIGIOUS_MEMBERS("ren", "Rename ambigious members"),
        REMOVE_INTELLIJ_NOTNULL("inn", "Remove IntelliJ @NotNull", true),
        DECOMPILE_LAMBDA_TO_ANONYMOUS("lac", "Decompile lambdas to anonymous classes");

        private String name;
        private String param;
        private boolean on;

        Settings(String param, String name) {
            this(param, name, false);
        }

        Settings(String param, String name, boolean on) {
            this.name = name;
            this.param = param;
            this.on = on;
        }

        public String getText() {
            return name;
        }

        public boolean isDefaultOn() {
            return on;
        }

        public String getParam() {
            return param;
        }
    }
}
