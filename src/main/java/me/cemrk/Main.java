package me.cemrk;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import sun.misc.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class Main {

    private static ClassNode readClass(byte[] bytes) {
        ClassNode node = new ClassNode();
        ClassReader cr = new ClassReader(bytes);
        cr.accept(node, ClassReader.SKIP_DEBUG | ClassReader.EXPAND_FRAMES);

        return node;
    }

    private static byte[] writeClass(ClassNode node) {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        node.accept(cw);

        return cw.toByteArray();
    }

    public static void main(String[] args) throws IOException {
        ZipFile rt = new ZipFile("rt.jar");
        ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(Paths.get("rt.mod.jar")));
        rt.stream().forEach(entry -> {
            try {
                if (!entry.getName().equals("java/lang/String.class")) {
                    zos.putNextEntry(entry);
                    zos.write(IOUtils.readAllBytes(rt.getInputStream(entry)));
                    zos.closeEntry();
                    return;
                }
                InputStream is = rt.getInputStream(entry);
                byte[] data = IOUtils.readAllBytes(is);
                is.close();

                ClassNode node = readClass(data);

                modifyString(node);

                zos.putNextEntry(new ZipEntry(entry.getName()));
                zos.write(writeClass(node));
                zos.closeEntry();

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        rt.close();
        zos.close();
    }

    private static void modifyString(ClassNode node) throws IOException {
        byte[] data = IOUtils.readAllBytes(Objects.requireNonNull(Main.class.getResourceAsStream("/me/cemrk/StringEx.class")));
        ClassNode stringEx = readClass(data);

        node.access ^= Opcodes.ACC_FINAL; // remove final from the string
        node.fields.forEach(field -> {
            if (field.name.equals("value") || field.name.equals("hash")) {
                field.access = Opcodes.ACC_PUBLIC; // set access of value & hash field to public
            }
        });

        stringEx.methods.forEach(m -> {
            if (m.name.contains("<")) return;
            fixInsns(m.instructions);
            node.methods.add(m);
        });
    }

    private static void fixInsns(InsnList insns) {
        for (AbstractInsnNode _insn : insns) {
            switch (_insn.getOpcode()) {
                case Opcodes.GETFIELD:
                case Opcodes.PUTFIELD:
                    FieldInsnNode fieldInsn = (FieldInsnNode) _insn;
                    ((FieldInsnNode) _insn).owner = fieldInsn.owner.replace("me/cemrk/StringEx", "java/lang/String");
                    break;
                case Opcodes.INVOKEVIRTUAL:
                    MethodInsnNode methodInsn = (MethodInsnNode) _insn;
                    ((MethodInsnNode) _insn).owner = methodInsn.owner.replace("me/cemrk/StringEx", "java/lang/String");
                    break;
            }
        }
    }
}