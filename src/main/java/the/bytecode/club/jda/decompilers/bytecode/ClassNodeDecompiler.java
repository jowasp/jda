package the.bytecode.club.jda.decompilers.bytecode;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InnerClassNode;
import org.objectweb.asm.tree.MethodNode;
import the.bytecode.club.jda.JDA;
import the.bytecode.club.jda.decompilers.Decompiler;
import the.bytecode.club.jda.settings.DecompilerSettings;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Konloch
 * @author Bibl
 */

public class ClassNodeDecompiler extends Decompiler {

    public ClassNodeDecompiler() {
        for (Settings setting : Settings.values()) {
            settings.registerSetting(setting);
        }
    }

    @Override
    public String getName() {
        return "Bytecode";
    }

    public String decompileClassNode(String containerName, ClassNode cn) {
        return decompile(new PrefixedStringBuilder(), new ArrayList<>(), containerName, cn).toString();
    }

    protected PrefixedStringBuilder decompile(PrefixedStringBuilder sb, ArrayList<String> decompiledClasses, String containerName, ClassNode cn) {
        ArrayList<String> unableToDecompile = new ArrayList<>();
        decompiledClasses.add(cn.name);
        sb.append(getAccessString(cn.access));
        sb.append(" ");
        sb.append(cn.name);
        if (cn.superName != null && !cn.superName.equals("java/lang/Object")) {
            sb.append(" extends ");
            sb.append(cn.superName);
        }

        int amountOfInterfaces = cn.interfaces.size();
        if (amountOfInterfaces > 0) {
            sb.append(" implements ");
            sb.append(cn.interfaces.get(0));
            if (amountOfInterfaces > 1) {
                // sb.append(",");
            }
            for (int i = 1; i < amountOfInterfaces; i++) {
                sb.append(", ");
                sb.append(cn.interfaces.get(i));
            }
        }
        sb.append(" {");
        sb.append(JDA.nl);

        for (Iterator<FieldNode> it = cn.fields.iterator(); it.hasNext(); ) {
            sb.append("     ");
            getFieldNodeDecompiler(sb, it).decompile();
            sb.append(JDA.nl);
            if (!it.hasNext())
                sb.append(JDA.nl);
        }

        for (Iterator<MethodNode> it = cn.methods.iterator(); it.hasNext(); ) {
            getMethodNodeDecompiler(sb, cn, it).decompile();
            if (it.hasNext())
                sb.append(JDA.nl);
        }

        if (getSettings().isSelected(Settings.DECOMPILE_INNER_CLASSES))
            for (InnerClassNode innerClassNode : cn.innerClasses) {
                String innerClassName = innerClassNode.name;
                if ((innerClassName != null) && !decompiledClasses.contains(innerClassName)) {
                    decompiledClasses.add(innerClassName);
                    ClassNode cn1 = JDA.getClassNode(containerName, innerClassName);
                    if (cn1 != null) {
                        sb.appendPrefix("     ");
                        sb.append(JDA.nl + JDA.nl);
                        sb = decompile(sb, decompiledClasses, containerName, cn1);
                        sb.trimPrefix(5);
                        sb.append(JDA.nl);
                    } else {
                        unableToDecompile.add(innerClassName);
                    }
                }
            }

        if (!unableToDecompile.isEmpty()) {
            sb.append("// The following inner classes couldn't be decompiled: ");
            for (String s : unableToDecompile) {
                sb.append(s);
                sb.append(" ");
            }
            sb.append(JDA.nl);
        }

        sb.append("}");
        // System.out.println("Wrote end for " + cn.name +
        // " with prefix length: " + sb.prefix.length());
        return sb;
    }

    protected FieldNodeDecompiler getFieldNodeDecompiler(PrefixedStringBuilder sb, Iterator<FieldNode> it) {
        return new FieldNodeDecompiler(sb, it.next());
    }

    protected MethodNodeDecompiler getMethodNodeDecompiler(PrefixedStringBuilder sb, ClassNode cn, Iterator<MethodNode> it) {
        return new MethodNodeDecompiler(this, sb, it.next(), cn);
    }

    public static String getAccessString(int access) {
        List<String> tokens = new ArrayList<>();
        if ((access & Opcodes.ACC_PUBLIC) != 0)
            tokens.add("public");
        if ((access & Opcodes.ACC_PRIVATE) != 0)
            tokens.add("private");
        if ((access & Opcodes.ACC_PROTECTED) != 0)
            tokens.add("protected");
        if ((access & Opcodes.ACC_FINAL) != 0)
            tokens.add("final");
        if ((access & Opcodes.ACC_SYNTHETIC) != 0)
            tokens.add("synthetic");
        // if ((access & Opcodes.ACC_SUPER) != 0)
        // tokens.add("super"); implied by invokespecial insn
        if ((access & Opcodes.ACC_ABSTRACT) != 0)
            tokens.add("abstract");
        if ((access & Opcodes.ACC_INTERFACE) != 0)
            tokens.add("interface");
        if ((access & Opcodes.ACC_ENUM) != 0)
            tokens.add("enum");
        if ((access & Opcodes.ACC_ANNOTATION) != 0)
            tokens.add("annotation");
        if (!tokens.contains("interface") && !tokens.contains("enum") && !tokens.contains("annotation"))
            tokens.add("class");
        if (tokens.size() == 0)
            return "[Error parsing]";

        // hackery delimeters
        StringBuilder sb = new StringBuilder(tokens.get(0));
        for (int i = 1; i < tokens.size(); i++) {
            sb.append(" ");
            sb.append(tokens.get(i));
        }
        return sb.toString();
    }

    @Override
    public void decompileToZip(String zipName) {
    }

    public enum Settings implements DecompilerSettings.SettingsEntry {
        DEBUG_HELPERS("debug-helpers", "Debug Helpers", true),
        APPEND_BRACKETS_TO_LABELS("append-brackets-to-labels", "Append Brackets to Labels", true),
        SHOW_METHOD_DESCRIPTORS("show-method-descriptors", "Show Method Descriptors", true),
        DECOMPILE_INNER_CLASSES("decompile-inner-classes", "Decompile Inner Classes", true);

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